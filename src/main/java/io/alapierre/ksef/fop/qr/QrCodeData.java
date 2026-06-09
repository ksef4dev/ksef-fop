package io.alapierre.ksef.fop.qr;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a single QR code with its associated data for PDF generation.
 * Each QR code can have an image, a label displayed below it, and a verification link.
 * 
 */
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

    public QrCodeData() {
    }

    public QrCodeData(byte @NotNull [] qrCodeImage, @NotNull String label, @NotNull String verificationLink, @NotNull String verificationLinkTitle) {
        this.qrCodeImage = qrCodeImage;
        this.label = Objects.requireNonNull(label, "label");
        this.verificationLink = Objects.requireNonNull(verificationLink, "verificationLink");
        this.verificationLinkTitle = Objects.requireNonNull(verificationLinkTitle, "verificationLinkTitle");
    }

    public byte @NotNull [] getQrCodeImage() {
        return qrCodeImage;
    }

    public void setQrCodeImage(byte @NotNull [] qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    public void setLabel(@NotNull String label) {
        this.label = Objects.requireNonNull(label, "label");
    }

    @NotNull
    public String getVerificationLink() {
        return verificationLink;
    }

    public void setVerificationLink(@NotNull String verificationLink) {
        this.verificationLink = Objects.requireNonNull(verificationLink, "verificationLink");
    }

    @NotNull
    public String getVerificationLinkTitle() {
        return verificationLinkTitle;
    }

    public void setVerificationLinkTitle(@NotNull String verificationLinkTitle) {
        this.verificationLinkTitle = Objects.requireNonNull(verificationLinkTitle, "verificationLinkTitle");
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof QrCodeData) {
            QrCodeData other = (QrCodeData) o;
            return other.canEqual(this)
                    && Arrays.equals(qrCodeImage, other.qrCodeImage)
                    && Objects.equals(label, other.label)
                    && Objects.equals(verificationLink, other.verificationLink)
                    && Objects.equals(verificationLinkTitle, other.verificationLinkTitle);
        }
        return false;
    }

    protected boolean canEqual(Object other) {
        return other instanceof QrCodeData;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(qrCodeImage);
        result = 31 * result + Objects.hash(label, verificationLink, verificationLinkTitle);
        return result;
    }

    @Override
    public String toString() {
        return "QrCodeData(qrCodeImage=" + Arrays.toString(qrCodeImage)
                + ", label=" + label
                + ", verificationLink=" + verificationLink
                + ", verificationLinkTitle=" + verificationLinkTitle + ")";
    }

    public static QrCodeDataBuilder builder() {
        return new QrCodeDataBuilder();
    }

    public static final class QrCodeDataBuilder {

        private byte @NotNull [] qrCodeImage;
        private String label;
        private String verificationLink;
        private String verificationLinkTitle;

        QrCodeDataBuilder() {
        }

        public QrCodeDataBuilder qrCodeImage(byte @NotNull [] qrCodeImage) {
            this.qrCodeImage = qrCodeImage;
            return this;
        }

        public QrCodeDataBuilder label(@NotNull String label) {
            this.label = Objects.requireNonNull(label, "label");
            return this;
        }

        public QrCodeDataBuilder verificationLink(@NotNull String verificationLink) {
            this.verificationLink = Objects.requireNonNull(verificationLink, "verificationLink");
            return this;
        }

        public QrCodeDataBuilder verificationLinkTitle(@NotNull String verificationLinkTitle) {
            this.verificationLinkTitle = Objects.requireNonNull(verificationLinkTitle, "verificationLinkTitle");
            return this;
        }

        public QrCodeData build() {
            return new QrCodeData(qrCodeImage, label, verificationLink, verificationLinkTitle);
        }

        @Override
        public String toString() {
            return "QrCodeData.QrCodeDataBuilder(qrCodeImage=" + Arrays.toString(qrCodeImage)
                    + ", label=" + label
                    + ", verificationLink=" + verificationLink
                    + ", verificationLinkTitle=" + verificationLinkTitle + ")";
        }
    }
}
