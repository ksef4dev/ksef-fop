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
    private boolean showFooter = false;
}
