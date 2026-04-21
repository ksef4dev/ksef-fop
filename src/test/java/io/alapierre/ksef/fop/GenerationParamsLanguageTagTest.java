package io.alapierre.ksef.fop;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the language-tag resolution contract shared by
 * {@link InvoiceGenerationParams} and {@link UpoGenerationParams}.
 *
 * <p>Precedence: {@code languageLocale} (BCP&nbsp;47 tag, trimmed, blank ignored)
 * &gt; {@code language} enum &gt; {@link Language#DEFAULT_LANGUAGE_TAG}.</p>
 */
class GenerationParamsLanguageTagTest {

    @Nested
    class InvoiceParams {

        @Test
        void returnsDefault_whenNothingSet() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .language(null)
                    .build();

            assertEquals(Language.DEFAULT_LANGUAGE_TAG, params.resolveLanguageTag());
        }

        @Test
        void returnsDefault_forFreshNoArgsInstance() {
            InvoiceGenerationParams params = new InvoiceGenerationParams();

            assertEquals(Language.DEFAULT_LANGUAGE_TAG, params.resolveLanguageTag());
        }

        @Test
        void returnsEnumCode_whenOnlyLanguageSet() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .language(Language.EN)
                    .build();

            assertEquals("en", params.resolveLanguageTag());
        }

        @Test
        void returnsLocaleTag_whenOnlyLocaleSet() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .language(null)
                    .languageLocale("uk")
                    .build();

            assertEquals("uk", params.resolveLanguageTag());
        }

        @Test
        void localeWinsOverEnum_whenBothSet() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .language(Language.EN)
                    .languageLocale("pl")
                    .build();

            assertEquals("pl", params.resolveLanguageTag());
        }

        @Test
        void passesThroughBcp47Tag() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .languageLocale("ar-SA")
                    .build();

            assertEquals("ar-SA", params.resolveLanguageTag());
        }

        @Test
        void trimsWhitespaceAroundLocale() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .language(null)
                    .languageLocale("  en-US  ")
                    .build();

            assertEquals("en-US", params.resolveLanguageTag());
        }

        @Test
        void fallsBackToEnum_whenLocaleIsEmpty() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .language(Language.EN)
                    .languageLocale("")
                    .build();

            assertEquals("en", params.resolveLanguageTag());
        }

        @Test
        void fallsBackToEnum_whenLocaleIsBlank() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .language(Language.EN)
                    .languageLocale("   ")
                    .build();

            assertEquals("en", params.resolveLanguageTag());
        }

        @Test
        void fallsBackToDefault_whenLocaleBlankAndEnumNull() {
            InvoiceGenerationParams params = InvoiceGenerationParams.builder()
                    .schema(InvoiceSchema.FA3_1_0_E)
                    .language(null)
                    .languageLocale("   ")
                    .build();

            assertEquals(Language.DEFAULT_LANGUAGE_TAG, params.resolveLanguageTag());
        }
    }

    @Nested
    class UpoParams {

        @Test
        void returnsDefault_whenNothingSet() {
            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_3)
                    .language(null)
                    .build();

            assertEquals(Language.DEFAULT_LANGUAGE_TAG, params.resolveLanguageTag());
        }

        @Test
        void returnsEnumCode_whenOnlyLanguageSet() {
            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_3)
                    .language(Language.EN)
                    .build();

            assertEquals("en", params.resolveLanguageTag());
        }

        @Test
        void localeWinsOverEnum_whenBothSet() {
            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_3)
                    .language(Language.EN)
                    .languageLocale("uk")
                    .build();

            assertEquals("uk", params.resolveLanguageTag());
        }

        @Test
        void trimsWhitespaceAroundLocale() {
            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_3)
                    .language(null)
                    .languageLocale("  en-US  ")
                    .build();

            assertEquals("en-US", params.resolveLanguageTag());
        }

        @Test
        void fallsBackToEnum_whenLocaleIsBlank() {
            UpoGenerationParams params = UpoGenerationParams.builder()
                    .schema(UpoSchema.UPO_V4_3)
                    .language(Language.EN)
                    .languageLocale("   ")
                    .build();

            assertEquals("en", params.resolveLanguageTag());
        }
    }
}
