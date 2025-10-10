package io.alapierre.ksef.fop.qr.helpers;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public record SelfSignedCertificate(X509Certificate certificate, KeyPair keyPair) {

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
}