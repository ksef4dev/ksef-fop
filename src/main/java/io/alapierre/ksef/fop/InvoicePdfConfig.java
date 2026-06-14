package io.alapierre.ksef.fop;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The {@code InvoicePdfConfig} class represents the configuration settings for generating PDF invoices.
 */
@Data
@Builder
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
     * When {@code true} (default), compiled XSLT {@link javax.xml.transform.Templates} are cached in memory.
     * Set to {@code false} in development when templates are updated at runtime (e.g. via HTTP serve).
     */
    @Builder.Default
    private boolean templateCacheEnabled = true;

    /**
     * Backward-compatible constructor; template cache remains enabled ({@code true}).
     */
    public InvoicePdfConfig(boolean showFooter, boolean useExtendedPriceDecimalPlaces) {
        this(showFooter, useExtendedPriceDecimalPlaces, true);
    }

    public InvoicePdfConfig(boolean showFooter, boolean useExtendedPriceDecimalPlaces, boolean templateCacheEnabled) {
        this.showFooter = showFooter;
        this.useExtendedPriceDecimalPlaces = useExtendedPriceDecimalPlaces;
        this.templateCacheEnabled = templateCacheEnabled;
    }
}
