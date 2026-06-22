package io.alapierre.ksef.fop.internal;

import io.alapierre.ksef.fop.http.RemoteResourceFetchException;
import io.alapierre.ksef.fop.http.RemoteResourceFetcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * HTTP(S) resource root.
 */
final class UrlResourceRoot extends ResourceRoot {

    private static final Logger log = LoggerFactory.getLogger(UrlResourceRoot.class);

    private final URI baseUri;
    private final RemoteResourceFetcher fetcher;

    private UrlResourceRoot(@NotNull URI baseUri, @NotNull RemoteResourceFetcher fetcher) {
        this.baseUri = baseUri;
        this.fetcher = fetcher;
    }

    static @NotNull UrlResourceRoot canonicalize(
            @NotNull URI uri,
            @NotNull RemoteResourceFetcher fetcher
    ) throws TransformerException {
        URI normalized = parseHttpUri(uri);
        String host = normalized.getHost();
        if (host == null || host.isEmpty()) {
            throw new TransformerException("HTTP resource root must have a host: " + uri);
        }
        return new UrlResourceRoot(stripTrailingSlash(normalized), fetcher);
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
        return URI.create(baseUri + "/").resolve(path).normalize();
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
        if (!containsUri(url)) {
            throw new TransformerException("Remote template URL escapes configured HTTP resource root '"
                    + baseUri + "': " + urlString);
        }
        log.debug("XSLT: fetching resource from remote server: {}", urlString);
        try {
            byte[] body = fetcher.fetch(url);
            log.debug("XSLT: fetched {} bytes from {}", body.length, urlString);
            return new StreamSource(new ByteArrayInputStream(body), urlString);
        } catch (RemoteResourceFetchException e) {
            throw new TransformerException(e.getMessage(), e);
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
