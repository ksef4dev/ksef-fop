package io.alapierre.ksef.fop.internal.chunk;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Writes UTF-8 XML markup to a file and knows the byte offset of everything it wrote, so the file can later be
 * read back in slices.
 */
final class CountingXmlWriter implements Closeable {

    private final OutputStream out;
    private long position;

    CountingXmlWriter(File file) throws IOException {
        this.out = new BufferedOutputStream(new FileOutputStream(file), 64 * 1024);
    }

    long position() {
        return position;
    }

    void write(String markup) throws IOException {
        byte[] bytes = markup.getBytes(StandardCharsets.UTF_8);
        out.write(bytes);
        position += bytes.length;
    }

    void writeText(char[] ch, int start, int length) throws IOException {
        StringBuilder sb = new StringBuilder(length + 16);
        for (int i = start; i < start + length; i++) {
            char c = ch[i];
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '\r': sb.append("&#13;"); break;
                default: sb.append(c);
            }
        }
        write(sb.toString());
    }

    static String escapeAttribute(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\t': sb.append("&#9;"); break;
                case '\n': sb.append("&#10;"); break;
                case '\r': sb.append("&#13;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
