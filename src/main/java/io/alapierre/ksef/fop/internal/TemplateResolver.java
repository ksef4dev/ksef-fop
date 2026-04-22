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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        try {
            this.roots = FilesystemRoots.canonicalize(roots);
        } catch (IOException e) {
            throw new TransformerException("Template root is not accessible: " + e.getMessage(), e);
        }
        XMLResolverConfiguration config = new XMLResolverConfiguration();
        config.setFeature(ResolverFeature.CATALOG_FILES,
                Collections.singletonList("classpath:catalog.xml"));
        this.catalogManager = config.getFeature(ResolverFeature.CATALOG_MANAGER);
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        Optional<Source> resolved = tryResolve(href, base);
        if (resolved.isPresent()) {
            return resolved.get();
        }
        throw new TransformerException("Template not found: " + href
                + (Strings.isEmpty(base) ? "" : " (base: " + base + ")"));
    }

    /**
     * Same as {@link #resolve(String, String)} but returns {@link Optional#empty()} when the
     * resource is not available, instead of throwing.
     *
     * <p>Genuine programmer errors (null {@code href}, malformed URIs, unsupported URI
     * schemes, or http/https URIs missing from the XML catalog) still propagate as
     * {@link TransformerException}. "Not found" for filesystem+classpath is the only
     * condition that collapses to an empty result.</p>
     */
    public Optional<Source> tryResolve(String href, String base) throws TransformerException {
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

        // 2 + 3. Derive the effective relative path, then try filesystem roots and classpath.
        // Root-relative hrefs bypass the base URI: this keeps them portable across whatever
        // base URI the XSLT engine feeds us (file:, vfs:, jar:, …) and avoids the need to
        // validate the base scheme when the caller has already said "this path is absolute".
        String relativePath;
        if (href.startsWith("/")) {
            relativePath = href.substring(1);
        } else {
            URI baseUri = parseBase(base);
            URI virtualUri = resolveUri(baseUri, href);
            relativePath = virtualUri.getPath().substring(1);
        }

        for (Path root : roots) {
            Source s = tryFilesystem(root, relativePath);
            if (s != null) return Optional.of(s);
        }

        Source s = tryClasspath(relativePath);
        return Optional.ofNullable(s);
    }

    /**
     * Collects every resolvable source for {@code href} — one per configured filesystem root
     * that contains a match, followed by the classpath entry if present. Unlike
     * {@link #tryResolve(String, String)}, which stops at the first hit, this is used by
     * layered resources such as i18n labels that must be overlaid on top of the classpath
     * defaults rather than replacing them.
     *
     * <p>The order returned is <em>priority order</em> (highest priority first): user roots
     * in insertion order, then classpath. Callers typically iterate and copy missing entries
     * from later sources into the accumulator built from earlier sources.</p>
     *
     * <p>Only bare or root-relative hrefs are accepted here. {@code http:} / {@code https:}
     * and any other URI scheme are rejected with {@link TransformerException} — layering
     * across the XML catalog has no well-defined meaning.</p>
     */
    public List<Source> resolveAll(String href) throws TransformerException {
        if (href == null) {
            throw new TransformerException("Cannot resolve null href");
        }
        if (href.startsWith(HTTP_PREFIX) || href.startsWith(HTTPS_PREFIX)) {
            throw new TransformerException("resolveAll does not support catalog URIs: " + href);
        }
        if (href.contains(":")) {
            throw new TransformerException("Unsupported URI scheme in href: " + href);
        }

        String relativePath = href.startsWith("/") ? href.substring(1) : href;

        List<Source> sources = new ArrayList<>(roots.size() + 1);
        for (Path root : roots) {
            Source s = tryFilesystem(root, relativePath);
            if (s != null) sources.add(s);
        }
        Source cp = tryClasspath(relativePath);
        if (cp != null) sources.add(cp);
        return sources;
    }

    // -----------------------------------------------------------------------
    // Effective path computation
    // -----------------------------------------------------------------------

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
     * <p>Containment and regular-file check are delegated to
     * {@link FilesystemRoots#resolveFileWithin(Path, String)}; symlink escapes and
     * {@code ..} traversal fall through silently to the next lookup step.</p>
     */
    private static Source tryFilesystem(Path root, String relativePath) throws TransformerException {
        Optional<Path> resolved = FilesystemRoots.resolveFileWithin(root, relativePath);
        if (!resolved.isPresent()) {
            return null;
        }
        try {
            return new StreamSource(Files.newInputStream(resolved.get()), VFS_PREFIX + relativePath);
        } catch (IOException e) {
            throw new TransformerException("File is not accessible: " + resolved.get(), e);
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
