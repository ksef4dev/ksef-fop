package io.alapierre.ksef.fop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UpoGenerationParams {

    @NotNull
    private UpoSchema schema;

    /**
     * @deprecated use {@link #languageLocale} instead, which accepts any BCP&nbsp;47
     * language tag (e.g. {@code "en-US"}, {@code "uk"}, {@code "ar-SA"}) and is not
     * limited to the values defined by this enum. Kept for backward compatibility.
     * When both are set, {@link #languageLocale} wins (see {@link #resolveLanguageTag()}).
     */
    @Deprecated
    private Language language;

    /**
     * Optional BCP&nbsp;47 language tag used to select the label file for translations
     * (e.g. {@code "en"}, {@code "en-US"}, {@code "uk"}, {@code "ar-SA"}). Both
     * {@code _} and {@code -} separators are accepted. Unknown tags fall back to the
     * default language ({@link Language#DEFAULT_LANGUAGE_TAG}) without raising an error.
     *
     * <p>When set, this value takes precedence over the deprecated {@link #language} enum.</p>
     */
    @Nullable
    private String languageLocale;

    /**
     * Optional classpath-relative path to a custom XSLT UPO template.
     * When set, overrides the schema-derived default template path.
     */
    @Nullable
    private String templatePath;

    /**
     * Ordered list of filesystem directories searched before the classpath when resolving templates.
     */
    private List<Path> templateRoots;

    /**
     * @deprecated use the builder instead.
     */
    @Deprecated
    public UpoGenerationParams() {
        this(builder().schema(UpoSchema.UPO_V4_3));
    }

    /**
     * @deprecated use the builder instead.
     */
    @Deprecated
    public UpoGenerationParams(@NotNull UpoSchema schema, Language language) {
        this(builder().schema(schema).language(language));
    }

    private UpoGenerationParams(UpoGenerationParamsBuilder builder) {
        this.schema = Objects.requireNonNull(builder.schema, "schema");
        this.language = builder.language == null ? Language.PL : builder.language;
        this.languageLocale = builder.languageLocale;
        this.templatePath = builder.templatePath;
        this.templateRoots = builder.templateRoots == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(builder.templateRoots));
    }

    @NotNull
    public UpoSchema getSchema() {
        return schema;
    }

    public void setSchema(@NotNull UpoSchema schema) {
        this.schema = Objects.requireNonNull(schema, "schema");
    }

    /**
     * @deprecated use {@link #languageLocale} instead, which accepts any BCP&nbsp;47
     * language tag (e.g. {@code "en-US"}, {@code "uk"}, {@code "ar-SA"}) and is not
     * limited to the values defined by this enum. Kept for backward compatibility.
     * When both are set, {@link #languageLocale} wins (see {@link #resolveLanguageTag()}).
     */
    @Deprecated
    public Language getLanguage() {
        return language;
    }

    /**
     * @deprecated use {@link #languageLocale} instead.
     */
    @Deprecated
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * Optional BCP&nbsp;47 language tag used to select the label file for translations
     * (e.g. {@code "en"}, {@code "en-US"}, {@code "uk"}, {@code "ar-SA"}). Both
     * {@code _} and {@code -} separators are accepted. Unknown tags fall back to the
     * default language ({@link Language#DEFAULT_LANGUAGE_TAG}) without raising an error.
     *
     * <p>When set, this value takes precedence over the deprecated {@link #language} enum.</p>
     */
    @Nullable
    public String getLanguageLocale() {
        return languageLocale;
    }

    public void setLanguageLocale(@Nullable String languageLocale) {
        this.languageLocale = languageLocale;
    }

    /**
     * Optional classpath-relative path to a custom XSLT UPO template.
     * When set, overrides the schema-derived default template path.
     */
    @Nullable
    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(@Nullable String templatePath) {
        this.templatePath = templatePath;
    }

    /**
     * Returns an unmodifiable view of the configured filesystem template roots.
     */
    public List<Path> getTemplateRoots() {
        return templateRoots;
    }

    /**
     * Resolves the effective language tag used for label lookups, in order of precedence:
     * <ol>
     *   <li>{@link #languageLocale} (BCP&nbsp;47 tag, trimmed; blank values are ignored),</li>
     *   <li>{@link #language} ({@code Language} enum, using Language.getCode()),</li>
     *   <li>{@link Language#DEFAULT_LANGUAGE_TAG}.</li>
     * </ol>
     *
     * <p>This is the single source of truth consumed by the rendering pipeline;
     * callers should not inspect {@link #languageLocale} or {@link #language} directly.</p>
     */
    @NotNull
    public String resolveLanguageTag() {
        if (languageLocale != null) {
            String trimmed = languageLocale.trim();
            if (!trimmed.isEmpty()) return trimmed;
        }
        if (language != null) return language.getCode();
        return Language.DEFAULT_LANGUAGE_TAG;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof UpoGenerationParams) {
            UpoGenerationParams other = (UpoGenerationParams) o;
            return other.canEqual(this)
                    && Objects.equals(schema, other.schema)
                    && Objects.equals(language, other.language)
                    && Objects.equals(languageLocale, other.languageLocale)
                    && Objects.equals(templatePath, other.templatePath)
                    && Objects.equals(getTemplateRoots(), other.getTemplateRoots());
        }
        return false;
    }

    protected boolean canEqual(Object other) {
        return other instanceof UpoGenerationParams;
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, language, languageLocale, templatePath, getTemplateRoots());
    }

    @Override
    public String toString() {
        return "UpoGenerationParams(schema=" + schema
                + ", language=" + language
                + ", languageLocale=" + languageLocale
                + ", templatePath=" + templatePath
                + ", templateRoots=" + getTemplateRoots() + ")";
    }

    public static UpoGenerationParamsBuilder builder() {
        return new UpoGenerationParamsBuilder();
    }

    public static final class UpoGenerationParamsBuilder {

        private UpoSchema schema;
        private Language language = Language.PL;
        private String languageLocale;
        private String templatePath;
        private ArrayList<Path> templateRoots;

        UpoGenerationParamsBuilder() {
        }

        public UpoGenerationParamsBuilder schema(@NotNull UpoSchema schema) {
            this.schema = Objects.requireNonNull(schema, "schema");
            return this;
        }

        /**
         * @deprecated use {@link #languageLocale(String)} instead.
         */
        @Deprecated
        public UpoGenerationParamsBuilder language(Language language) {
            this.language = language;
            return this;
        }

        public UpoGenerationParamsBuilder languageLocale(@Nullable String languageLocale) {
            this.languageLocale = languageLocale;
            return this;
        }

        public UpoGenerationParamsBuilder templatePath(@Nullable String templatePath) {
            this.templatePath = templatePath;
            return this;
        }

        public UpoGenerationParamsBuilder templateRoot(Path templateRoot) {
            if (this.templateRoots == null) this.templateRoots = new ArrayList<>();
            this.templateRoots.add(templateRoot);
            return this;
        }

        public UpoGenerationParamsBuilder templateRoots(Collection<? extends Path> templateRoots) {
            if (templateRoots == null) {
                throw new NullPointerException("templateRoots cannot be null");
            }
            if (this.templateRoots == null) this.templateRoots = new ArrayList<>();
            this.templateRoots.addAll(templateRoots);
            return this;
        }

        public UpoGenerationParamsBuilder clearTemplateRoots() {
            if (this.templateRoots != null) this.templateRoots.clear();
            return this;
        }

        public UpoGenerationParams build() {
            return new UpoGenerationParams(this);
        }

        @Override
        public String toString() {
            return "UpoGenerationParams.UpoGenerationParamsBuilder(schema=" + schema
                    + ", language=" + language
                    + ", languageLocale=" + languageLocale
                    + ", templatePath=" + templatePath
                    + ", templateRoots=" + templateRoots + ")";
        }
    }
}
