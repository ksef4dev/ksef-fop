package io.alapierre.ksef.fop.internal.chunk;

import net.sf.saxon.jaxp.TemplatesImpl;
import net.sf.saxon.s9api.QName;

import javax.xml.transform.Templates;

/**
 * Parameters and ids a template must support to be rendered in chunks.
 *
 * <ul>
 *   <li>{@value #MODE_PARAM}: {@code ''} renders the whole invoice, {@code head} the invoice start with rows
 *       {@value #FROM_PARAM}..{@value #TO_PARAM}, {@code middle} only those rows, {@code tail} the rows from
 *       {@value #FROM_PARAM} and the rest of the invoice;</li>
 *   <li>{@value #FROM_PARAM}, {@value #TO_PARAM}: 1-based position range, {@code 0} as the upper bound means
 *       "until the last row";</li>
 *   <li>{@value #INITIAL_PAGE_PARAM}: page number of the first page of a continuation chunk;</li>
 *   <li>every rendered position carries the id {@value #ROW_ID_PREFIX}{@code N} (N = position number).</li>
 * </ul>
 */
public final class ChunkTemplateContract {

    public static final String MODE_PARAM = "chunkMode";
    public static final String FROM_PARAM = "chunkFrom";
    public static final String TO_PARAM = "chunkTo";
    public static final String INITIAL_PAGE_PARAM = "chunkInitialPage";
    public static final String ROW_ID_PREFIX = "ksefrow-";

    private static final QName MODE_PARAM_NAME = new QName(MODE_PARAM);

    private ChunkTemplateContract() {
    }

    /** Returns {@code true} when the compiled template declares the {@value #MODE_PARAM} parameter. */
    public static boolean isSupportedBy(Templates templates) {
        return templates instanceof TemplatesImpl
                && ((TemplatesImpl) templates).getImplementation().getGlobalParameters().containsKey(MODE_PARAM_NAME);
    }
}
