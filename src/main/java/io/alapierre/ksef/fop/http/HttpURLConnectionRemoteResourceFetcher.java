package io.alapierre.ksef.fop.http;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Default {@link RemoteResourceFetcher} using JDK {@link HttpURLConnection}.
 */
public final class HttpURLConnectionRemoteResourceFetcher implements RemoteResourceFetcher {

    public static final HttpURLConnectionRemoteResourceFetcher INSTANCE =
            new HttpURLConnectionRemoteResourceFetcher();

    public static final int REMOTE_TIMEOUT_MS = 10_000;
    public static final int MAX_REMOTE_BYTES = 16 * 1024 * 1024;

    private static final String ACCEPT_HEADER =
            "application/xslt+xml, text/xml, application/xml, */*";

    private HttpURLConnectionRemoteResourceFetcher() {
    }

    @Override
    public byte @NotNull [] fetch(@NotNull URI url) throws RemoteResourceFetchException {
        String urlString = url.toString();
        String scheme = url.getScheme();
        if (scheme == null
                || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
            throw new RemoteResourceFetchException(
                    url,
                    "Only http and https URLs are supported: " + urlString,
                    null);
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setConnectTimeout(REMOTE_TIMEOUT_MS);
            connection.setReadTimeout(REMOTE_TIMEOUT_MS);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", ACCEPT_HEADER);
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new RemoteResourceFetchException(url, status);
            }

            return readFully(connection.getInputStream());
        } catch (RemoteResourceFetchException e) {
            throw e;
        } catch (IOException e) {
            throw new RemoteResourceFetchException(
                    url,
                    "Failed to fetch remote resource (server may be down): " + urlString
                            + " — " + e.getMessage(),
                    e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static byte[] readFully(InputStream in) throws IOException, RemoteResourceFetchException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        while ((n = in.read(chunk)) != -1) {
            buf.write(chunk, 0, n);
            if (buf.size() > MAX_REMOTE_BYTES) {
                throw new IOException("Remote resource exceeds maximum allowed size of "
                        + MAX_REMOTE_BYTES + " bytes");
            }
        }
        return buf.toByteArray();
    }
}
