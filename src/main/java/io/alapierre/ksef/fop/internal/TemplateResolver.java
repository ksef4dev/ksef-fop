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
 * <p>Resolution order for each {@code href}:</p>
 * <ol>
 *   <li>When {@code remoteBaseUrl} is set: absolute or relatively-resolved URLs under that base
 *       are fetched over HTTP from the template server.</li>
 *   <li>Remaining {@code http:} / {@code https:} URIs are rewritten via the XML catalog to a
 *       {@code classpath:} resource (no network I/O to the original URL).</li>
 *   <li>Filesystem template roots, then classpath, for non-URL paths.</li>
 * </ol>
 *
 * <p><strong>Containment guarantee.</strong> For each filesystem root, the resolved path is
 * verified to remain within the root. Both {@code ..} traversal and symlinks pointing outside
 * the root are rejected.</p>
 *
 * <p>System IDs produced by this resolver use the {@code vfs:} scheme
 * (e.g. {@code vfs:///templates/fa3/ksef_invoice.xsl}) so that Saxon treats them as
 * absolute and does not attempt to resolve them against the JVM working directory.</p>
 *
 * <p>Remote fetching is contained: a URL resolving outside {@code remoteBaseUrl} ({@code ../}
 * escapes, authority changes) is rejected and HTTP redirects are not followed.</p>
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

    private final List<Path> roots;
    private final CatalogManager catalogManager;

    /**
     * When non-null, hrefs that start with this prefix are fetched over HTTP instead of
     * being looked up in the XML catalog.  All other {@code http(s):} hrefs still use the
     * catalog (e.g. external XSD files like KodyKrajow).
     */
    @Nullable
    private final String remoteBaseUrl;

    /** Constructs a resolver without remote HTTP support (catalog / classpath only). */
    public TemplateResolver(List<Path> roots) throws TransformerException {
        this(roots, null);
    }

    /**
     * Constructs a resolver with optional remote HTTP support.
     *
     * @param roots         ordered filesystem roots searched before the classpath
     * @param remoteBaseUrl base URL of the remote template server
     *                      (e.g. {@code "http://localhost:8077/xslt"}).
     *                      Pass {@code null} to disable remote fetching.
     */
    public TemplateResolver(List<Path> roots, @Nullable String remoteBaseUrl) throws TransformerException {
        try {
            this.roots = FilesystemRoots.canonicalize(roots);
        } catch (IOException e) {
            throw new TransformerException("Template root is not accessible: " + e.getMessage(), e);
        }
        XMLResolverConfiguration config = new XMLResolverConfiguration();
        config.setFeature(ResolverFeature.CATALOG_FILES,
                Collections.singletonList("classpath:catalog.xml"));
        this.catalogManager = config.getFeature(ResolverFeature.CATALOG_MANAGER);
        this.remoteBaseUrl = canonicalizeRemoteBaseUrl(remoteBaseUrl);
    }

    /**
     * Validates and normalizes a remote template-server base URL.
     *
     * <p>{@code null} or blank (after trim) disables remote fetching. Otherwise the value must
     * be a syntactically valid {@code http:} / {@code https:} URI with a host; trailing slashes
     * are stripped.</p>
     */
    @Nullable
    static String canonicalizeRemoteBaseUrl(@Nullable String remoteBaseUrl) throws TransformerException {
        if (remoteBaseUrl == null) {
            return null;
        }
        String trimmed = remoteBaseUrl.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        URI uri = parseUri(trimmed);
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new TransformerException(
                    "Remote template base URL must use http or https scheme: " + remoteBaseUrl);
        }
        String schemeLower = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(schemeLower) && !"https".equals(schemeLower)) {
            throw new TransformerException(
                    "Remote template base URL must use http or https scheme: " + remoteBaseUrl);
        }
        String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            throw new TransformerException(
                    "Remote template base URL must have a host: " + remoteBaseUrl);
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
     * True if {@code url} equals {@code remoteBaseUrl} or sits under it as a path segment.
     */
    public static boolean isUnderRemoteBase(@NotNull String url, @NotNull String remoteBaseUrl) {
        return url.equals(remoteBaseUrl) || url.startsWith(remoteBaseUrl + "/");
    }

    List<Path> getRoots() {
        return roots;
    }

    @Nullable
    public String getRemoteBaseUrl() {
        return remoteBaseUrl;
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
     * {@link TransformerException}. "Not found" for filesystem+classpath is the only
     * condition that collapses to an empty result.</p>
     */
    public Optional<Source> tryResolve(String href, String base) throws TransformerException {
        if (href == null) {
            throw new TransformerException("Cannot resolve null href");
        }

        // 0. Remote HTTP/HTTPS fetch from the configured template server.
        //    This takes priority over the catalog so that stylesheets are loaded from
        //    the server rather than from the bundled classpath copies.
        if (remoteBaseUrl != null) {
            // a) Absolute href that belongs to the remote server.
            if (isUnderRemoteBase(href)) {
                return Optional.of(fetchFromRemote(requireUnderRemoteBase(normalizeUrl(href), href, base)));
            }
            // b) Relative href whose parent stylesheet was fetched from the remote server.
            //    base will be the HTTP URL of the parent (set as systemId when it was loaded).
            if (base != null && isUnderRemoteBase(base) && !href.contains(":")) {
                String resolved = URI.create(base).resolve(href).normalize().toString();
                return Optional.of(fetchFromRemote(requireUnderRemoteBase(resolved, href, base)));
            }
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
    // Remote HTTP fetch
    // -----------------------------------------------------------------------

    private boolean isUnderRemoteBase(String url) {
        return isUnderRemoteBase(url, remoteBaseUrl);
    }

    /**
     * Rejects a resolved URL that escapes the base (e.g. {@code ../} path escape or a
     * protocol-relative authority change), which raw href/base checks can miss.
     */
    private String requireUnderRemoteBase(String resolvedUrl, String href, String base) throws TransformerException {
        if (!isUnderRemoteBase(resolvedUrl)) {
            throw new TransformerException("Remote template href escapes the configured base '"
                    + remoteBaseUrl + "': " + href
                    + (Strings.isEmpty(base) ? "" : " (base: " + base + ")")
                    + " resolved to " + resolvedUrl);
        }
        return resolvedUrl;
    }

    private static String normalizeUrl(String url) {
        return URI.create(url).normalize().toString();
    }

    /**
     * Fetches a stylesheet over HTTP. The URL is used as the source systemId so Saxon resolves
     * relative {@code xsl:import}/{@code include} hrefs against it (re-entering as case 0b).
     */
    private Source fetchFromRemote(String url) throws TransformerException {
        log.debug("XSLT: fetching stylesheet from remote template server: {}", url);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(REMOTE_TIMEOUT_MS);
            connection.setReadTimeout(REMOTE_TIMEOUT_MS);
            // Do not follow redirects: a 3xx could send the fetch to an arbitrary host outside
            // the configured base, defeating the containment check. Treat any non-200 as an error.
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/xslt+xml, text/xml, application/xml, */*");
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new TransformerException(
                        "Remote template server returned HTTP " + status + " for URL: " + url);
            }

            // Copy the response body into memory so the connection can be closed cleanly
            // while Saxon still has an InputStream to read from.
            byte[] body = readFully(connection.getInputStream());
            log.debug("XSLT: fetched {} bytes from {}", body.length, url);
            return new StreamSource(new ByteArrayInputStream(body), url);

        } catch (IOException e) {
            throw new TransformerException(
                    "Failed to fetch remote template (server may be down): " + url + " — " + e.getMessage(), e);
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
                throw new TransformerException("Remote template exceeds maximum allowed size of "
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
