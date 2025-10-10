package io.alapierre.ksef.fop.qr;

import io.alapierre.ksef.fop.qr.enums.ContextIdentifierType;
import io.alapierre.ksef.fop.qr.enums.Environment;
import lombok.extern.slf4j.Slf4j;

import java.security.PrivateKey;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public final class VerificationLinkGenerator {

    private static final DateTimeFormatter KSEF_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private VerificationLinkGenerator() {
    }

    // ===== KOD I =====

    /**
     * Builds a KSeF verification link (CODE I) according to the specification:
     * https://{env}/client-app/invoice/{NIP}/{DD-MM-YYYY}/{SHA256(xml) in Base64URL without padding}
     *
     * @param environment  the environment (must return the base URL, e.g. <a href="https://ksef-test.mf.gov.pl">...</a>)
     * @param nip          the seller’s NIP (10 digits; all non-digit characters will be removed)
     * @param issueDate    the invoice issue date (field P_1) – formatted as dd-MM-yyyy
     * @param invoiceXml   the full invoice content in XML (as bytes), used to calculate the SHA-256 hash
     * @return a verification URL (CODE I) that allows verifying or retrieving the invoice in KSeF
     */
    public static String generateVerificationLink(Environment environment,
                                                  String nip,
                                                  LocalDate issueDate,
                                                  byte[] invoiceXml) {
        String base = trimTrailingSlash(environment.getUrl());
        String normalizedNip = normalizeAndValidateNip(nip);
        String date = issueDate.format(KSEF_DATE);
        String hash = CryptoUtils.computeInvoiceHashBase64Url(invoiceXml);
        return String.format("%s/client-app/invoice/%s/%s/%s", base, normalizedNip, date, hash);
    }


    // ===== KOD II =====

    /**
     * Builds a KSeF certificate verification link (CODE II) and signs the path using either RSA-PSS or ECDSA.
     * Signature input is the URL path without protocol and trailing slash, e.g.:
     *   ksef-test.mf.gov.pl/client-app/certificate/Nip/1111111111/1111111111/01F20A5D352AE590/{hash}
     *
     * @param environment base URL (e.g. <a href="https://ksef-test.mf.gov.pl">...</a>)
     * @param ctxType     context identifier type (Nip, InternalId, NipVatUe, PeppolId)
     * @param ctxValue    context identifier value
     * @param sellerNip   seller’s NIP (10 digits)
     * @param certSerial  KSeF certificate serial number (hex string expected by docs)
     * @param invoiceXml  SHA-256 of invoice XML in Base64URL (no padding)
     * @param privateKey  private key – RSA (for RSA-PSS) or EC (for ECDSA P-256)
     * @return full CODE II URL with Base64URL(no padding) signature appended
     */
    public static String generateCertificateVerificationLink(Environment environment,
                                                             ContextIdentifierType ctxType,
                                                             String ctxValue,
                                                             String sellerNip,
                                                             String certSerial,
                                                             PrivateKey privateKey,
                                                             byte[] invoiceXml) {
        String invoiceHash = CryptoUtils.computeInvoiceHashBase64Url(invoiceXml);


        String base = trimTrailingSlash(environment.getUrl());
        String normalizedNip = normalizeAndValidateNip(sellerNip);

        // 1) Build unsigned path (without protocol, without trailing slash)
        //    hostPart + /client-app/certificate/{CtxType}/{CtxValue}/{SellerNip}/{CertSerial}/{Hash}
        String unsignedPath = String.format("%s/client-app/certificate/%s/%s/%s/%s/%s",
                        base,
                        ctxType.pathPart(),
                        ctxValue,
                        normalizedNip,
                        certSerial,
                        invoiceHash)
                .replace("https://", "");

        // 2) Sign the path
        String signature = CryptoUtils.computeUrlEncodedSignedHash(unsignedPath, privateKey);

        // 3) Assemble full URL (with protocol)
        return String.format("%s/client-app/certificate/%s/%s/%s/%s/%s/%s",
                base,
                ctxType.pathPart(),
                ctxValue,
                normalizedNip,
                certSerial,
                invoiceHash,
                signature);
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
