package io.alapierre.ksef.fop.http;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registry for the active {@link RemoteResourceFetcher}.
 */
public final class RemoteResourceFetchers {

    private static volatile RemoteResourceFetcher global = HttpURLConnectionRemoteResourceFetcher.INSTANCE;

    private RemoteResourceFetchers() {
    }

    @NotNull
    public static RemoteResourceFetcher get() {
        return global;
    }

    public static void setGlobal(@Nullable RemoteResourceFetcher fetcher) {
        global = fetcher != null ? fetcher : HttpURLConnectionRemoteResourceFetcher.INSTANCE;
    }
}
