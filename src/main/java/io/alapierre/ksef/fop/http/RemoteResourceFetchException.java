package io.alapierre.ksef.fop.http;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

/**
 * Thrown when a {@link RemoteResourceFetcher} cannot load a remote resource.
 */
public class RemoteResourceFetchException extends Exception {

    private final URI url;
    private final int statusCode;

    public RemoteResourceFetchException(@NotNull URI url, int statusCode) {
        super("Remote resource server returned HTTP " + statusCode + " for URL: " + url);
        this.url = url;
        this.statusCode = statusCode;
    }

    public RemoteResourceFetchException(@NotNull URI url, @NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
        this.url = url;
        this.statusCode = -1;
    }

    @NotNull
    public URI getUrl() {
        return url;
    }

    /**
     * HTTP status when the server responded; {@code -1} for transport or IO failures.
     */
    public int getStatusCode() {
        return statusCode;
    }
}
