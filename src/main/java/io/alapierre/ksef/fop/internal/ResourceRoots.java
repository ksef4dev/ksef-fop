package io.alapierre.ksef.fop.internal;

import io.alapierre.ksef.fop.http.RemoteResourceFetcher;
import org.jetbrains.annotations.NotNull;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Canonicalises {@link URI} resource roots (filesystem directories and HTTP(S) base URLs).
 */
public final class ResourceRoots {

    private ResourceRoots() {
    }

    public static boolean isHttp(@NotNull URI root) {
        String scheme = root.getScheme();
        if (scheme == null) {
            return false;
        }
        String lower = scheme.toLowerCase(Locale.ROOT);
        return "http".equals(lower) || "https".equals(lower);
    }

    public static boolean isFilesystem(@NotNull URI root) {
        String scheme = root.getScheme();
        return scheme == null || "file".equalsIgnoreCase(scheme);
    }

  /**
   * Canonicalises resource roots using the supplied HTTP client for remote roots.
   *
   * @param fetcher HTTP client for {@code http(s):} roots; must not be {@code null}
   */
    public static @NotNull List<ResourceRoot> canonicalize(
            @NotNull List<URI> roots,
            @NotNull RemoteResourceFetcher fetcher
    ) throws IOException, TransformerException {
        List<ResourceRoot> result = new ArrayList<>(roots.size());
        for (URI root : roots) {
            if (root == null) {
                throw new IllegalArgumentException("Resource root must not be null");
            }
            if (isHttp(root)) {
                result.add(UrlResourceRoot.canonicalize(root, fetcher));
            } else if (isFilesystem(root)) {
                Path canonical = FilesystemRoots.canonicalize(
                        Collections.singletonList(toFilesystemPath(root))).get(0);
                result.add(new PathResourceRoot(canonical));
            } else {
                throw new TransformerException(
                        "Resource root must be a file: directory or http(s) URL: " + root);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private static @NotNull Path toFilesystemPath(@NotNull URI root) {
        if ("file".equalsIgnoreCase(root.getScheme())) {
            return Paths.get(root);
        }
        return Paths.get(root.getPath());
    }
}
