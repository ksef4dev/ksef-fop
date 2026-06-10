package io.alapierre.ksef.fop.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import java.util.Optional;

/**
 * A single canonicalised resource root used by {@link TemplateResolver}.
 */
abstract class ResourceRoot {

    /**
     * Tries to load {@code relativePath} from this root. Returns {@code null} on a miss.
     */
    @Nullable
    abstract Source tryResolveRelative(@NotNull String relativePath) throws TransformerException;

    /**
     * Resolves a relative path to a public URI ({@code file:} or {@code http(s):}) when the
     * resource exists under this root.
     */
    abstract @NotNull Optional<String> tryResolvePublicUri(@NotNull String relativePath)
            throws TransformerException;

    boolean isHttp() {
        return false;
    }

    /**
     * Whether {@code url} lies under this root. Always {@code false} for non-HTTP roots.
     */
    boolean contains(@NotNull String url) {
        return false;
    }

    /**
     * Fetches an absolute URL when it lies under this HTTP root. Returns {@code null} when this
     * is not an HTTP root or the URL is outside the root.
     */
    @Nullable
    Source tryFetchAbsolute(@NotNull String url) throws TransformerException {
        return null;
    }

    /**
     * Resolves {@code href} against an HTTP {@code base} that lies under this root and fetches
     * the result. Returns {@code null} when not applicable or on a miss.
     */
    @Nullable
    Source tryFetchRelativeToBase(@NotNull String href, @NotNull String base) throws TransformerException {
        return null;
    }
}
