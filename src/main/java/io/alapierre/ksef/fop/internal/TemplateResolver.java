/*
 * SPDX-License-identifier: Apache-2.0
 */
package io.alapierre.ksef.fop.internal;

import net.sf.saxon.trans.NonDelegatingURIResolver;
import org.xmlresolver.CatalogManager;
import org.xmlresolver.ResolverFeature;
import org.xmlresolver.XMLResolverConfiguration;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * URI resolver for XSLT template loading.
 *
 * <p>Every resource is looked up in the following order:</p>
 * <ol>
 *   <li>If the resource name starts with {@code http:} or {@code https:}, it is resolved
 *       using an XML catalog.</li>
 *   <li>Each configured filesystem root, in insertion order.</li>
 *   <li>Classpath.</li>
 * </ol>
 * <p>Anything unresolved throws {@link TransformerException}.</p>
 *
 * <p><strong>Containment guarantee.</strong> For each filesystem root, the resolved path is
 * verified to remain within the root. Both {@code ..} traversal and symlinks pointing outside
 * the root are rejected.</p>
 *
 * <p>System IDs produced by this resolver use the {@code vfs:} scheme
 * (e.g. {@code vfs:///templates/fa3/ksef_invoice.xsl}) so that Saxon treats them as
 * absolute and does not attempt to resolve them against the JVM working directory.</p>
 *
 * <p>This class is <strong>internal</strong> and may change between releases.</p>
 */
public class TemplateResolver implements NonDelegatingURIResolver {

    public static final String HTTP_PREFIX = "http://";
    public static final String HTTPS_PREFIX = "https://";
    private static final String VFS_SCHEME = "vfs";
    private static final String VFS_PREFIX = VFS_SCHEME + ":///";
    private static final URI VFS_ROOT = URI.create(VFS_PREFIX);

    private final List<Path> roots;
    private final CatalogManager catalogManager;

    public TemplateResolver(List<Path> roots) throws TransformerException {
        this.roots = new ArrayList<>(roots.size());
        for (Path root : roots) {
            this.roots.add(canonicalize(root));
        }
        XMLResolverConfiguration config = new XMLResolverConfiguration();
        config.setFeature(ResolverFeature.CATALOG_FILES,
                Collections.singletonList("classpath:catalog.xml"));
        this.catalogManager = config.getFeature(ResolverFeature.CATALOG_MANAGER);
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        if (href == null) {
            throw new TransformerException("Cannot resolve null href");
        }

        // 1. http: / https: URIs → XML catalog (allowlist); anything not listed throws
        if (href.startsWith(HTTP_PREFIX) || href.startsWith(HTTPS_PREFIX)) {
            URI mapped = catalogManager.lookupURI(href);
            if (mapped == null) {
                throw new TransformerException("External URI not in catalog: " + href);
            }
            if (!"classpath".equals(mapped.getScheme())) {
                throw new TransformerException("Catalog must map to a classpath: URI, got: " + mapped);
            }
            href = "/" + mapped.getSchemeSpecificPart();
        }

        // Reject any remaining URI scheme
        if (href.contains(":")) {
            throw new TransformerException("Unsupported URI scheme in href: " + href);
        }

        // 2 + 3. Derive the effective relative path, then try filesystem roots and classpath
        URI baseUri = parseBase(base);
        URI virtualUri = resolveUri(baseUri, href);
        // Remove leading slash
        String relativePath = virtualUri.getPath().substring(1);

        for (Path root : roots) {
            Source s = tryFilesystem(root, relativePath);
            if (s != null) return s;
        }

        Source s = tryClasspath(relativePath);
        if (s != null) return s;

        throw new TransformerException("Template not found: " + href + (Strings.isEmpty(base) ? "" : " (base: " + base + ")"));
    }

    // -----------------------------------------------------------------------
    // Effective path computation
    // -----------------------------------------------------------------------

    /**
     * Canonicalises {@code path} via {@code toRealPath()}, resolving symlinks and {@code ..} segments.
     */
    private static Path canonicalize(Path path) throws TransformerException {
        try {
            return path.toRealPath();
        } catch (IOException e) {
            throw new TransformerException("File is not accessible: " + path, e);
        }
    }

    /**
     * Returns {@code uri} unchanged if its scheme is {@code vfs:}, otherwise throws.
     */
    private static URI requireVfsScheme(URI uri) throws TransformerException {
        if (!VFS_SCHEME.equals(uri.getScheme())) {
            throw new TransformerException("Unexpected URI scheme: " + uri);
        }
        return uri;
    }

    /**
     * Parses and validates {@code base}.
     *
     * @return {@link #VFS_ROOT} if {@code base} is absent (entry-point call), or the parsed URI
     * @throws TransformerException if {@code base} is not a valid URI or not a {@code vfs:} URI
     */
    private static URI parseBase(String base) throws TransformerException {
        if (Strings.isEmpty(base)) {
            return VFS_ROOT;
        }
        return requireVfsScheme(parseUri(base));
    }

    /**
     * Resolves {@code href} against {@code baseUri} and normalises the result.
     */
    private static URI resolveUri(URI baseUri, String href) throws TransformerException {
        return baseUri.resolve(encodePathUri(href)).normalize();
    }

    /**
     * Parses {@code uri} as a URI, rethrowing {@link URISyntaxException} as {@link TransformerException}.
     */
    private static URI parseUri(String uri) throws TransformerException {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new TransformerException("Invalid resource path: " + uri, e);
        }
    }

    /**
     * Wraps {@code path} in a scheme-less relative URI, percent-encoding any reserved characters.
     */
    private static URI encodePathUri(String path) throws TransformerException {
        try {
            return new URI(null, null, path, null);
        } catch (URISyntaxException e) {
            // Should never occur
            throw new TransformerException("Invalid resource path: " + path, e);
        }
    }

    // -----------------------------------------------------------------------
    // Lookup strategies
    // -----------------------------------------------------------------------

    /**
     * Tries to load {@code relativePath} from under {@code root}.
     *
     * <p>Containment check: after resolving symlinks via {@code toRealPath()}, the real path must
     * remain under {@code root}.</p>
     */
    private static Source tryFilesystem(Path root, String relativePath) throws TransformerException {
        Path candidate = root.resolve(relativePath);
        if (!Files.exists(candidate)) {
            return null;
        }

        // Resolve symlinks to check for symlink escape or path traversal.
        try {
            Path realCandidate = candidate.toRealPath();
            if (!realCandidate.startsWith(root)) {
                throw new TransformerException("Path traversal rejected: " + relativePath);
            }
            return new StreamSource(Files.newInputStream(realCandidate), VFS_PREFIX + relativePath);
        } catch (NoSuchFileException e) {
            // TOCTOU: the file disappeared after the check
            // The caller will throw an appropriate error.
            return null;
        } catch (IOException e) {
            throw new TransformerException("File is not accessible: " + candidate, e);
        }
    }

    /**
     * Tries to load {@code path} as a classpath resource.
     */
    private static Source tryClasspath(String relativePath) {
        InputStream is = TemplateResolver.class.getClassLoader().getResourceAsStream(relativePath);
        if (is == null) return null;
        return new StreamSource(is, VFS_PREFIX + relativePath);
    }
}
