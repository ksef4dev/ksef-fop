package io.alapierre.ksef.fop.http;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

/**
 * Pluggable HTTP client for fetching remote template resources (XSLT, logos, labels).
 * Host applications register an implementation once at startup via
 * {@link RemoteResourceFetchers#setGlobal(RemoteResourceFetcher)}.
 * When none is registered, ksef-fop uses {@link HttpURLConnectionRemoteResourceFetcher}.
 * <p>
 * <strong>Security:</strong> the host application is responsible for providing a correct,
 * trustworthy implementation (or relying on the built-in default). A flawed custom fetcher
 * can undermine containment guarantees — see the README section on remote HTTP fetching.
 */
@FunctionalInterface
public interface RemoteResourceFetcher {

    /**
     * Performs an HTTP GET for the given absolute URL.
     * <p>
     * Implementations must not follow redirects and should enforce a reasonable body size limit.
     *
     * @param url absolute {@code http:} or {@code https:} URI
     * @return response body bytes
     * @throws RemoteResourceFetchException when the request fails or returns a non-success status
     */
    byte @NotNull [] fetch(@NotNull URI url) throws RemoteResourceFetchException;
}
