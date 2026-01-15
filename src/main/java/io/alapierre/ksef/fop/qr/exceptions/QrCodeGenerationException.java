package io.alapierre.ksef.fop.qr.exceptions;


public class QrCodeGenerationException extends RuntimeException {

    public QrCodeGenerationException(String message) {
        super(message);
    }

    public QrCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public QrCodeGenerationException(Throwable cause) {
        super(cause);
    }
}

