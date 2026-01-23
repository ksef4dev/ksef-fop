package io.alapierre.ksef.fop.qr;

import io.alapierre.ksef.fop.qr.enums.ContextIdentifierType;
import lombok.extern.slf4j.Slf4j;

import java.security.PrivateKey;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static io.alapierre.ksef.fop.qr.CryptoUtils.computeUrlEncodedSignedHash;

@Slf4j
public final class VerificationLinkGenerator {

    private static final DateTimeFormatter KSEF_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private VerificationLinkGenerator() {
    }

    // ===== KOD I =====

    /**
     * Builds a KSeF verification link (CODE I) according to the specification:
     * https://{env}/invoice/{NIP}/{DD-MM-YYYY}/{SHA256(xml) in Base64URL without padding}
     *
     * @param environmentUrl base URL (e.g. <a href="https://qr-test.ksef.mf.gov.pl">...</a>)
     * @param nip          the seller’s NIP (10 digits; all non-digit characters will be removed)
     * @param issueDate    the invoice issue date (field P_1) – formatted as dd-MM-yyyy
     * @param invoiceXml   the full invoice content in XML (as bytes), used to calculate the SHA-256 hash
     * @return a verification URL (CODE I) that allows verifying or retrieving the invoice in KSeF
     */
    public static String generateVerificationLink(String environmentUrl,
                                                  String nip,
                                                  LocalDate issueDate,
                                                  byte[] invoiceXml) {
        String base = trimTrailingSlash(environmentUrl);
        String normalizedNip = normalizeAndValidateNip(nip);
        String date = issueDate.format(KSEF_DATE);
        String hash = CryptoUtils.computeInvoiceHashBase64Url(invoiceXml);
        return String.format("%s/invoice/%s/%s/%s", base, normalizedNip, date, hash);
    }


    // ===== KOD II =====

    /**
     * Builds a KSeF certificate verification link (CODE II) and signs the path using either RSA-PSS or ECDSA.
     * Signature input is the URL path without protocol and trailing slash, e.g.:
     * {{environmentUrl}}/certificate/Nip/1111111111/1111111111/01F20A5D352AE590/{hash}
     *
     * @param environmentUrl base URL (e.g. <a href="https://qr-test.ksef.mf.gov.pl">...</a>)
     * @param ctxType     context identifier type (Nip, InternalId, NipVatUe, PeppolId)
     * @param ctxValue    context identifier value
     * @param sellerNip   seller’s NIP (10 digits)
     * @param certSerial  KSeF certificate serial number (hex string expected by docs)
     * @param invoiceXml  the full invoice content in XML (as bytes), used to calculate the SHA-256 hash
     * @param privateKey  private key – RSA (for RSA-PSS) or EC (for ECDSA P-256)
     * @return full CODE II URL with Base64URL(no padding) signature appended
     */
    public static String generateCertificateVerificationLink(String environmentUrl,
                                                             ContextIdentifierType ctxType,
                                                             String ctxValue,
                                                             String sellerNip,
                                                             String certSerial,
                                                             PrivateKey privateKey,
                                                             byte[] invoiceXml) {
        String invoiceHashUrlEncoded = CryptoUtils.computeInvoiceHashBase64Url(invoiceXml);

        String baseUrl = trimTrailingSlash(environmentUrl);
        String normalizedNip = normalizeAndValidateNip(sellerNip);

        String pathToSign = String.format("%s/certificate/%s/%s/%s/%s/%s",
                        baseUrl,
                        ctxType.pathPart(),
                        ctxValue,
                        normalizedNip,
                        certSerial,
                        invoiceHashUrlEncoded)
                .replace("https://", "");
        String signedHash = computeUrlEncodedSignedHash(pathToSign, privateKey);

        return String.format("%s/certificate/%s/%s/%s/%s/%s/%s",
                baseUrl,
                ctxType.pathPart(),
                ctxValue,
                normalizedNip,
                certSerial,
                invoiceHashUrlEncoded,
                signedHash);
    }

    private static String trimTrailingSlash(String url) {
        if (url == null) throw new IllegalArgumentException("Environment URL is null");
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String normalizeAndValidateNip(String nip) {
        String digits = nip == null ? "" : nip.replaceAll("\\D", "");
        if (digits.length() != 10) throw new IllegalArgumentException("NIP must contain exactly 10 digits");
        return digits;
    }

}
