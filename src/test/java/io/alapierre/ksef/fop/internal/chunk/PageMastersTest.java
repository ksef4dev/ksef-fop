package io.alapierre.ksef.fop.internal.chunk;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageMastersTest {

    @Test
    void continuesSimpleMasterWithTheSameMaster() {
        PageMasters masters = new PageMasters();
        masters.addSimpleMaster("A4");

        assertTrue(masters.supports("A4"));
        assertEquals("<fo:page-sequence-master master-name=\"ksef-chunk-continuation\">"
                        + "<fo:single-page-master-reference master-reference=\"A4\"/>"
                        + "<fo:repeatable-page-master-reference master-reference=\"A4\"/>"
                        + "</fo:page-sequence-master>",
                masters.continuationMaster("A4", "A4", 7));
    }

    @Test
    void skipsSpecifiersUsedByEarlierPages() {
        PageMasters masters = new PageMasters();
        masters.addSimpleMaster("cover");
        masters.addSimpleMaster("body");
        masters.startSequenceMaster("invoice");
        masters.addSingleReference("cover");
        masters.addRepeatableReference("body", "3");
        masters.addRepeatableReference("cover", null);
        masters.endSequenceMaster();

        assertEquals("<fo:page-sequence-master master-name=\"ksef-chunk-continuation\">"
                        + "<fo:single-page-master-reference master-reference=\"body\"/>"
                        + "<fo:repeatable-page-master-reference master-reference=\"body\" maximum-repeats=\"2\"/>"
                        + "<fo:repeatable-page-master-reference master-reference=\"cover\"/>"
                        + "</fo:page-sequence-master>",
                masters.continuationMaster("invoice", "body", 1));
    }

    @Test
    void keepsAlternativesAfterTheFirstPage() {
        PageMasters masters = alternatives("rest");

        assertTrue(masters.supports("invoice"));
        assertEquals("<fo:page-sequence-master master-name=\"ksef-chunk-continuation\">"
                        + "<fo:single-page-master-reference master-reference=\"rest\"/>"
                        + "<fo:repeatable-page-master-alternatives>"
                        + "<fo:conditional-page-master-reference page-position=\"first\" master-reference=\"first\"/>"
                        + "<fo:conditional-page-master-reference page-position=\"rest\" master-reference=\"rest\"/>"
                        + "</fo:repeatable-page-master-alternatives>"
                        + "</fo:page-sequence-master>",
                masters.continuationMaster("invoice", "rest", 4));
    }

    @Test
    void doesNotSupportLastPageMasters() {
        assertFalse(alternatives("last").supports("invoice"));
        assertFalse(new PageMasters().supports("unknown"));
    }

    private static PageMasters alternatives(String secondPosition) {
        PageMasters masters = new PageMasters();
        masters.addSimpleMaster("first");
        masters.addSimpleMaster("rest");
        masters.startSequenceMaster("invoice");
        masters.startAlternatives(null);
        masters.addConditional(conditional("first", "first"));
        masters.addConditional(conditional(secondPosition, "rest"));
        masters.endAlternatives();
        masters.endSequenceMaster();
        return masters;
    }

    private static Map<String, String> conditional(String pagePosition, String masterReference) {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("page-position", pagePosition);
        attributes.put("master-reference", masterReference);
        return attributes;
    }
}
