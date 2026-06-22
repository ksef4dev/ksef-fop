package io.alapierre.ksef.fop.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RemoteResourceFetchersTest {

    @AfterEach
    void tearDown() {
        RemoteResourceFetchers.setGlobal(null);
    }

    @Test
    void get_returnsDefaultWhenNothingRegistered() {
        assertThat(RemoteResourceFetchers.get())
                .isSameAs(HttpURLConnectionRemoteResourceFetcher.INSTANCE);
    }

    @Test
    void get_returnsRegisteredGlobalFetcher() throws Exception {
        RemoteResourceFetcher custom = uri -> "custom".getBytes();
        RemoteResourceFetchers.setGlobal(custom);

        assertThat(RemoteResourceFetchers.get()).isSameAs(custom);
        assertThat(RemoteResourceFetchers.get().fetch(URI.create("http://example.com/x")))
                .containsExactly("custom".getBytes());
    }

    @Test
    void setGlobal_nullRevertsToDefault() {
        RemoteResourceFetchers.setGlobal(uri -> new byte[0]);
        RemoteResourceFetchers.setGlobal(null);

        assertThat(RemoteResourceFetchers.get())
                .isSameAs(HttpURLConnectionRemoteResourceFetcher.INSTANCE);
    }

    @Test
    void defaultFetcher_rejectsNonHttpUrl() {
        assertThatThrownBy(() ->
                HttpURLConnectionRemoteResourceFetcher.INSTANCE.fetch(URI.create("ftp://example.com/x")))
                .isInstanceOf(RemoteResourceFetchException.class);
    }
}
