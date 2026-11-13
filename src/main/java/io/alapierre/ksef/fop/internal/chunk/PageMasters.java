package io.alapierre.ksef.fop.internal.chunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Page masters of the spooled FO document, used to build the page-sequence-master of a continuation chunk.
 *
 * <p>A continuation chunk reopens a page sequence in the middle. Its first page must use the master FOP chose for
 * that page in the previous chunk, and the following pages the masters the original sequence would use from there
 * on. Masters with {@code page-position="last"} or {@code "only"} are not supported: FOP lays out the last page of
 * every chunk as a "last" page, so the master seen in a chunk would differ from the one in the whole document.</p>
 */
final class PageMasters {

    private static final int UNBOUNDED = -1;

    private final Set<String> simpleMasters = new HashSet<>();
    private final Map<String, SequenceMaster> sequenceMasters = new HashMap<>();
    private SequenceMaster current;
    private Alternatives currentAlternatives;

    // ---- building while the layout-master-set is spooled ----

    void addSimpleMaster(String name) {
        simpleMasters.add(name);
    }

    void startSequenceMaster(String name) {
        current = new SequenceMaster();
        sequenceMasters.put(name, current);
    }

    void endSequenceMaster() {
        current = null;
    }

    void addSingleReference(String masterReference) {
        if (current != null) {
            current.specifiers.add(new Reference(masterReference, 1, false));
        }
    }

    void addRepeatableReference(String masterReference, String maximumRepeats) {
        if (current != null) {
            current.specifiers.add(new Reference(masterReference, parseRepeats(maximumRepeats), true));
        }
    }

    void startAlternatives(String maximumRepeats) {
        if (current != null) {
            currentAlternatives = new Alternatives(parseRepeats(maximumRepeats));
            current.specifiers.add(currentAlternatives);
        }
    }

    void endAlternatives() {
        currentAlternatives = null;
    }

    void addConditional(Map<String, String> attributes) {
        if (currentAlternatives == null) {
            return;
        }
        String pagePosition = attributes.get("page-position");
        if ("last".equals(pagePosition) || "only".equals(pagePosition)) {
            current.supported = false;
        }
        currentAlternatives.conditionals.add(new LinkedHashMap<>(attributes));
    }

    // ---- queries ----

    /** Whether a page sequence using {@code masterReference} can be continued in another chunk. */
    boolean supports(String masterReference) {
        if (simpleMasters.contains(masterReference)) {
            return true;
        }
        SequenceMaster master = sequenceMasters.get(masterReference);
        return master != null && master.supported;
    }

    /**
     * Page-sequence-master for a page sequence (using {@code masterReference}) continued at a page that used
     * {@code firstPageMaster} and was preceded by {@code pagesBefore} pages of the same sequence.
     */
    String continuationMaster(String masterReference, String firstPageMaster, int pagesBefore) {
        StringBuilder xml = new StringBuilder();
        xml.append("<fo:page-sequence-master master-name=\"").append(FoNames.CONTINUATION_MASTER).append("\">");
        xml.append("<fo:single-page-master-reference master-reference=\"")
                .append(CountingXmlWriter.escapeAttribute(firstPageMaster)).append("\"/>");
        for (Specifier specifier : remainingSpecifiers(masterReference, pagesBefore + 1)) {
            specifier.appendTo(xml);
        }
        return xml.append("</fo:page-sequence-master>").toString();
    }

    private List<Specifier> remainingSpecifiers(String masterReference, int pagesToSkip) {
        List<Specifier> specifiers = simpleMasters.contains(masterReference)
                ? Collections.<Specifier>singletonList(new Reference(masterReference, UNBOUNDED, true))
                : sequenceMasters.get(masterReference).specifiers;
        List<Specifier> remaining = new ArrayList<>();
        int toSkip = pagesToSkip;
        for (Specifier specifier : specifiers) {
            if (toSkip == 0 || specifier.maximumRepeats == UNBOUNDED) {
                remaining.add(specifier);
                toSkip = 0;
            } else if (toSkip >= specifier.maximumRepeats) {
                toSkip -= specifier.maximumRepeats;
            } else {
                remaining.add(specifier.withMaximumRepeats(specifier.maximumRepeats - toSkip));
                toSkip = 0;
            }
        }
        return remaining;
    }

    private static int parseRepeats(String value) {
        if (value == null || value.trim().isEmpty() || "no-limit".equals(value.trim())) {
            return UNBOUNDED;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return UNBOUNDED;
        }
    }

    private static final class SequenceMaster {
        final List<Specifier> specifiers = new ArrayList<>();
        boolean supported = true;
    }

    private abstract static class Specifier {
        final int maximumRepeats;

        Specifier(int maximumRepeats) {
            this.maximumRepeats = maximumRepeats;
        }

        abstract Specifier withMaximumRepeats(int repeats);

        abstract void appendTo(StringBuilder xml);

        void appendMaximumRepeats(StringBuilder xml) {
            if (maximumRepeats != UNBOUNDED) {
                xml.append(" maximum-repeats=\"").append(maximumRepeats).append('"');
            }
        }
    }

    private static final class Reference extends Specifier {
        final String masterReference;
        final boolean repeatable;

        Reference(String masterReference, int maximumRepeats, boolean repeatable) {
            super(maximumRepeats);
            this.masterReference = masterReference;
            this.repeatable = repeatable;
        }

        @Override
        Specifier withMaximumRepeats(int repeats) {
            return new Reference(masterReference, repeats, true);
        }

        @Override
        void appendTo(StringBuilder xml) {
            xml.append(repeatable ? "<fo:repeatable-page-master-reference" : "<fo:single-page-master-reference");
            xml.append(" master-reference=\"").append(CountingXmlWriter.escapeAttribute(masterReference)).append('"');
            if (repeatable) {
                appendMaximumRepeats(xml);
            }
            xml.append("/>");
        }
    }

    private static final class Alternatives extends Specifier {
        final List<Map<String, String>> conditionals;

        Alternatives(int maximumRepeats) {
            this(maximumRepeats, new ArrayList<Map<String, String>>());
        }

        private Alternatives(int maximumRepeats, List<Map<String, String>> conditionals) {
            super(maximumRepeats);
            this.conditionals = conditionals;
        }

        @Override
        Specifier withMaximumRepeats(int repeats) {
            return new Alternatives(repeats, conditionals);
        }

        @Override
        void appendTo(StringBuilder xml) {
            xml.append("<fo:repeatable-page-master-alternatives");
            appendMaximumRepeats(xml);
            xml.append('>');
            for (Map<String, String> conditional : conditionals) {
                xml.append("<fo:conditional-page-master-reference");
                for (Map.Entry<String, String> attribute : conditional.entrySet()) {
                    xml.append(' ').append(attribute.getKey()).append("=\"")
                            .append(CountingXmlWriter.escapeAttribute(attribute.getValue())).append('"');
                }
                xml.append("/>");
            }
            xml.append("</fo:repeatable-page-master-alternatives>");
        }
    }
}
