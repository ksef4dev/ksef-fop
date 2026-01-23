package io.alapierre.ksef.fop.qr.exceptions;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.12.22
 */
public class BarcodeGenerationException extends RuntimeException {

    public BarcodeGenerationException(String message) {
        super(message);
    }

    public BarcodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BarcodeGenerationException(Throwable cause) {
        super(cause);
    }
}
