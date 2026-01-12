package io.alapierre.ksef.fop.qr;

import io.alapierre.ksef.fop.InvoiceGenerationParams;
import io.alapierre.ksef.fop.InvoiceQRCodeGeneratorRequest;
import io.alapierre.ksef.fop.i18n.TranslationService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
     * Builds QR codes based on invoice generation parameters.
     * Each QR code (KOD I and KOD II) can be generated independently from either:
     * - Direct URL (onlineQrCodeUrl / certificateQrCodeUrl)
     * - InvoiceQRCodeGeneratorRequest (traditional generation)
     *
     * @param params the invoice generation parameters
     * @param invoiceXmlBytes the invoice XML bytes (used only if generating from request)
     * @param langCode the language code for translations
     * @return list of QR code data, or null if no QR codes can be generated
     */
    public @Nullable List<QrCodeData> buildQrCodes(@NotNull InvoiceGenerationParams params,
                                                   byte @NotNull [] invoiceXmlBytes,
                                                   @NotNull String langCode) {
        List<QrCodeData> qrCodes = new ArrayList<>();

        QrCodeData onlineQr = buildOnlineQrCode(params, invoiceXmlBytes, langCode);
        if (onlineQr != null) {
            qrCodes.add(onlineQr);
        }

        QrCodeData certificateQr = buildCertificateQrCode(params, invoiceXmlBytes, langCode);
        if (certificateQr != null) {
            qrCodes.add(certificateQr);
        }

        return qrCodes.isEmpty() ? null : qrCodes;
    }

    /**
     * Builds online verification QR code (KOD I).
     * Uses direct URL if provided, otherwise generates from request.
     *
     * @param params the invoice generation parameters
     * @param invoiceXmlBytes the invoice XML bytes (used only if generating from request)
     * @param langCode the language code for translations
     * @return QR code data for online verification, or null if cannot be generated
     */
    private @Nullable QrCodeData buildOnlineQrCode(@NotNull InvoiceGenerationParams params,
                                                    byte @NotNull [] invoiceXmlBytes,
                                                    @NotNull String langCode) {
        if (isNotBlank(params.getOnlineQrCodeUrl())) {
            return buildOnlineQrCodeFromUrl(params.getOnlineQrCodeUrl(), params.getKsefNumber(), langCode);
        }

        if (params.getInvoiceQRCodeGeneratorRequest() != null) {
            return buildOnlineQr(params.getInvoiceQRCodeGeneratorRequest(), params.getKsefNumber(), invoiceXmlBytes, langCode);
        }

        return null;
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
    public @Nullable QrCodeData buildOnlineQr(@NotNull InvoiceQRCodeGeneratorRequest req,
                                              @Nullable String ksefNumber,
                                              byte @NotNull [] invoiceXmlBytes,
                                              @NotNull String langCode) {
        String link = VerificationLinkGenerator.generateVerificationLink(
                req.getEnvironmentUrl(), req.getIdentifier(), req.getIssueDate(), invoiceXmlBytes);
        return buildOnlineQrCodeFromUrl(link, ksefNumber, langCode);
    }

    /**
     * Builds QR code for KOD I (online verification) from a direct URL.
     *
     * @param url URL for KOD I (online verification)
     * @param ksefNumber optional KSeF number to display as label (if null or blank, uses offline label)
     * @param langCode the language code for translations
     * @return QR code data, or null if url is null or empty
     */
    public @Nullable QrCodeData buildOnlineQrCodeFromUrl(@Nullable String url,
                                                         @Nullable String ksefNumber,
                                                         @NotNull String langCode) {
        if (!isNotBlank(url)) {
            return null;
        }

        String label = isNotBlank(ksefNumber) ? ksefNumber : translationService.getTranslation(langCode, "qr.offline");
        String title = translationService.getTranslation(langCode, "qr.onlineTitle");
        return qrFromLink(url.trim(), label, title);
    }

    /**
     * Builds certificate verification QR code (KOD II).
     * Uses direct URL if provided, otherwise generates from request (if offline mode).
     *
     * @param params the invoice generation parameters
     * @param invoiceXmlBytes the invoice XML bytes (used only if generating from request)
     * @param langCode the language code for translations
     * @return QR code data for certificate verification, or null if cannot be generated
     */
    private @Nullable QrCodeData buildCertificateQrCode(@NotNull InvoiceGenerationParams params,
                                                        byte @NotNull [] invoiceXmlBytes,
                                                        @NotNull String langCode) {
        if (isNotBlank(params.getCertificateQrCodeUrl())) {
            return buildCertificateQrCodeFromUrl(params.getCertificateQrCodeUrl(), langCode);
        }

        InvoiceQRCodeGeneratorRequest request = params.getInvoiceQRCodeGeneratorRequest();
        if (request != null && !request.isOnline()) {
            return buildCertificateQr(request, invoiceXmlBytes, langCode);
        }

        return null;
    }

    /**
     * Builds a certificate verification QR code (KOD II).
     *
     * @param req the QR code generation request
     * @param invoiceXmlBytes the invoice XML bytes
     * @param langCode the language code for translations
     * @return QR code data
     */
    public @Nullable QrCodeData buildCertificateQr(@NotNull InvoiceQRCodeGeneratorRequest req,
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
        return buildCertificateQrCodeFromUrl(link, langCode);
    }

    /**
     * Builds QR code for KOD II (certificate verification) from a direct URL.
     *
     * @param url URL for KOD II (certificate verification)
     * @param langCode the language code for translations
     * @return QR code data, or null if url is null or empty
     */
    public @Nullable QrCodeData buildCertificateQrCodeFromUrl(@Nullable String url,
                                                              @NotNull String langCode) {
        if (!isNotBlank(url)) {
            return null;
        }

        String label = translationService.getTranslation(langCode, "qr.certificate");
        String title = translationService.getTranslation(langCode, "qr.certificateTitle");
        return qrFromLink(url.trim(), label, title);
    }

    private @NotNull QrCodeData qrFromLink(@NotNull String link, @NotNull String label, @NotNull String title) {
        byte[] image = QrCodeGenerator.generateBarcode(link, QR_SIZE, QR_SIZE);
        return QrCodeData.builder()
                .qrCodeImage(image)
                .label(label)
                .verificationLink(link)
                .verificationLinkTitle(title)
                .build();
    }

    private boolean isNotBlank(@Nullable String str) {
        return str != null && !str.isBlank();
    }
}
