package io.alapierre.ksef.fop;

/**
 * The {@code InvoicePdfConfig} class represents the configuration settings for generating PDF invoices.
 */
public class InvoicePdfConfig {

    /**
     * Indicates whether the footer should be displayed on the PDF invoice.
     * If {@code true}, the footer will be shown on the generated PDF invoice.
     * If {@code false}, the footer will not be included. (default)
     */
    private boolean showFooter = false;
    
    /**
     * Indicates whether to use extended decimal places (4 places) for unit prices.
     * If {@code true}, unit prices (P_9A, P_9B) will be displayed with 4 decimal places.
     * If {@code false}, unit prices will be displayed with 2 decimal places. (default)
     */
    private boolean useExtendedPriceDecimalPlaces = false;

    public InvoicePdfConfig() {
    }

    public InvoicePdfConfig(boolean showFooter, boolean useExtendedPriceDecimalPlaces) {
        this.showFooter = showFooter;
        this.useExtendedPriceDecimalPlaces = useExtendedPriceDecimalPlaces;
    }

    public boolean isShowFooter() {
        return showFooter;
    }

    public void setShowFooter(boolean showFooter) {
        this.showFooter = showFooter;
    }

    public boolean isUseExtendedPriceDecimalPlaces() {
        return useExtendedPriceDecimalPlaces;
    }

    public void setUseExtendedPriceDecimalPlaces(boolean useExtendedPriceDecimalPlaces) {
        this.useExtendedPriceDecimalPlaces = useExtendedPriceDecimalPlaces;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof InvoicePdfConfig) {
            InvoicePdfConfig other = (InvoicePdfConfig) o;
            return other.canEqual(this)
                    && showFooter == other.showFooter
                    && useExtendedPriceDecimalPlaces == other.useExtendedPriceDecimalPlaces;
        }
        return false;
    }

    protected boolean canEqual(Object other) {
        return other instanceof InvoicePdfConfig;
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(showFooter);
        result = 31 * result + Boolean.hashCode(useExtendedPriceDecimalPlaces);
        return result;
    }

    @Override
    public String toString() {
        return "InvoicePdfConfig(showFooter=" + showFooter
                + ", useExtendedPriceDecimalPlaces=" + useExtendedPriceDecimalPlaces + ")";
    }

    public static InvoicePdfConfigBuilder builder() {
        return new InvoicePdfConfigBuilder();
    }

    public static final class InvoicePdfConfigBuilder {

        private boolean showFooter;
        private boolean useExtendedPriceDecimalPlaces;

        InvoicePdfConfigBuilder() {
        }

        public InvoicePdfConfigBuilder showFooter(boolean showFooter) {
            this.showFooter = showFooter;
            return this;
        }

        public InvoicePdfConfigBuilder useExtendedPriceDecimalPlaces(boolean useExtendedPriceDecimalPlaces) {
            this.useExtendedPriceDecimalPlaces = useExtendedPriceDecimalPlaces;
            return this;
        }

        public InvoicePdfConfig build() {
            return new InvoicePdfConfig(showFooter, useExtendedPriceDecimalPlaces);
        }

        @Override
        public String toString() {
            return "InvoicePdfConfig.InvoicePdfConfigBuilder(showFooter=" + showFooter
                    + ", useExtendedPriceDecimalPlaces=" + useExtendedPriceDecimalPlaces + ")";
        }
    }
}
