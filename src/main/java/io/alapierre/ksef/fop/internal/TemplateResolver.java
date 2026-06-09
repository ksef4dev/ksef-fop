/*
 * SPDX-License-identifier: Apache-2.0
 */
package io.alapierre.ksef.fop.internal;

import net.sf.saxon.trans.NonDelegatingURIResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlresolver.CatalogManager;
import org.xmlresolver.ResolverFeature;
import org.xmlresolver.XMLResolverConfiguration;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * URI resolver for XSLT template loading.
 *
 * <p>Every resource is looked up in the following order:</p>
 * <ol>
 *   <li>When HTTP(S) resource roots are configured: absolute {@code href} values under such a
 *       root, and relative {@code href} values whose {@code base} sits under such a root, are
 *       fetched over HTTP (GET; redirects are not followed).</li>
 *   <li>If the resource name starts with {@code http:} or {@code https:} and was not loaded
 *       in step&nbsp;1, it is resolved using the XML catalog (allowlisted entries rewrite to
 *       {@code classpath:} resources — no network I/O to the original URL).</li>
 *   <li>Each configured resource root ({@code file:} directory or {@code http(s):} base), in
 *       insertion order.</li>
 *   <li>Classpath.</li>
 * </ol>
 * <p>Anything unresolved throws {@link TransformerException}.</p>
 *
 * <p><strong>Containment guarantee.</strong> For each filesystem root, the resolved path is
 * verified to remain within the root. Both {@code ..} traversal and symlinks pointing outside
 * the root are rejected. For HTTP roots, a URL resolving outside the configured base
 * ({@code ../} escapes, authority changes) is rejected.</p>
 *
 * <p>System IDs produced by this resolver use the {@code vfs:} scheme
 * (e.g. {@code vfs:///templates/fa3/ksef_invoice.xsl}) so that Saxon treats them as
 * absolute and does not attempt to resolve them against the JVM working directory.
 * Resources fetched over HTTP use the source URL as the system ID so Saxon resolves relative
 * {@code xsl:import}/{@code include} hrefs against it.</p>
 *
 * <p>This class is <strong>internal</strong> and may change between releases.</p>
 */
public class TemplateResolver implements NonDelegatingURIResolver {

    private static final Logger log = LoggerFactory.getLogger(TemplateResolver.class);

    /** Connect + read timeout used for every HTTP fetch from the remote template server. */
    private static final int REMOTE_TIMEOUT_MS = 10_000;

    /** Hard cap on the body size of a single remotely fetched stylesheet, to avoid OOM. */
    private static final int MAX_REMOTE_TEMPLATE_BYTES = 16 * 1024 * 1024;

    public static final String HTTP_PREFIX = "http://";
    public static final String HTTPS_PREFIX = "https://";
    private static final String VFS_SCHEME = "vfs";
    private static final String VFS_PREFIX = VFS_SCHEME + ":///";
    private static final URI VFS_ROOT = URI.create(VFS_PREFIX);

    private final List<ResourceRoot> resourceRoots;
    private final CatalogManager catalogManager;

    /** Constructs a resolver without user resource roots (classpath / catalog only). */
    public TemplateResolver() throws TransformerException {
        this(Collections.emptyList());
    }

    /**
     * Constructs a resolver from ordered resource roots.
     *
     * @param roots ordered list of filesystem ({@code file:}) or HTTP(S) base URIs
     */
    public TemplateResolver(List<URI> roots) throws TransformerException {
        try {
            this.resourceRoots = ResourceRoots.canonicalize(roots);
        } catch (IOException e) {
            throw new TransformerException("Resource root is not accessible: " + e.getMessage(), e);
        }
        XMLResolverConfiguration config = new XMLResolverConfiguration();
        config.setFeature(ResolverFeature.CATALOG_FILES,
                Collections.singletonList("classpath:catalog.xml"));
        this.catalogManager = config.getFeature(ResolverFeature.CATALOG_MANAGER);
    }

    /**
     * Validates and normalizes an HTTP(S) resource-root base URL.
     */
    @NotNull
    static String canonicalizeHttpBaseUrl(@NotNull String baseUrl) throws TransformerException {
        String trimmed = baseUrl.trim();
        if (trimmed.isEmpty()) {
            throw new TransformerException("HTTP resource root must not be blank");
        }
        URI uri = parseUri(trimmed);
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new TransformerException(
                    "HTTP resource root must use http or https scheme: " + baseUrl);
        }
        String schemeLower = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(schemeLower) && !"https".equals(schemeLower)) {
            throw new TransformerException(
                    "HTTP resource root must use http or https scheme: " + baseUrl);
        }
        String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            throw new TransformerException(
                    "HTTP resource root must have a host: " + baseUrl);
        }
        return stripTrailingSlashes(uri.normalize().toString());
    }

    private static String stripTrailingSlashes(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return end == value.length() ? value : value.substring(0, end);
    }

    /**
     * True if {@code url} equals {@code httpRoot} or sits under it as a path segment.
     */
    public static boolean isUnderRemoteBase(@NotNull String url, @NotNull String httpRoot) {
        return url.equals(httpRoot) || url.startsWith(httpRoot + "/");
    }

    @NotNull
    List<ResourceRoot> getResourceRoots() {
        return resourceRoots;
    }

    public boolean hasHttpResourceRoots() {
        for (ResourceRoot root : resourceRoots) {
            if (root.isHttp()) {
                return true;
            }
        }
        return false;
    }

    public boolean isUnderAnyHttpRoot(@NotNull String url) {
        for (ResourceRoot root : resourceRoots) {
            if (root.isUnder(url)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        Optional<Source> resolved = tryResolve(href, base);
        if (resolved.isPresent()) {
            Source source = resolved.get();
            String systemId = source.getSystemId();
            log.debug("XSLT: resolved href='{}' base='{}' -> systemId={}",
                    href, Strings.isEmpty(base) ? "(none)" : base, systemId);
            return source;
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
     * {@link TransformerException}. "Not found" for resource roots and classpath is the only
     * condition that collapses to an empty result.</p>
     */
    public Optional<Source> tryResolve(String href, String base) throws TransformerException {
        if (href == null) {
            throw new TransformerException("Cannot resolve null href");
        }

        // 0. Remote HTTP/HTTPS fetch from configured resource roots.
        //    a) Absolute href under an HTTP root.
        //    b) Relative href whose parent stylesheet was fetched from an HTTP root
        //       (base is the HTTP URL of the parent, set as systemId when it was loaded).
        if (isUnderAnyHttpRoot(href)) {
            return Optional.of(fetchFromRemote(requireUnderHttpRoot(normalizeUrl(href), href, base)));
        }
        if (base != null && isUnderAnyHttpRoot(base) && !href.contains(":")) {
            String resolved = URI.create(base).resolve(href).normalize().toString();
            return Optional.of(fetchFromRemote(requireUnderHttpRoot(resolved, href, base)));
        }

        // 1. http: / https: URIs → XML catalog (allowlist); anything not listed throws
        if (href.startsWith(HTTP_PREFIX) || href.startsWith(HTTPS_PREFIX)) {
            String requestedUri = href;
            URI mapped = catalogManager.lookupURI(href);
            if (mapped == null) {
                throw new TransformerException("External URI not in catalog: " + href);
            }
            if (!"classpath".equals(mapped.getScheme())) {
                throw new TransformerException("Catalog must map to a classpath: URI, got: " + mapped);
            }
            log.debug(
                    "XSLT: stylesheet URL '{}' rewritten by classpath catalog to 'classpath:{}' — "
                            + "bytes are loaded from the classpath/JAR only, never via HTTP from that URL "
                            + "(having template-server up or down does not change this; remove the catalog rewrite if you want a real HTTP fetch).",
                    requestedUri,
                    mapped.getSchemeSpecificPart());
            href = "/" + mapped.getSchemeSpecificPart();
        }

        // Reject any remaining URI scheme
        if (href.contains(":")) {
            throw new TransformerException("Unsupported URI scheme in href: " + href);
        }

        // 2 + 3. Derive the effective relative path, then try resource roots and classpath.
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

        for (ResourceRoot root : resourceRoots) {
            Source s = tryResolveAtRoot(root, relativePath);
            if (s != null) {
                return Optional.of(s);
            }
        }

        Source s = tryClasspath(relativePath);
        return Optional.ofNullable(s);
    }

    /**
     * Collects every resolvable source for {@code href} — one per configured resource root
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

        List<Source> sources = new ArrayList<>(resourceRoots.size() + 1);
        for (ResourceRoot root : resourceRoots) {
            Source s = tryResolveAtRoot(root, relativePath);
            if (s != null) {
                sources.add(s);
            }
        }
        Source cp = tryClasspath(relativePath);
        if (cp != null) sources.add(cp);
        return sources;
    }

    /**
     * Resolves a relative resource to a public URI suitable for FOP external references
     * ({@code file:} or {@code http(s):}).
     */
    public Optional<String> tryResolvePublicUri(String href) {
        if (href == null || href.contains(":")) {
            return Optional.empty();
        }
        String relativePath = href.startsWith("/") ? href.substring(1) : href;

        for (ResourceRoot root : resourceRoots) {
            if (root.isHttp()) {
                String url = root.resolveUrl(relativePath);
                if (tryFetchFromRemote(url) != null) {
                    return Optional.of(url);
                }
            } else {
                Optional<Path> resolved = FilesystemRoots.resolveFileWithin(
                        root.getFilesystemPath(), relativePath);
                if (resolved.isPresent()) {
                    return Optional.of(resolved.get().toUri().toString());
                }
            }
        }

        URL classpathUrl = TemplateResolver.class.getClassLoader().getResource(relativePath);
        if (classpathUrl != null) {
            return Optional.of(classpathUrl.toString());
        }
        return Optional.empty();
    }

    // -----------------------------------------------------------------------
    // Remote HTTP fetch
    // -----------------------------------------------------------------------

    private String requireUnderHttpRoot(String resolvedUrl, String href, String base) throws TransformerException {
        if (!isUnderAnyHttpRoot(resolvedUrl)) {
            throw new TransformerException("Remote template href escapes configured HTTP resource root(s): "
                    + href
                    + (Strings.isEmpty(base) ? "" : " (base: " + base + ")")
                    + " resolved to " + resolvedUrl);
        }
        return resolvedUrl;
    }

    private static String normalizeUrl(String url) {
        return URI.create(url).normalize().toString();
    }

    @Nullable
    private Source tryResolveAtRoot(ResourceRoot root, String relativePath) throws TransformerException {
        if (root.isHttp()) {
            return tryFetchFromRemote(root.resolveUrl(relativePath));
        }
        return tryFilesystem(root.getFilesystemPath(), relativePath);
    }

    /**
     * Single GET; returns {@code null} on 404 / network error (used when probing HTTP roots).
     */
    @Nullable
    private Source tryFetchFromRemote(String url) {
        try {
            return fetchFromRemote(url);
        } catch (TransformerException e) {
            log.debug("Remote resource miss for {}: {}", url, e.getMessage());
            return null;
        }
    }

    private Source fetchFromRemote(String url) throws TransformerException {
        log.debug("XSLT: fetching resource from remote server: {}", url);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(REMOTE_TIMEOUT_MS);
            connection.setReadTimeout(REMOTE_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/xslt+xml, text/xml, application/xml, */*");
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new TransformerException(
                        "Remote resource server returned HTTP " + status + " for URL: " + url);
            }

            byte[] body = readFully(connection.getInputStream());
            log.debug("XSLT: fetched {} bytes from {}", body.length, url);
            return new StreamSource(new ByteArrayInputStream(body), url);

        } catch (IOException e) {
            throw new TransformerException(
                    "Failed to fetch remote resource (server may be down): " + url + " — " + e.getMessage(), e);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static byte[] readFully(InputStream in) throws IOException, TransformerException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        while ((n = in.read(chunk)) != -1) {
            buf.write(chunk, 0, n);
            if (buf.size() > MAX_REMOTE_TEMPLATE_BYTES) {
                throw new TransformerException("Remote resource exceeds maximum allowed size of "
                        + MAX_REMOTE_TEMPLATE_BYTES + " bytes");
            }
        }
        return buf.toByteArray();
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
