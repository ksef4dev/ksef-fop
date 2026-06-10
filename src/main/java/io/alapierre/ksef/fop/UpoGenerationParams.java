package io.alapierre.ksef.fop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Parameters controlling how a KSeF UPO (Official Receipt Confirmation) is rendered to PDF.
 */
public class UpoGenerationParams {

    @NotNull
    private UpoSchema schema;

    private Language language;

    @Nullable
    private String languageLocale;

    @Nullable
    private String templatePath;

    private final List<Path> templateRoots;

    /**
     * @deprecated use the builder instead.
     */
    @Deprecated
    public UpoGenerationParams() {
        this(builder().schema(UpoSchema.UPO_V4_3));
    }

    /**
     * @param schema the UPO schema to render
     * @param language the label language
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
        this.templateRoots = builder.templateRoots.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(builder.templateRoots));
    }

    /**
     * Returns the UPO schema to render.
     * @return the UPO schema
     */
    @NotNull
    public UpoSchema getSchema() {
        return schema;
    }

    /**
     * Sets the UPO schema to render.
     * @param schema the UPO schema, never {@code null}
     */
    public void setSchema(@NotNull UpoSchema schema) {
        this.schema = Objects.requireNonNull(schema, "schema");
    }

    /**
     * @return the label language
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
     * @param language the label language
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
     *
     * @return the BCP&nbsp;47 language tag, or {@code null} if unset
     */
    @Nullable
    public String getLanguageLocale() {
        return languageLocale;
    }

    /**
     * Sets the BCP&nbsp;47 language tag used to select the label file.
     * @param languageLocale the BCP&nbsp;47 language tag, or {@code null} to unset
     */
    public void setLanguageLocale(@Nullable String languageLocale) {
        this.languageLocale = languageLocale;
    }

    /**
     * Optional classpath-relative path to a custom XSLT UPO template.
     * When set, overrides the schema-derived default template path.
     * @return the custom template path, or {@code null} to use the schema default
     */
    @Nullable
    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * Sets the classpath-relative path to a custom XSLT UPO template.
     * @param templatePath the custom template path, or {@code null} to use the schema default
     */
    public void setTemplatePath(@Nullable String templatePath) {
        this.templatePath = templatePath;
    }

    /**
     * Returns an unmodifiable view of the configured filesystem template roots.
     * @return the template roots, never {@code null}
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
     *
     * @return the resolved BCP&nbsp;47 language tag, never {@code null}
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

    /**
     * Tells whether {@code other} may be compared for equality with this instance.
     * @param other the object to test
     * @return {@code true} if {@code other} is a {@code UpoGenerationParams}
     */
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

    /**
     * Creates a new builder for {@link UpoGenerationParams}.
     * @return a fresh builder
     */
    public static UpoGenerationParamsBuilder builder() {
        return new UpoGenerationParamsBuilder();
    }

    /**
     * Fluent builder for {@link UpoGenerationParams}.
     */
    public static final class UpoGenerationParamsBuilder {

        private UpoSchema schema;
        private Language language = Language.PL;
        private String languageLocale;
        private String templatePath;
        private final ArrayList<Path> templateRoots = new ArrayList<>();

        UpoGenerationParamsBuilder() {
        }

        /**
         * Sets the UPO schema to render.
         * @param schema the UPO schema
         * @return this builder
         */
        public UpoGenerationParamsBuilder schema(@NotNull UpoSchema schema) {
            this.schema = schema;
            return this;
        }

        /**
         * @param language the label language
         * @return this builder
         * @deprecated use {@link #languageLocale(String)} instead.
         */
        @Deprecated
        public UpoGenerationParamsBuilder language(Language language) {
            this.language = language;
            return this;
        }

        /**
         * Sets the BCP&nbsp;47 language tag used to select the label file.
         * @param languageLocale the BCP&nbsp;47 language tag, or {@code null} to unset
         * @return this builder
         */
        public UpoGenerationParamsBuilder languageLocale(@Nullable String languageLocale) {
            this.languageLocale = languageLocale;
            return this;
        }

        /**
         * Sets the classpath-relative path to a custom XSLT UPO template.
         * @param templatePath the custom template path, or {@code null} to use the schema default
         * @return this builder
         */
        public UpoGenerationParamsBuilder templatePath(@Nullable String templatePath) {
            this.templatePath = templatePath;
            return this;
        }

        /**
         * Appends a single filesystem template root searched before the classpath.
         * @param templateRoot the directory to append
         * @return this builder
         */
        public UpoGenerationParamsBuilder templateRoot(Path templateRoot) {
            this.templateRoots.add(templateRoot);
            return this;
        }

        /**
         * Appends a collection of filesystem template roots searched before the classpath.
         * A {@code null} collection is ignored.
         * @param templateRoots the directories to append, or {@code null}
         * @return this builder
         */
        public UpoGenerationParamsBuilder templateRoots(Collection<? extends Path> templateRoots) {
            if (templateRoots != null) {
                this.templateRoots.addAll(templateRoots);
            }
            return this;
        }

        /**
         * Removes all template roots accumulated so far.
         * @return this builder
         */
        public UpoGenerationParamsBuilder clearTemplateRoots() {
            this.templateRoots.clear();
            return this;
        }

        /**
         * Builds an immutable {@link UpoGenerationParams} from this builder's state.
         * @return the configured parameters
         */
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