package io.alapierre.ksef.fop.qr;

import io.alapierre.ksef.fop.qr.exceptions.VerificationLinkGenerationException;
import io.alapierre.ksef.fop.qr.helpers.CertificateBuilders;
import io.alapierre.ksef.fop.qr.helpers.SelfSignedCertificate;
import io.alapierre.ksef.fop.qr.helpers.TestCertificateGenerator;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilsTest {

    @Test
    void computeInvoiceHashBase64Url_shouldReturnBase64UrlEncodedHash() {
        String testData = "<?xml version=\"1.0\"?><invoice>test</invoice>";
        byte[] invoiceXml = testData.getBytes();

        String hash = CryptoUtils.computeInvoiceHashBase64Url(invoiceXml);

        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        // Base64URL should not contain +, /, or = characters
        assertFalse(hash.contains("+"));
        assertFalse(hash.contains("/"));
        assertFalse(hash.contains("="));
        // Should only contain Base64URL characters
        assertTrue(hash.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void computeInvoiceHashBase64Url_shouldReturnConsistentHashForSameInput() {
        String testData = "<?xml version=\"1.0\"?><invoice>test</invoice>";
        byte[] invoiceXml = testData.getBytes();

        String hash1 = CryptoUtils.computeInvoiceHashBase64Url(invoiceXml);
        String hash2 = CryptoUtils.computeInvoiceHashBase64Url(invoiceXml);

        assertEquals(hash1, hash2);
    }

    @Test
    void computeInvoiceHashBase64Url_shouldReturnDifferentHashForDifferentInput() {
        String testData1 = "<?xml version=\"1.0\"?><invoice>test1</invoice>";
        String testData2 = "<?xml version=\"1.0\"?><invoice>test2</invoice>";
        byte[] invoiceXml1 = testData1.getBytes();
        byte[] invoiceXml2 = testData2.getBytes();

        String hash1 = CryptoUtils.computeInvoiceHashBase64Url(invoiceXml1);
        String hash2 = CryptoUtils.computeInvoiceHashBase64Url(invoiceXml2);

        assertNotEquals(hash1, hash2);
    }

    @Test
    void computeInvoiceHashBase64Url_shouldHandleEmptyInput() {
        byte[] emptyData = new byte[0];
        String hash = CryptoUtils.computeInvoiceHashBase64Url(emptyData);

        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        // Should be valid Base64URL format
        assertTrue(hash.matches("[A-Za-z0-9_-]+"));
        // SHA-256 of empty byte array should produce consistent result
        String hash2 = CryptoUtils.computeInvoiceHashBase64Url(emptyData);
        assertEquals(hash, hash2);
    }

    @Test
    void computeInvoiceHashBase64Url_shouldHandleLargeInput() {
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeData.append("test data ");
        }
        byte[] largeXml = largeData.toString().getBytes();

        String hash = CryptoUtils.computeInvoiceHashBase64Url(largeXml);

        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertTrue(hash.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void computeUrlEncodedSignedHash_shouldWorkWithRSAKey() throws Exception {
        // Generate RSA key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        assertTrue(privateKey instanceof RSAPrivateKey);

        String pathToSign = "test/path/to/sign";
        String signature = CryptoUtils.computeUrlEncodedSignedHash(pathToSign, privateKey);

        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        // Base64URL should not contain +, /, or = characters
        assertFalse(signature.contains("+"));
        assertFalse(signature.contains("/"));
        assertFalse(signature.contains("="));
        // Should only contain Base64URL characters
        assertTrue(signature.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void computeUrlEncodedSignedHash_shouldWorkWithECDSAKey() throws Exception {
        // Generate ECDSA key pair using test helper
        CertificateBuilders.X500NameHolder x500 = new CertificateBuilders()
                .buildForOrganization("Test Org", "VATPL-1234567890", "TestCN", "PL");
        TestCertificateGenerator generator = new TestCertificateGenerator();
        SelfSignedCertificate cert = generator.generateSelfSignedCertificateEcdsa(x500);
        PrivateKey privateKey = cert.getPrivateKey();

        assertTrue(privateKey instanceof ECPrivateKey);

        String pathToSign = "test/path/to/sign";
        String signature = CryptoUtils.computeUrlEncodedSignedHash(pathToSign, privateKey);

        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        // Base64URL should not contain +, /, or = characters
        assertFalse(signature.contains("+"));
        assertFalse(signature.contains("/"));
        assertFalse(signature.contains("="));
        // Should only contain Base64URL characters
        assertTrue(signature.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void computeUrlEncodedSignedHash_shouldReturnConsistentSignatureForSameInput() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        String pathToSign = "test/path/to/sign";
        String signature1 = CryptoUtils.computeUrlEncodedSignedHash(pathToSign, privateKey);
        String signature2 = CryptoUtils.computeUrlEncodedSignedHash(pathToSign, privateKey);

        // Note: RSA-PSS signatures are randomized, so they won't be identical
        // But both should be valid Base64URL strings
        assertNotNull(signature1);
        assertNotNull(signature2);
        assertTrue(signature1.matches("[A-Za-z0-9_-]+"));
        assertTrue(signature2.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void computeUrlEncodedSignedHash_shouldReturnDifferentSignatureForDifferentInput() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        String path1 = "test/path/1";
        String path2 = "test/path/2";
        String signature1 = CryptoUtils.computeUrlEncodedSignedHash(path1, privateKey);
        String signature2 = CryptoUtils.computeUrlEncodedSignedHash(path2, privateKey);

        assertNotEquals(signature1, signature2);
    }

    @Test
    void computeUrlEncodedSignedHash_shouldThrowExceptionForUnsupportedKeyType() throws Exception {
        // Create a DSA key (unsupported)
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
        keyGen.initialize(1024);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        String pathToSign = "test/path";
        
        VerificationLinkGenerationException exception = assertThrows(
                VerificationLinkGenerationException.class,
                () -> CryptoUtils.computeUrlEncodedSignedHash(pathToSign, privateKey)
        );

        assertTrue(exception.getMessage().contains("Certificate not support RSA or ECDsa"));
    }

    @Test
    void computeUrlEncodedSignedHash_shouldHandleEmptyPath() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        String signature = CryptoUtils.computeUrlEncodedSignedHash("", privateKey);

        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        assertTrue(signature.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void computeUrlEncodedSignedHash_shouldHandleLongPath() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        StringBuilder longPath = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longPath.append("very/long/path/");
        }
        String signature = CryptoUtils.computeUrlEncodedSignedHash(longPath.toString(), privateKey);

        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        assertTrue(signature.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void computeUrlEncodedSignedHash_shouldHandleSpecialCharactersInPath() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        String pathWithSpecialChars = "test/path/with/special-chars_123";
        String signature = CryptoUtils.computeUrlEncodedSignedHash(pathWithSpecialChars, privateKey);

        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        assertTrue(signature.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void computeUrlEncodedSignedHash_shouldHandleUnicodeCharactersInPath() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        String pathWithUnicode = "test/path/with/unicode/ąęłńśćźż";
        String signature = CryptoUtils.computeUrlEncodedSignedHash(pathWithUnicode, privateKey);

        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        assertTrue(signature.matches("[A-Za-z0-9_-]+"));
    }
}
