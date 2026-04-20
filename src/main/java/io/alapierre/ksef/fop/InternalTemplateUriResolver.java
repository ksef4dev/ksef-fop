package io.alapierre.ksef.fop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal URI resolver for invoice templates.
 * Lookup order:
 * 1) configured filesystem roots (in order)
 * 2) classpath resources
 */
final class InternalTemplateUriResolver implements URIResolver {

    private static final String CLASSPATH_SCHEME = "classpath:";

    private final ClassLoader classLoader;
    private final List<Path> roots;

    InternalTemplateUriResolver(@NotNull ClassLoader classLoader, @Nullable List<Path> roots) {
        this.classLoader = classLoader;
        this.roots = normalizeRoots(roots);
    }

    StreamSource entryTemplateSource(@NotNull String templatePath) {
        return new StreamSource(templatePath);
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String effectiveHref = href == null ? "" : href;

        if (base != null && !base.trim().isEmpty()) {
            Source sourceFromBase = resolveAgainstBase(effectiveHref, base);
            if (sourceFromBase != null) {
                return sourceFromBase;
            }
        }

        Source sourceFromRoots = resolveFromRoots(effectiveHref);
        if (sourceFromRoots != null) {
            return sourceFromRoots;
        }

        Source sourceFromClasspath = resolveFromClasspath(effectiveHref);
        if (sourceFromClasspath != null) {
            return sourceFromClasspath;
        }

        throw new TransformerException("Cannot resolve template resource: href=" + href + ", base=" + base);
    }

    private @Nullable Source resolveAgainstBase(@NotNull String href, @NotNull String base) throws TransformerException {
        if (base.startsWith(CLASSPATH_SCHEME)) {
            return resolveAgainstClasspathBase(href, base);
        }
        return tryResolveAgainstFileBase(href, base);
    }

    private @Nullable Source resolveAgainstClasspathBase(@NotNull String href, @NotNull String base) throws TransformerException {
        String basePath = base.substring(CLASSPATH_SCHEME.length());
        String combined = resolveClasspathRelative(basePath, href);
        Source fromRoots = resolveFromRoots(combined);
        return fromRoots != null ? fromRoots : resolveFromClasspath(combined);
    }

    /**
     * Resolves {@code href} relative to a {@code file:} base. Other schemes are ignored so the
     * outer {@link #resolve} flow can fall back to roots/classpath.
     */
    private @Nullable Source tryResolveAgainstFileBase(@NotNull String href, @NotNull String base) throws TransformerException {
        try {
            URI baseUri = URI.create(base);
            if (!"file".equalsIgnoreCase(baseUri.getScheme())) {
                return null;
            }
            Path resolved = resolveRelativeToFile(Paths.get(baseUri), href);
            return resolveFromResolvedFilePath(resolved);
        } catch (IllegalArgumentException e) {
            throw new TransformerException("Invalid template base URI: " + base, e);
        }
    }

    private @Nullable Source resolveFromResolvedFilePath(@NotNull Path resolved) throws TransformerException {
        Source fromRoots = resolvePathWithinRoots(resolved);
        if (fromRoots != null) {
            return fromRoots;
        }
        String classpathCandidate = relativePathFromAnyRoot(resolved);
        return classpathCandidate != null ? resolveFromClasspath(classpathCandidate) : null;
    }

    private static @NotNull Path resolveRelativeToFile(@NotNull Path baseFile, @NotNull String href) {
        Path parent = baseFile.getParent();
        return parent == null ? Paths.get(href) : parent.resolve(href).normalize();
    }

    private @Nullable Source resolveFromRoots(@NotNull String href) throws TransformerException {
        if (href.trim().isEmpty() || looksLikeScheme(href)) {
            return null;
        }
        Path relative = Paths.get(href).normalize();
        if (relative.isAbsolute()) {
            return resolvePathWithinRoots(relative);
        }
        for (Path root : roots) {
            Path candidate = root.resolve(relative).normalize();
            Source source = resolvePathWithinRoots(candidate);
            if (source != null) {
                return source;
            }
        }
        return null;
    }

    private @Nullable Source resolvePathWithinRoots(@NotNull Path candidate) throws TransformerException {
        try {
            Path canonicalCandidate = candidate.toAbsolutePath().normalize();
            for (Path root : roots) {
                if (!canonicalCandidate.startsWith(root)) {
                    continue;
                }
                if (Files.isRegularFile(canonicalCandidate)) {
                    return fileSource(canonicalCandidate);
                }
            }
            return null;
        } catch (Exception e) {
            throw new TransformerException("Failed to resolve template file candidate: " + candidate, e);
        }
    }

    private @Nullable Source resolveFromClasspath(@NotNull String href) throws TransformerException {
        if (href.trim().isEmpty()) {
            return null;
        }
        String normalized = stripLeadingSlash(href);
        InputStream is = classLoader.getResourceAsStream(normalized);
        if (is == null) {
            return null;
        }
        StreamSource source = new StreamSource(is);
        source.setSystemId(CLASSPATH_SCHEME + normalized);
        return source;
    }

    private static @NotNull StreamSource fileSource(@NotNull Path file) throws IOException {
        StreamSource source = new StreamSource(Files.newInputStream(file));
        source.setSystemId(file.toUri().toString());
        return source;
    }

    private @Nullable String relativePathFromAnyRoot(@NotNull Path file) {
        Path normalizedFile = file.toAbsolutePath().normalize();
        for (Path root : roots) {
            if (normalizedFile.startsWith(root)) {
                return root.relativize(normalizedFile).toString().replace('\\', '/');
            }
        }
        return null;
    }

    private static @NotNull String resolveClasspathRelative(@NotNull String basePath, @NotNull String href) {
        Path base = Paths.get(stripLeadingSlash(basePath));
        Path resolved = base.getParent() == null ? Paths.get(href) : base.getParent().resolve(href);
        return resolved.normalize().toString().replace('\\', '/');
    }

    private static boolean looksLikeScheme(@NotNull String value) {
        int idx = value.indexOf(':');
        if (idx <= 0) {
            return false;
        }
        int slashIdx = value.indexOf('/');
        return slashIdx == -1 || idx < slashIdx;
    }

    private static @NotNull List<Path> normalizeRoots(@Nullable List<Path> roots) {
        List<Path> normalized = new ArrayList<>();
        if (roots == null) {
            return normalized;
        }
        for (Path root : roots) {
            if (root == null) {
                continue;
            }
            normalized.add(root.toAbsolutePath().normalize());
        }
        return normalized;
    }

    private static @NotNull String stripLeadingSlash(@NotNull String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
