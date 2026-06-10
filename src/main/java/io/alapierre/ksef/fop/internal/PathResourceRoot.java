package io.alapierre.ksef.fop.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Filesystem ({@code file:}) resource root.
 */
final class PathResourceRoot extends ResourceRoot {

    private static final String VFS_PREFIX = "vfs:///";

    private final Path root;

    PathResourceRoot(@NotNull Path canonicalRoot) {
        this.root = Objects.requireNonNull(canonicalRoot, "canonicalRoot");
    }

    @NotNull
    Path getRoot() {
        return root;
    }

    @Override
    @Nullable
    Source tryResolveRelative(@NotNull String relativePath) throws TransformerException {
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

    @Override
    @NotNull
    Optional<String> tryResolvePublicUri(@NotNull String relativePath) throws TransformerException {
        Optional<Path> resolved = FilesystemRoots.resolveFileWithin(root, relativePath);
        return resolved.map(path -> path.toUri().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PathResourceRoot)) return false;
        PathResourceRoot that = (PathResourceRoot) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }

    @Override
    public String toString() {
        return "PathResourceRoot{" + root + "}";
    }
}
