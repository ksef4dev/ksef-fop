package io.alapierre.ksef.fop.qr;

import io.alapierre.ksef.fop.qr.exceptions.VerificationLinkGenerationException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;

@Slf4j
public final class CryptoUtils {

    private static final String SHA_256 = "SHA-256";
    private static final String SHA_256_WITH_RSA = "SHA256withRSA";
    private static final String SHA_256_WITH_ECDSA = "SHA256withECDSA";

    /**
     * Computes SHA-256 over invoice XML and encodes it as Base64URL without padding.
     */
    public static String computeInvoiceHashBase64Url(byte[] invoiceXml) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] sha = digest.digest(invoiceXml);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sha);
        } catch (NoSuchAlgorithmException e) {
            throw new VerificationLinkGenerationException("SHA-256 algorithm not available", e);
        }
    }

    public static String computeUrlEncodedSignedHash(String pathToSign, PrivateKey privateKey) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance(SHA_256);
            byte[] sha = sha256.digest(pathToSign.getBytes(StandardCharsets.UTF_8));


            Signature signature;
            if (privateKey instanceof RSAPrivateKey) {
                signature = Signature.getInstance(SHA_256_WITH_RSA);
            } else if (privateKey instanceof ECPrivateKey) {
                signature = Signature.getInstance(SHA_256_WITH_ECDSA);
            } else {
                throw new VerificationLinkGenerationException("Certificate not support RSA or ECDsa.", null);
            }

            signature.initSign(privateKey);
            signature.update(sha);
            byte[] signedBytes = signature.sign();


            return Base64.getUrlEncoder().withoutPadding().encodeToString(signedBytes);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new VerificationLinkGenerationException("Cannot compute signature", e);
        }
    }

}
