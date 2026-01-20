package io.alapierre.ksef.fop.qr;

import io.alapierre.ksef.fop.InvoiceQRCodeGeneratorRequest;
import io.alapierre.ksef.fop.i18n.TranslationService;
import io.alapierre.ksef.fop.qr.exceptions.QrCodeGenerationException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Builder for creating QR codes for invoice PDFs.
 *
 * Supports two modes:
 * - ONLINE mode: Generates only KOD I (online verification QR) - for invoices already in KSeF
 * - OFFLINE mode: Generates both KOD I and KOD II (online + certificate verification QRs) - for invoices not yet in KSeF
 *
 * Each QR code can be generated from:
 * - Direct URL (if provided in InvoiceQRCodeGeneratorRequest)
 * - Parameters (environmentUrl, identifier, issueDate, etc.) - URL will be generated
 */
@RequiredArgsConstructor
public class QrCodeBuilder {

    private static final int QR_SIZE = 200;
    private final TranslationService translationService;

    /**
     * Builds QR codes based on the request. Returns null if request is null.
     * For online mode returns single QR code, for offline mode returns two QR codes (online + certificate).
     *
     * @param request the QR code generation request
     * @param ksefNumber optional KSeF number to display as label (if null or blank, uses offline label)
     * @param invoiceXmlBytes the invoice XML bytes
     * @param langCode the language code for translations
     * @return list of QR code data, or null if request is null
     */
    public @Nullable List<QrCodeData> buildQrCodes(@Nullable InvoiceQRCodeGeneratorRequest request,
                                                   @Nullable String ksefNumber,
                                                   byte @NotNull [] invoiceXmlBytes,
                                                   @NotNull String langCode) {
        if (request == null) return null;

        QrCodeData online = buildOnlineQr(request, ksefNumber, invoiceXmlBytes, langCode);
        if (request.isOnline()) { // KOD I
            return Collections.singletonList(online);
        } else { // KOD I + KOD II
            QrCodeData cert = buildCertificateQr(request, invoiceXmlBytes, langCode);
            return Arrays.asList(online, cert);
        }
    }


    /**
     * Builds an online verification QR code (KOD I) from a request.
     * Uses direct URL if provided, otherwise generates from parameters.
     *
     * @param req the QR code generation request
     * @param ksefNumber optional KSeF number to display as label
     * @param invoiceXmlBytes the invoice XML bytes (used only if generating from parameters)
     * @param langCode the language code for translations
     * @return QR code data
     * @throws QrCodeGenerationException if URL is not provided and required parameters are missing
     */
    public @NotNull QrCodeData buildOnlineQr(@NotNull InvoiceQRCodeGeneratorRequest req,
                                              @Nullable String ksefNumber,
                                              byte @NotNull [] invoiceXmlBytes,
                                              @NotNull String langCode) {
        if (isNotBlank(req.getOnlineQrCodeUrl())) {
            return buildOnlineQr(req.getOnlineQrCodeUrl(), ksefNumber, langCode);
        }

        if (req.getEnvironmentUrl() == null || req.getIdentifier() == null || req.getIssueDate() == null) {
            throw new QrCodeGenerationException(
                "When onlineQrCodeUrl is not provided, environmentUrl, identifier, and issueDate are required");
        }

        String link = VerificationLinkGenerator.generateVerificationLink(
                req.getEnvironmentUrl(), req.getIdentifier(), req.getIssueDate(), invoiceXmlBytes);
        return buildOnlineQr(link.trim(), ksefNumber, langCode);
    }

    /**
     * Builds QR code for KOD I (online verification) from a direct URL.
     *
     * @param url URL for KOD I (online verification)
     * @param ksefNumber optional KSeF number to display as label (if null or blank, uses offline label)
     * @param langCode the language code for translations
     * @return QR code data
     */
    public @NotNull QrCodeData buildOnlineQr(@NotNull String url,
                                             @Nullable String ksefNumber,
                                             @NotNull String langCode) {
        String label = isNotBlank(ksefNumber) ? ksefNumber : translationService.getTranslation(langCode, "qr.offline");
        String title = translationService.getTranslation(langCode, "qr.onlineTitle");
        return qrFromLink(url.trim(), label, title);
    }


    /**
     * Builds a certificate verification QR code (KOD II) from a request.
     * Uses direct URL if provided, otherwise generates from parameters.
     *
     * @param req the QR code generation request
     * @param invoiceXmlBytes the invoice XML bytes (used only if generating from parameters)
     * @param langCode the language code for translations
     * @return QR code data
     * @throws QrCodeGenerationException if URL is not provided and required parameters are missing
     */
    public @NotNull QrCodeData buildCertificateQr(@NotNull InvoiceQRCodeGeneratorRequest req,
                                                   byte @NotNull [] invoiceXmlBytes,
                                                   @NotNull String langCode) {
        if (isNotBlank(req.getCertificateQrCodeUrl())) {
            return buildCertificateQr(req.getCertificateQrCodeUrl(), langCode);
        }

        if (req.getEnvironmentUrl() == null || req.getCtxType() == null || req.getCtxValue() == null ||
            req.getIdentifier() == null || req.getCertSerial() == null || req.getPrivateKey() == null) {
            throw new QrCodeGenerationException(
                "When certificateQrCodeUrl is not provided, environmentUrl, ctxType, ctxValue, identifier, certSerial, and privateKey are required");
        }

        String link = VerificationLinkGenerator.generateCertificateVerificationLink(
                req.getEnvironmentUrl(),
                req.getCtxType(),
                req.getCtxValue(),
                req.getIdentifier(),
                req.getCertSerial(),
                req.getPrivateKey(),
                invoiceXmlBytes
        );
        return buildCertificateQr(link.trim(), langCode);
    }

    /**
     * Builds QR code for KOD II (certificate verification) from a direct URL.
     *
     * @param url URL for KOD II (certificate verification)
     * @param langCode the language code for translations
     * @return QR code data
     */
    public @NotNull QrCodeData buildCertificateQr(@NotNull String url,
                                                  @NotNull String langCode) {
        String label = translationService.getTranslation(langCode, "qr.certificate");
        String title = translationService.getTranslation(langCode, "qr.certificateTitle");
        return qrFromLink(url.trim(), label, title);
    }

    /**
     * @deprecated This method will be private in version 2.0.0.
     *             Please use methods dedicated for online or offline build instead, such as
     *             {@link #buildOnlineQr(String, String, String)} for online builds or
     *             {@link #buildCertificateQr(String, String)} for offline builds.
     */
    @Deprecated
    public @NotNull QrCodeData qrFromLink(@NotNull String link, @NotNull String label, @NotNull String title) {
        byte[] image = QrCodeGenerator.generateBarcode(link, QR_SIZE, QR_SIZE);
        return QrCodeData.builder()
                .qrCodeImage(image)
                .label(label)
                .verificationLink(link)
                .verificationLinkTitle(title)
                .build();
    }

    private boolean isNotBlank(@Nullable String str) {
        return str != null && !str.trim().isEmpty();
    }
}
