package io.alapierre.ksef.fop.qr;

import io.alapierre.ksef.fop.InvoiceQRCodeGeneratorRequest;
import io.alapierre.ksef.fop.i18n.TranslationService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Builder for creating QR codes for invoice PDFs.
 * Handles both online and certificate-based verification QR codes.
 */
@RequiredArgsConstructor
public class QrCodeBuilder {

    private static final int QR_SIZE = 200;
    private final TranslationService translationService;

    /**
     * Builds QR codes based on the request. Returns null if request is null.
     * For online mode returns single QR code, for offline mode returns two QR codes (online + certificate).
     *
     * @param req the QR code generation request
     * @param ksefNumber optional KSeF number to display as label (if null or blank, uses offline label)
     * @param invoiceXmlBytes the invoice XML bytes
     * @param langCode the language code for translations
     * @return list of QR code data, or null if request is null
     */
    public @Nullable List<QrCodeData> buildQrCodes(@Nullable InvoiceQRCodeGeneratorRequest req,
                                                   @Nullable String ksefNumber,
                                                   byte @NotNull [] invoiceXmlBytes,
                                                   @NotNull String langCode) {
        if (req == null) return null;

        QrCodeData online = buildOnlineQr(req, ksefNumber, invoiceXmlBytes, langCode);
        if (req.isOnline()) { // KOD I
            return List.of(online);
        } else { // KOD I + KOD II
            QrCodeData cert = buildCertificateQr(req, invoiceXmlBytes, langCode);
            return List.of(online, cert);
        }
    }

    /**
     * Builds an online verification QR code (KOD I).
     *
     * @param req the QR code generation request
     * @param ksefNumber optional KSeF number to display as label
     * @param invoiceXmlBytes the invoice XML bytes
     * @param langCode the language code for translations
     * @return QR code data
     */
    public @NotNull QrCodeData buildOnlineQr(@NotNull InvoiceQRCodeGeneratorRequest req,
                                             @Nullable String ksefNumber,
                                             byte @NotNull [] invoiceXmlBytes,
                                             @NotNull String langCode) {
        String link = VerificationLinkGenerator.generateVerificationLink(
                req.getEnvironmentUrl(), req.getIdentifier(), req.getIssueDate(), invoiceXmlBytes);

        String labelOffline = translationService.getTranslation(langCode, "qr.offline");
        String titleOnline = translationService.getTranslation(langCode, "qr.onlineTitle");

        String label = (ksefNumber != null && !ksefNumber.isBlank()) ? ksefNumber : labelOffline;
        return qrFromLink(link, label, titleOnline);
    }

    /**
     * Builds a certificate verification QR code (KOD II).
     *
     * @param req the QR code generation request
     * @param invoiceXmlBytes the invoice XML bytes
     * @param langCode the language code for translations
     * @return QR code data
     */
    public @NotNull QrCodeData buildCertificateQr(@NotNull InvoiceQRCodeGeneratorRequest req,
                                                   byte @NotNull [] invoiceXmlBytes,
                                                   @NotNull String langCode) {
        String link = VerificationLinkGenerator.generateCertificateVerificationLink(
                req.getEnvironmentUrl(),
                req.getCtxType(),
                req.getCtxValue(),
                req.getIdentifier(),
                req.getCertSerial(),
                req.getPrivateKey(),
                invoiceXmlBytes
        );
        String labelCert = translationService.getTranslation(langCode, "qr.certificate");
        String titleCert = translationService.getTranslation(langCode, "qr.certificateTitle");
        return qrFromLink(link, labelCert, titleCert);
    }

    /**
     * Creates a QR code data object from a verification link.
     *
     * @param link the verification link
     * @param label the label to display below the QR code
     * @param title the title for the verification link
     * @return QR code data
     */
    public @NotNull QrCodeData qrFromLink(@NotNull String link, @NotNull String label, @NotNull String title) {
        byte[] image = QrCodeGenerator.generateBarcode(link, QR_SIZE, QR_SIZE);
        return QrCodeData.builder()
                .qrCodeImage(image)
                .label(label)
                .verificationLink(link)
                .verificationLinkTitle(title)
                .build();
    }
}
