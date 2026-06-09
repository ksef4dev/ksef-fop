package io.alapierre.ksef.fop;

/**
 * Built-in language codes recognised by the library.
 *
 * <p>The enum covers the two translations shipped with the library
 * ({@code pl}, {@code en}). For any other language use
 * {@code InvoiceGenerationParams#languageLocale} (a BCP&nbsp;47 tag such
 * as {@code "uk"} or {@code "ar-SA"}) — the resolution logic accepts arbitrary
 * tags and falls back to this default when no label file is available.</p>
 */
public enum Language {
    PL("pl"),
    EN("en");

    /**
     * Language tag used whenever no explicit language or locale is supplied.
     * Kept in sync with {@link #PL}.
     */
    public static final String DEFAULT_LANGUAGE_TAG = "pl";

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
