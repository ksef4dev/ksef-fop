package io.alapierre.ksef.fop.qr.helpers;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class SelfSignedCertificate {

    private final X509Certificate certificate;
    private final KeyPair keyPair;

    public SelfSignedCertificate(X509Certificate certificate, KeyPair keyPair) {
        this.certificate = certificate;
        this.keyPair = keyPair;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
}