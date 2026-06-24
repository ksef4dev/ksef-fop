package io.alapierre.ksef.fop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The {@code InvoicePdfConfig} class represents the configuration settings for generating PDF invoices.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoicePdfConfig {

    /**
     * Indicates whether the footer should be displayed on the PDF invoice.
     * If {@code true}, the footer will be shown on the generated PDF invoice.
     * If {@code false}, the footer will not be included. (default)
     */
    @Builder.Default
    private boolean showFooter = false;
    
    /**
     * Indicates whether to use extended decimal places (4 places) for unit prices.
     * If {@code true}, unit prices (P_9A, P_9B) will be displayed with 4 decimal places.
     * If {@code false}, unit prices will be displayed with 2 decimal places. (default)
     */
    @Builder.Default
    private boolean useExtendedPriceDecimalPlaces = false;

    /**
     * Number of isolated FOP renderers available to a single {@link PdfGenerator} instance.
     * The default is {@code 1}, which serializes rendering for maximum safety. Values lower
     * than {@code 1} are treated as {@code 1}.
     */
    @Builder.Default
    private int rendererPoolSize = 1;
}
