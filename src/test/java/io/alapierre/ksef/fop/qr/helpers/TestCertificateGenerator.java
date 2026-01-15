package io.alapierre.ksef.fop.qr.helpers;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class TestCertificateGenerator {
    private static final String EC = "EC";
    private static final String SHA_256_WITH_ECDSA = "SHA256withECDSA";
    private static final String BC = "BC";

    public SelfSignedCertificate generateSelfSignedCertificateEcdsa(CertificateBuilders.X500NameHolder x500Name) {

        return generateSelfSignedCertificate(x500Name.getX500Name());
    }

    private SelfSignedCertificate generateSelfSignedCertificate(X500Name x500Name) {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(TestCertificateGenerator.EC);
            keyPairGenerator.initialize(256);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            Instant now = Instant.now();
            Date notBefore = Date.from(now);
            Date notAfter = Date.from(now.plus(365, ChronoUnit.DAYS));

            BigInteger certSerialNumber = new BigInteger(Long.toString(System.currentTimeMillis()));

            JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                    x500Name,
                    certSerialNumber,
                    notBefore,
                    notAfter,
                    x500Name,
                    keyPair.getPublic()
            );

            ContentSigner contentSigner = new JcaContentSignerBuilder(TestCertificateGenerator.SHA_256_WITH_ECDSA)
                    .setProvider(BC)
                    .build(keyPair.getPrivate());

            X509CertificateHolder certHolder = certBuilder.build(contentSigner);

            JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter()
                    .setProvider(BC);
            X509Certificate certificate = certConverter.getCertificate(certHolder);

            return new SelfSignedCertificate(certificate, keyPair);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
