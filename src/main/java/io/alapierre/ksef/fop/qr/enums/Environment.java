package io.alapierre.ksef.fop.qr.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Environment {
    DEMO("https://ksef-demo.mf.gov.pl"),
    PROD("https://ksef.mf.gov.pl"),
    TEST("https://ksef-test.mf.gov.pl");

    private final String url;
}
