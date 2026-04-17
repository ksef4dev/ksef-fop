package io.alapierre.ksef.fop;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpoGenerationParams {

    @NotNull
    private UpoSchema schema;

    @Builder.Default
    private Language language = Language.PL;

    /**
     * Optional classpath-relative path to a custom XSLT UPO template.
     * When set, overrides the schema-derived default template path.
     */
    @Nullable
    private String templatePath;

    /**
     * Ordered list of filesystem directories searched before the classpath when resolving templates.
     */
    @Singular("templateRoot")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<Path> templateRoots;

    /**
     * @deprecated use the builder instead.
     */
    @Deprecated
    public UpoGenerationParams(@NotNull UpoSchema schema, Language language) {
        this.schema = schema;
        this.language = language != null ? language : Language.PL;
        this.templatePath = null;
        this.templateRoots = Collections.emptyList();
    }

    /**
     * Returns an unmodifiable view of the configured filesystem template roots.
     */
    public List<Path> getTemplateRoots() {
        if (templateRoots == null) return Collections.emptyList();
        return Collections.unmodifiableList(templateRoots);
    }
}
