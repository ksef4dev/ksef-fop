package io.alapierre.ksef.fop;

import io.alapierre.ksef.fop.qr.enums.ContextIdentifierType;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.PrivateKey;
import java.time.LocalDate;

/**
 * Request for generating QR codes for KSeF invoices.
 * 
 * Supports two modes:
 * - ONLINE mode: Generates only KOD I (1 QR code)
 * - OFFLINE mode: Generates both KOD I and KOD II (2 QR codes)
 * 
 * For each QR code, provide either a direct URL or parameters to generate it.
 * Use static factory methods (onlineQrBuilder / offlineCertificateQrBuilder) to ensure required data is provided.
 * 
 * @see <a href="https://github.com/CIRFMF/ksef-docs/blob/main/kody-qr.md">KSeF QR Codes Documentation</a>
 */
@Data
public class InvoiceQRCodeGeneratorRequest {

    @Nullable
    private String environmentUrl;

    /**
     * Seller nip
     */
    @Nullable
    private String identifier;

    /**
     * Invoice issue date (P_1)
     */
    @Nullable
    private LocalDate issueDate;

    @Nullable
    private ContextIdentifierType ctxType;

    /**
     * Value of context identifier
     */
    @Nullable
    private String ctxValue;

    /**
     * Serial number of KSeF Certificate
     */
    @Nullable
    private String certSerial;

    @Nullable
    private PrivateKey privateKey;

    private boolean online;

    /**
     * Direct URL for KOD I (online verification QR code).
     * If provided, this URL will be used directly instead of generating from parameters.
     */
    @Nullable
    private String onlineQrCodeUrl;

    /**
     * Direct URL for KOD II (certificate verification QR code).
     * If provided, this URL will be used directly instead of generating from parameters.
     */
    @Nullable
    private String certificateQrCodeUrl;

    /**
     * Builder for ONLINE mode - generates only KOD I (online verification QR)
     * Use when invoice is already in KSeF system
     * 
     * @param onlineQrCodeUrl direct URL for KOD I
     */
    public static InvoiceQRCodeGeneratorRequest onlineQrBuilder(@Nullable String onlineQrCodeUrl) {
        InvoiceQRCodeGeneratorRequest request = new InvoiceQRCodeGeneratorRequest();
        request.onlineQrCodeUrl = onlineQrCodeUrl;
        request.online = true;
        return request;
    }

    /**
     * Builder for ONLINE mode - generates only KOD I (online verification QR)
     * Use when invoice is already in KSeF system
     * 
     * @param environmentUrl base URL (e.g. https://qr-test.ksef.mf.gov.pl)
     * @param sellerNip seller's NIP
     * @param issueDate invoice issue date
     */
    public static InvoiceQRCodeGeneratorRequest onlineQrBuilder(String environmentUrl,
                                                                String sellerNip,
                                                                LocalDate issueDate) {
        InvoiceQRCodeGeneratorRequest request = new InvoiceQRCodeGeneratorRequest(environmentUrl, sellerNip, issueDate);
        request.online = true;
        return request;
    }

    /**
     * Builder for OFFLINE mode - generates both KOD I and KOD II (online + certificate verification QRs)
     * Use when invoice is not yet in KSeF system - both QR codes from URLs
     * 
     * @param onlineQrCodeUrl direct URL for KOD I
     * @param certificateQrCodeUrl direct URL for KOD II
     */
    public static InvoiceQRCodeGeneratorRequest offlineCertificateQrBuilder(@Nullable String onlineQrCodeUrl,
                                                                            @Nullable String certificateQrCodeUrl) {
        InvoiceQRCodeGeneratorRequest request = new InvoiceQRCodeGeneratorRequest();
        request.onlineQrCodeUrl = onlineQrCodeUrl;
        request.certificateQrCodeUrl = certificateQrCodeUrl;
        request.online = false;
        return request;
    }
    
    /**
     * Builder for OFFLINE mode - KOD I from parameters, KOD II from URL
     * Use when you want to generate KOD I but have a direct URL for KOD II
     * 
     * @param environmentUrl base URL for generating KOD I
     * @param sellerNip seller's NIP for generating KOD I
     * @param issueDate invoice issue date for generating KOD I
     * @param certificateQrCodeUrl direct URL for KOD II
     */
    public static InvoiceQRCodeGeneratorRequest offlineCertificateQrBuilder(String environmentUrl,
                                                                            String sellerNip,
                                                                            LocalDate issueDate,
                                                                            @Nullable String certificateQrCodeUrl) {
        InvoiceQRCodeGeneratorRequest request = new InvoiceQRCodeGeneratorRequest(environmentUrl, sellerNip, issueDate);
        request.certificateQrCodeUrl = certificateQrCodeUrl;
        request.online = false;
        return request;
    }

    /**
     * Builder for OFFLINE mode - generates both KOD I and KOD II (online + certificate verification QRs)
     * Use when invoice is not yet in KSeF system
     * 
     * @param environmentUrl base URL (e.g. https://qr-test.ksef.mf.gov.pl)
     * @param ctxType context identifier type
     * @param ctxValue context identifier value
     * @param sellerNip seller's NIP
     * @param certSerial KSeF certificate serial number
     * @param privateKey private key for signing
     * @param issueDate invoice issue date
     */
    public static InvoiceQRCodeGeneratorRequest offlineCertificateQrBuilder(String environmentUrl,
                                                                            ContextIdentifierType ctxType,
                                                                            String ctxValue,
                                                                            String sellerNip,
                                                                            String certSerial,
                                                                            PrivateKey privateKey,
                                                                            LocalDate issueDate) {
        InvoiceQRCodeGeneratorRequest request = new InvoiceQRCodeGeneratorRequest(environmentUrl, sellerNip, issueDate);
        request.ctxValue = ctxValue;
        request.ctxType = ctxType;
        request.certSerial = certSerial;
        request.privateKey = privateKey;
        request.online = false;
        return request;
    }

    /**
     * Constructor for traditional QR code generation (used by builders)
     */
    public InvoiceQRCodeGeneratorRequest(@NotNull String environmentUrl, @NotNull String identifier, @NotNull LocalDate issueDate) {
        this.environmentUrl = environmentUrl;
        this.identifier = identifier;
        this.issueDate = issueDate;
    }

    private InvoiceQRCodeGeneratorRequest() {}

}