/*
 * SPDX-License-identifier: Apache-2.0
 */
package io.alapierre.ksef.fop.internal;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public final class FilesystemRoots {

    private FilesystemRoots() {
    }

    /**
     * Canonicalises and validates the given roots.
     *
     * <p>Each root is resolved via {@link Path#toRealPath(java.nio.file.LinkOption...)} and
     * must refer to an existing, accessible directory. The returned list is immutable and
     * contains the canonical paths in the same order as the input.</p>
     *
     * @param roots ordered list of caller-supplied roots; must not contain {@code null} entries
     * @return immutable list of canonical, directory-typed paths
     * @throws IOException              if a root does not exist, is not accessible or is not a
     *                                  directory
     * @throws IllegalArgumentException if any entry in {@code roots} is {@code null}
     */
    public static @NotNull List<Path> canonicalize(@NotNull List<Path> roots) throws IOException {
        List<Path> result = new ArrayList<>(roots.size());
        for (Path root : roots) {
            if (root == null) {
                throw new IllegalArgumentException("Filesystem root must not be null");
            }
            Path canonical = root.toRealPath();
            if (!Files.isDirectory(canonical)) {
                throw new NotDirectoryException(root.toString());
            }
            result.add(canonical);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Safely resolves {@code relativePath} under {@code canonicalRoot}.
     *
     * <p>Returns the canonical path only when <em>all</em> of the following hold:</p>
     * <ol>
     *     <li>The candidate exists after {@link Path#toRealPath(java.nio.file.LinkOption...)}
     *         (so symlinks are followed).</li>
     *     <li>The real path still starts with {@code canonicalRoot}, i.e. it has not escaped
     *         the root via {@code ..} traversal or symlinks.</li>
     *     <li>The real path refers to a regular file (directories and special files are
     *         rejected).</li>
     * </ol>
     * <p>Any I/O error, a miss, or a containment violation results in {@link Optional#empty()}.
     * This method does <em>not</em> throw: callers that need to distinguish an attacker-like
     * input from a plain miss should layer that on top.</p>
     *
     * @param canonicalRoot a root previously returned from {@link #canonicalize(List)}
     * @param relativePath  resource name to resolve relative to the root
     * @return canonical path to the resolved file, or empty if no safe match is found
     */
    public static @NotNull Optional<Path> resolveFileWithin(@NotNull Path canonicalRoot,
                                                            @NotNull String relativePath) {
        Path candidate = canonicalRoot.resolve(relativePath);
        if (!Files.exists(candidate)) {
            return Optional.empty();
        }
        try {
            Path real = candidate.toRealPath();
            if (!real.startsWith(canonicalRoot) || !Files.isRegularFile(real)) {
                return Optional.empty();
            }
            return Optional.of(real);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
