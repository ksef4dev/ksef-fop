package io.alapierre.ksef.fop.qr.helpers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@RequiredArgsConstructor
@Getter
public class SelfSignedCertificate {

    private final X509Certificate certificate;
    private final KeyPair keyPair;

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
}