package io.alapierre.ksef.fop;

import io.alapierre.ksef.fop.qr.enums.ContextIdentifierType;
import io.alapierre.ksef.fop.qr.enums.Environment;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.PrivateKey;
import java.time.LocalDate;

/**
 * <a href="https://github.com/CIRFMF/ksef-docs/blob/main/kody-qr.md">...</a>
 */
@Data
public class InvoiceQRCodeGeneratorRequest {

    @NotNull
    private Environment environment;

    /**
     * Seller nip
     */
    @NotNull
    private String identifier;

    /**
     * Invoice issue date (P_1)
     */
    @NotNull
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
     * Builder for online verification QR (KOD I)
     */
    public static InvoiceQRCodeGeneratorRequest onlineQrBuilder(Environment environment,
                                                                String sellerNip,
                                                                LocalDate issueDate) {
        InvoiceQRCodeGeneratorRequest invoiceQRCodeGeneratorRequest = new InvoiceQRCodeGeneratorRequest(environment, sellerNip, issueDate);
        invoiceQRCodeGeneratorRequest.online = true;
        return invoiceQRCodeGeneratorRequest;
    }

    /**
     * Builder for certificate verification QR (KOD II)
     */
    public static InvoiceQRCodeGeneratorRequest offlineCertificateQrBuilder(Environment environment,
                                                                            ContextIdentifierType ctxType,
                                                                            String ctxValue,
                                                                            String sellerNip,
                                                                            String certSerial,
                                                                            PrivateKey privateKey,
                                                                            LocalDate issueDate) {
        InvoiceQRCodeGeneratorRequest invoiceQRCodeGeneratorRequest = new InvoiceQRCodeGeneratorRequest(environment, sellerNip, issueDate);
        invoiceQRCodeGeneratorRequest.ctxValue = ctxValue;
        invoiceQRCodeGeneratorRequest.ctxType = ctxType;
        invoiceQRCodeGeneratorRequest.certSerial = certSerial;
        invoiceQRCodeGeneratorRequest.privateKey = privateKey;
        invoiceQRCodeGeneratorRequest.online = false;
        return invoiceQRCodeGeneratorRequest;
    }
}