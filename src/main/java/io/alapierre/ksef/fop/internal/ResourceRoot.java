package io.alapierre.ksef.fop.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A single canonicalised resource root — either a filesystem directory or an HTTP(S) base URL.
 */
final class ResourceRoot {

    private final Path filesystemPath;
    private final String httpBaseUrl;

    private ResourceRoot(@Nullable Path filesystemPath, @Nullable String httpBaseUrl) {
        this.filesystemPath = filesystemPath;
        this.httpBaseUrl = httpBaseUrl;
    }

    static @NotNull ResourceRoot filesystem(@NotNull Path canonicalPath) {
        return new ResourceRoot(canonicalPath, null);
    }

    static @NotNull ResourceRoot http(@NotNull String canonicalBaseUrl) {
        return new ResourceRoot(null, canonicalBaseUrl);
    }

    boolean isHttp() {
        return httpBaseUrl != null;
    }

    @NotNull
    Path getFilesystemPath() {
        if (filesystemPath == null) {
            throw new IllegalStateException("Not a filesystem root: " + httpBaseUrl);
        }
        return filesystemPath;
    }

    @NotNull
    String getHttpBaseUrl() {
        if (httpBaseUrl == null) {
            throw new IllegalStateException("Not an HTTP root: " + filesystemPath);
        }
        return httpBaseUrl;
    }

    boolean isUnder(@NotNull String url) {
        return isHttp() && TemplateResolver.isUnderRemoteBase(url, httpBaseUrl);
    }

    @NotNull
    String resolveUrl(@NotNull String relativePath) {
        String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return getHttpBaseUrl() + "/" + path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceRoot)) return false;
        ResourceRoot that = (ResourceRoot) o;
        return Objects.equals(filesystemPath, that.filesystemPath)
                && Objects.equals(httpBaseUrl, that.httpBaseUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filesystemPath, httpBaseUrl);
    }

    @Override
    public String toString() {
        return isHttp() ? "ResourceRoot{http=" + httpBaseUrl + "}" : "ResourceRoot{file=" + filesystemPath + "}";
    }
}
