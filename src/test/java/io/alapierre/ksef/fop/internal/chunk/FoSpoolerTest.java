package io.alapierre.ksef.fop.internal.chunk;

import io.alapierre.ksef.fop.internal.XmlFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoSpoolerTest {

    private static final String HEAD = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">"
            + "<fo:layout-master-set><fo:simple-page-master master-name=\"A4\"><fo:region-body/>"
            + "</fo:simple-page-master></fo:layout-master-set>"
            + "<fo:page-sequence master-reference=\"A4\"><fo:flow flow-name=\"xsl-region-body\">";
    private static final String TAIL = "</fo:flow></fo:page-sequence></fo:root>";

    @TempDir
    Path tempDir;

    @Test
    void marksRowsOfTablesThatCanBeReopened() throws Exception {
        SpooledFo fo = spool(HEAD + "<fo:block>" + table(3) + "</fo:block>" + TAIL);

        assertNull(fo.notChunkableReason);
        assertEquals(3, fo.rows.size());
        assertFalse(fo.rows.get(0).followsCandidateRow);
        assertTrue(fo.rows.get(1).followsCandidateRow);
        String spooled = new String(Files.readAllBytes(fo.file.toPath()), StandardCharsets.UTF_8);
        assertTrue(spooled.contains("<fo:block id=\"ksef-chunk-row-2\">"), spooled);
        assertTrue(spooled.substring((int) fo.rows.get(1).start, (int) fo.rows.get(1).end).startsWith("<fo:table-row>"));
    }

    @Test
    void ignoresTablesInsideBorderedBlocksAndCells() throws Exception {
        String nested = "<fo:table><fo:table-column/><fo:table-body><fo:table-row><fo:table-cell>"
                + table(2) + "</fo:table-cell></fo:table-row></fo:table-body></fo:table>";
        SpooledFo fo = spool(HEAD + "<fo:block border=\"solid 1pt\">" + table(2) + "</fo:block>" + nested + TAIL);

        assertEquals(1, fo.rows.size(), "only the row of the outer table without borders around it");
    }

    @Test
    void skipsRowsCoveredByRowSpans() throws Exception {
        String rows = "<fo:table-row><fo:table-cell number-rows-spanned=\"2\"><fo:block>a</fo:block></fo:table-cell>"
                + "<fo:table-cell><fo:block>b</fo:block></fo:table-cell></fo:table-row>"
                + "<fo:table-row><fo:table-cell><fo:block>c</fo:block></fo:table-cell></fo:table-row>"
                + "<fo:table-row><fo:table-cell><fo:block>d</fo:block></fo:table-cell>"
                + "<fo:table-cell><fo:block>e</fo:block></fo:table-cell></fo:table-row>";
        SpooledFo fo = spool(HEAD + "<fo:table><fo:table-column/><fo:table-column/><fo:table-body>" + rows
                + "</fo:table-body></fo:table>" + TAIL);

        assertEquals(2, fo.rows.size());
        assertFalse(fo.rows.get(1).followsCandidateRow, "the row after the spanned one does not follow a candidate");
    }

    @Test
    void reportsReferencesAcrossPages() throws Exception {
        SpooledFo fo = spool(HEAD + "<fo:block><fo:page-number-citation ref-id=\"x\"/></fo:block>" + table(2) + TAIL);

        assertEquals("document uses fo:page-number-citation", fo.notChunkableReason);
    }

    private SpooledFo spool(String fo) throws Exception {
        File file = tempDir.resolve("test.fo").toFile();
        FoSpooler spooler = new FoSpooler(file);
        XMLReader reader = XmlFactories.SAX_PARSER_FACTORY.newSAXParser().getXMLReader();
        reader.setContentHandler(spooler);
        reader.parse(new InputSource(new StringReader(fo)));
        return spooler.finish();
    }

    private static String table(int rows) {
        StringBuilder table = new StringBuilder("<fo:table><fo:table-column/><fo:table-body>");
        for (int i = 0; i < rows; i++) {
            table.append("<fo:table-row><fo:table-cell><fo:block>").append(i)
                    .append("</fo:block></fo:table-cell></fo:table-row>");
        }
        return table.append("</fo:table-body></fo:table>").toString();
    }
}
