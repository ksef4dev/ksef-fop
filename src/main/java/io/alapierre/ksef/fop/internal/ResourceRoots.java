package io.alapierre.ksef.fop.internal;

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
import java.util.Objects;

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

    public static @NotNull List<ResourceRoot> canonicalize(@NotNull List<URI> roots)
            throws IOException, TransformerException {
        List<ResourceRoot> result = new ArrayList<>(roots.size());
        for (URI root : roots) {
            if (root == null) {
                throw new IllegalArgumentException("Resource root must not be null");
            }
            if (isHttp(root)) {
                result.add(ResourceRoot.http(TemplateResolver.canonicalizeHttpBaseUrl(root.toString())));
            } else if (isFilesystem(root)) {
                Path canonical = FilesystemRoots.canonicalize(
                        Collections.singletonList(toFilesystemPath(root))).get(0);
                result.add(ResourceRoot.filesystem(canonical));
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
