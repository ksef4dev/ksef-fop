package io.alapierre.ksef.fop.internal.chunk;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Temporary files of one chunked render, all deleted on {@link #close()}.
 */
@Slf4j
final class TemporaryFiles implements Closeable {

    private final List<File> files = new ArrayList<>();

    File create(String prefix, String suffix) throws IOException {
        File file = Files.createTempFile(prefix, suffix).toFile();
        files.add(file);
        return file;
    }

    @Override
    public void close() {
        for (File file : files) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                log.warn("Could not delete temporary file {}", file, e);
            }
        }
        files.clear();
    }
}
