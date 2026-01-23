package io.alapierre.ksef.fop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
    PL("pl"),
    EN("en");

    private final String code;
}

