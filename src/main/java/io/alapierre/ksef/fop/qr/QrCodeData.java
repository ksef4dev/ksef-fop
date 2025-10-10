package io.alapierre.ksef.fop.qr;

import lombok.*;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a single QR code with its associated data for PDF generation.
 * Each QR code can have an image, a label displayed below it, and a verification link.
 * 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrCodeData {
    
    /**
     * The QR code image as byte array (PNG format)
     */
    private byte @NotNull [] qrCodeImage;
    
    /**
     * Label displayed below the QR code (e.g., KSeF number)
     */
    @NotNull
    private String label;
    
    /**
     * Verification link associated with this QR code
     */
    @NotNull
    private String verificationLink;

    /**
     * Verification link associated with this QR code
     */
    @NotNull
    private String verificationLinkTitle;
    
}
