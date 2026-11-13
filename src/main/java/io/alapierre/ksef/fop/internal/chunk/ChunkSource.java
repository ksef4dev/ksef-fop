package io.alapierre.ksef.fop.internal.chunk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds the FO document of one chunk from slices of the spooled FO.
 *
 * <p>A continuation chunk starts with the document prologue (plus a continuation page master), reopens the page
 * sequence, flow, blocks, table and table body around its first row, and continues with the spooled rows. A chunk
 * that does not reach the document end closes everything after its last row.</p>
 */
final class ChunkSource {

    /** Attributes that apply to the first area of an element only; a reopened element must not repeat them. */
    private static final Set<String> FIRST_AREA_ATTRIBUTES = new HashSet<>(Arrays.asList(
            "id", "break-before", "page-break-before", "margin-top", "space-before", "keep-with-previous"));

    private final SpooledFo fo;

    ChunkSource(SpooledFo fo) {
        this.fo = fo;
    }

    InputStream open(ChunkRequest request) throws IOException {
        List<InputStream> parts = new ArrayList<>();
        long sliceStart = 0;
        if (!request.startsDocument()) {
            FoRow first = fo.rows.get(request.firstRow);
            parts.add(new ByteArrayInputStream(continuationPrologue(first, request.continuation)));
            sliceStart = first.start;
        }
        long sliceEnd = request.endsDocument() ? fo.length : fo.rows.get(request.lastRow).end;
        parts.add(new FileSlice(fo, sliceStart, sliceEnd));
        if (!request.endsDocument()) {
            parts.add(new ByteArrayInputStream(closingTags(fo.rows.get(request.lastRow).body)));
        }
        return new SequenceInputStream(Collections.enumeration(parts));
    }

    private byte[] continuationPrologue(FoRow row, Continuation continuation) throws IOException {
        List<FoElement> chain = chainFromPageSequence(row.body);
        FoElement pageSequence = chain.get(0);
        FoElement flow = chain.get(1);
        ByteArrayOutputStream prologue = new ByteArrayOutputStream();
        prologue.write(fo.read(0, fo.layoutMasterSetEnd));
        write(prologue, fo.pageMasters.continuationMaster(pageSequence.attribute("master-reference"),
                continuation.firstPageMaster, continuation.pagesBefore));
        prologue.write(fo.read(fo.layoutMasterSetEnd, fo.firstPageSequenceStart));
        write(prologue, reopenedPageSequence(pageSequence, continuation.firstPageNumber));
        prologue.write(fo.read(pageSequence.startTagEnd, flow.startTagStart));
        prologue.write(fo.read(flow.startTagStart, flow.startTagEnd));
        for (FoElement element : chain.subList(2, chain.size())) {
            write(prologue, reopenedStartTag(element));
            if (element.is("table")) {
                prologue.write(tablePrelude(element));
            }
        }
        return prologue.toByteArray();
    }

    /** Columns, header and footer of a table; the header is left out when the table omits it at breaks. */
    private byte[] tablePrelude(FoElement table) throws IOException {
        if (table.headerStart < 0 || !"true".equals(table.attribute("table-omit-header-at-break"))) {
            return fo.read(table.startTagEnd, table.firstBodyStart);
        }
        ByteArrayOutputStream prelude = new ByteArrayOutputStream();
        prelude.write(fo.read(table.startTagEnd, table.headerStart));
        prelude.write(fo.read(table.headerEnd, table.firstBodyStart));
        return prelude.toByteArray();
    }

    private static String reopenedPageSequence(FoElement pageSequence, int firstPageNumber) {
        StringBuilder tag = new StringBuilder("<fo:page-sequence");
        for (FoAttribute attribute : pageSequence.attributes) {
            if (attribute.isUnqualified("master-reference") || attribute.isUnqualified("initial-page-number")
                    || attribute.isUnqualified("id")) {
                continue;
            }
            appendAttribute(tag, attribute);
        }
        tag.append(" master-reference=\"").append(FoNames.CONTINUATION_MASTER).append('"');
        tag.append(" initial-page-number=\"").append(firstPageNumber).append('"');
        return tag.append('>').toString();
    }

    private static String reopenedStartTag(FoElement element) {
        StringBuilder tag = new StringBuilder("<fo:").append(element.localName);
        for (FoAttribute attribute : element.attributes) {
            if (!appliesToFirstAreaOnly(attribute)) {
                appendAttribute(tag, attribute);
            }
        }
        return tag.append('>').toString();
    }

    private static boolean appliesToFirstAreaOnly(FoAttribute attribute) {
        if (!attribute.uri.isEmpty()) {
            return false;
        }
        String name = attribute.localName;
        int dot = name.indexOf('.');
        return FIRST_AREA_ATTRIBUTES.contains(dot < 0 ? name : name.substring(0, dot));
    }

    private static void appendAttribute(StringBuilder tag, FoAttribute attribute) {
        String name = attribute.localName;
        if (!attribute.uri.isEmpty()) {
            if (FoNames.XML_NAMESPACE.equals(attribute.uri)) {
                name = "xml:" + name;
            } else {
                tag.append(" xmlns:ka=\"").append(CountingXmlWriter.escapeAttribute(attribute.uri)).append('"');
                name = "ka:" + name;
            }
        }
        tag.append(' ').append(name).append("=\"").append(CountingXmlWriter.escapeAttribute(attribute.value)).append('"');
    }

    private static byte[] closingTags(FoElement body) {
        List<FoElement> chain = chainFromPageSequence(body);
        StringBuilder tags = new StringBuilder();
        for (int i = chain.size() - 1; i >= 0; i--) {
            tags.append("</fo:").append(chain.get(i).localName).append('>');
        }
        return tags.append("</fo:root>").toString().getBytes(StandardCharsets.UTF_8);
    }

    /** Page sequence, flow, blocks, table and table body around a row, outermost first. */
    private static List<FoElement> chainFromPageSequence(FoElement body) {
        List<FoElement> chain = new ArrayList<>();
        for (FoElement element = body; element != null && !element.is("root"); element = element.parent) {
            chain.add(element);
        }
        Collections.reverse(chain);
        return chain;
    }

    private static void write(ByteArrayOutputStream out, String markup) throws IOException {
        out.write(markup.getBytes(StandardCharsets.UTF_8));
    }

    /** A byte range of the spooled file. */
    private static final class FileSlice extends InputStream {

        private final FileInputStream in;
        private long remaining;

        FileSlice(SpooledFo fo, long from, long to) throws IOException {
            this.in = new FileInputStream(fo.file);
            this.in.getChannel().position(from);
            this.remaining = to - from;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int b = in.read();
            if (b >= 0) {
                remaining--;
            }
            return b;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int read = in.read(buffer, offset, (int) Math.min(length, remaining));
            if (read > 0) {
                remaining -= read;
            }
            return read;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }
}
