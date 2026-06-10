package io.alapierre.ksef.fop.qr;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a single QR code with its associated data for PDF generation.
 * Each QR code can have an image, a label displayed below it, and a verification link.
 */
public class QrCodeData {

    private byte @NotNull [] qrCodeImage;

    @NotNull
    private String label;

    @NotNull
    private String verificationLink;

    @NotNull
    private String verificationLinkTitle;

    /**
     * Creates an empty instance; populate it through the setters.
     *
     * @deprecated use the builder instead.
     */
    @Deprecated
    public QrCodeData() {
        // Suppresses Sonar warning
        this.qrCodeImage = new byte[0];
        this.label = "";
        this.verificationLink = "";
        this.verificationLinkTitle = "";
    }

    /**
     * Creates a fully populated QR code.
     * @param qrCodeImage the QR code image as a PNG byte array
     * @param label the label displayed below the QR code
     * @param verificationLink the verification link
     * @param verificationLinkTitle the title of the verification link
     */
    public QrCodeData(byte @NotNull [] qrCodeImage, @NotNull String label, @NotNull String verificationLink, @NotNull String verificationLinkTitle) {
        this(builder()
                .qrCodeImage(qrCodeImage)
                .label(label)
                .verificationLink(verificationLink)
                .verificationLinkTitle(verificationLinkTitle));
    }

    private QrCodeData(QrCodeDataBuilder builder) {
        this.qrCodeImage = Objects.requireNonNull(builder.qrCodeImage, "qrCodeImage");
        this.label = Objects.requireNonNull(builder.label, "label");
        this.verificationLink = Objects.requireNonNull(builder.verificationLink, "verificationLink");
        this.verificationLinkTitle = Objects.requireNonNull(builder.verificationLinkTitle, "verificationLinkTitle");
    }

    /**
     * Returns the QR code image as a PNG byte array.
     * @return the QR code image bytes
     */
    public byte @NotNull [] getQrCodeImage() {
        return qrCodeImage;
    }

    /**
     * Sets the QR code image as a PNG byte array.
     * @param qrCodeImage the QR code image bytes
     */
    public void setQrCodeImage(byte @NotNull [] qrCodeImage) {
        this.qrCodeImage = Objects.requireNonNull(qrCodeImage, "qrCodeImage");
    }

    /**
     * Returns the label displayed below the QR code.
     * @return the label
     */
    @NotNull
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label displayed below the QR code.
     * @param label the label, never {@code null}
     */
    public void setLabel(@NotNull String label) {
        this.label = Objects.requireNonNull(label, "label");
    }

    /**
     * Returns the verification link.
     * @return the verification link
     */
    @NotNull
    public String getVerificationLink() {
        return verificationLink;
    }

    /**
     * Sets the verification link.
     * @param verificationLink the verification link, never {@code null}
     */
    public void setVerificationLink(@NotNull String verificationLink) {
        this.verificationLink = Objects.requireNonNull(verificationLink, "verificationLink");
    }

    /**
     * Returns the title of the verification link.
     * @return the verification link title
     */
    @NotNull
    public String getVerificationLinkTitle() {
        return verificationLinkTitle;
    }

    /**
     * Sets the title of the verification link.
     * @param verificationLinkTitle the verification link title, never {@code null}
     */
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

    /**
     * Tells whether {@code other} may be compared for equality with this instance.
     * @param other the object to test
     * @return {@code true} if {@code other} is a {@code QrCodeData}
     */
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

    /**
     * Creates a new builder for {@link QrCodeData}.
     * @return a fresh builder
     */
    public static QrCodeDataBuilder builder() {
        return new QrCodeDataBuilder();
    }

    /**
     * Fluent builder for {@link QrCodeData}.
     */
    public static final class QrCodeDataBuilder {

        private byte [] qrCodeImage;
        private String label;
        private String verificationLink;
        private String verificationLinkTitle;

        QrCodeDataBuilder() {
        }

        /**
         * Sets the QR code image as a PNG byte array.
         * @param qrCodeImage the QR code image bytes
         * @return this builder
         */
        public QrCodeDataBuilder qrCodeImage(byte @NotNull [] qrCodeImage) {
            this.qrCodeImage = qrCodeImage;
            return this;
        }

        /**
         * Sets the label displayed below the QR code.
         * @param label the label
         * @return this builder
         */
        public QrCodeDataBuilder label(@NotNull String label) {
            this.label = label;
            return this;
        }

        /**
         * Sets the verification link.
         * @param verificationLink the verification link
         * @return this builder
         */
        public QrCodeDataBuilder verificationLink(@NotNull String verificationLink) {
            this.verificationLink = verificationLink;
            return this;
        }

        /**
         * Sets the title of the verification link.
         * @param verificationLinkTitle the verification link title
         * @return this builder
         */
        public QrCodeDataBuilder verificationLinkTitle(@NotNull String verificationLinkTitle) {
            this.verificationLinkTitle = verificationLinkTitle;
            return this;
        }

        /**
         * Builds a {@link QrCodeData} from this builder's state.
         * @return the configured QR code data
         */
        public QrCodeData build() {
            return new QrCodeData(this);
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