package io.alapierre.ksef.fop.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * HTTP(S) resource root.
 */
final class UrlResourceRoot extends ResourceRoot {

    private static final Logger log = LoggerFactory.getLogger(UrlResourceRoot.class);

    private static final int REMOTE_TIMEOUT_MS = 10_000;
    private static final int MAX_REMOTE_BYTES = 16 * 1024 * 1024;

    private final URI baseUri;

    private UrlResourceRoot(@NotNull URI baseUri) {
        this.baseUri = baseUri;
    }

    static @NotNull UrlResourceRoot canonicalize(@NotNull URI uri) throws TransformerException {
        URI normalized = parseHttpUri(uri);
        String host = normalized.getHost();
        if (host == null || host.isEmpty()) {
            throw new TransformerException("HTTP resource root must have a host: " + uri);
        }
        return new UrlResourceRoot(stripTrailingSlash(normalized));
    }

    @NotNull
    URI getBaseUri() {
        return baseUri;
    }

    @Override
    boolean isHttp() {
        return true;
    }

    @Override
    boolean contains(@NotNull String url) {
        return containsUri(URI.create(url).normalize());
    }

    @NotNull
    URI resolveRelative(@NotNull String relativePath) {
        String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return baseUri.resolve(path).normalize();
    }

    @Override
    @Nullable
    Source tryFetchAbsolute(@NotNull String url) throws TransformerException {
        if (!contains(url)) {
            return null;
        }
        return fetch(resolveUri(url));
    }

    @Override
    @Nullable
    Source tryFetchRelativeToBase(@NotNull String href, @NotNull String base) throws TransformerException {
        if (!contains(base) || href.contains(":")) {
            return null;
        }
        URI resolved = URI.create(base).resolve(href).normalize();
        if (!containsUri(resolved)) {
            throw new TransformerException("Remote template href escapes configured HTTP resource root '"
                    + baseUri + "': " + href + " (base: " + base + ") resolved to " + resolved);
        }
        return fetch(resolved);
    }

    @Override
    @Nullable
    Source tryResolveRelative(@NotNull String relativePath) {
        return tryFetch(resolveRelative(relativePath));
    }

    @Override
    @NotNull
    Optional<String> tryResolvePublicUri(@NotNull String relativePath) {
        URI url = resolveRelative(relativePath);
        return tryFetch(url) != null ? Optional.of(url.toString()) : Optional.empty();
    }

    @Nullable
    Source tryFetch(@NotNull URI url) {
        try {
            return fetch(url);
        } catch (TransformerException e) {
            log.debug("Remote resource miss for {}: {}", url, e.getMessage());
            return null;
        }
    }

    @NotNull
    Source fetch(@NotNull URI url) throws TransformerException {
        String urlString = url.toString();
        log.debug("XSLT: fetching resource from remote server: {}", urlString);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setConnectTimeout(REMOTE_TIMEOUT_MS);
            connection.setReadTimeout(REMOTE_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/xslt+xml, text/xml, application/xml, */*");
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new TransformerException(
                        "Remote resource server returned HTTP " + status + " for URL: " + urlString);
            }

            byte[] body = readFully(connection.getInputStream());
            log.debug("XSLT: fetched {} bytes from {}", body.length, urlString);
            return new StreamSource(new ByteArrayInputStream(body), urlString);

        } catch (IOException e) {
            throw new TransformerException(
                    "Failed to fetch remote resource (server may be down): " + urlString
                            + " — " + e.getMessage(), e);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private boolean containsUri(@NotNull URI url) {
        URI normalized = url.normalize();
        if (normalized.equals(baseUri)) {
            return true;
        }
        String base = baseUri.toString();
        String target = normalized.toString();
        return target.startsWith(base + "/");
    }

    @NotNull
    private static URI resolveUri(@NotNull String url) {
        return URI.create(url).normalize();
    }

    @NotNull
    private static URI parseHttpUri(@NotNull URI uri) throws TransformerException {
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new TransformerException(
                    "HTTP resource root must use http or https scheme: " + uri);
        }
        String schemeLower = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(schemeLower) && !"https".equals(schemeLower)) {
            throw new TransformerException(
                    "HTTP resource root must use http or https scheme: " + uri);
        }
        try {
            return new URI(schemeLower, uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    uri.getPath(), uri.getQuery(), uri.getFragment()).normalize();
        } catch (URISyntaxException e) {
            throw new TransformerException("Invalid HTTP resource root: " + uri, e);
        }
    }

    @NotNull
    private static URI stripTrailingSlash(@NotNull URI uri) {
        String s = uri.toString();
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == '/') {
            end--;
        }
        if (end == s.length()) {
            return uri;
        }
        return URI.create(s.substring(0, end));
    }

    private static byte[] readFully(InputStream in) throws IOException, TransformerException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        while ((n = in.read(chunk)) != -1) {
            buf.write(chunk, 0, n);
            if (buf.size() > MAX_REMOTE_BYTES) {
                throw new TransformerException("Remote resource exceeds maximum allowed size of "
                        + MAX_REMOTE_BYTES + " bytes");
            }
        }
        return buf.toByteArray();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UrlResourceRoot)) return false;
        UrlResourceRoot that = (UrlResourceRoot) o;
        return Objects.equals(baseUri, that.baseUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseUri);
    }

    @Override
    public String toString() {
        return "UrlResourceRoot{" + baseUri + "}";
    }
}
