package io.alapierre.ksef.fop.qr;

import io.alapierre.ksef.fop.qr.exceptions.VerificationLinkGenerationException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
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
            byte[] data = pathToSign.getBytes(StandardCharsets.UTF_8);

            Signature signature;
            if (privateKey instanceof RSAPrivateKey) {
                signature = Signature.getInstance("RSASSA-PSS");
                PSSParameterSpec pssSpec = new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1);
                signature.setParameter(pssSpec);
            } else if (privateKey instanceof ECPrivateKey) {
                signature = Signature.getInstance(SHA_256_WITH_ECDSA);
            } else {
                throw new VerificationLinkGenerationException("Certificate not support RSA or ECDsa.", null);
            }

            signature.initSign(privateKey);
            signature.update(data);
            byte[] signedBytes = signature.sign();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signedBytes);
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new VerificationLinkGenerationException("Cannot compute signature", e);
        }
    }

}
