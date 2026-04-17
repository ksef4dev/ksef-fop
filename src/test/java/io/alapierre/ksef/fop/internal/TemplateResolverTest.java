package io.alapierre.ksef.fop.internal;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateResolverTest {

    // -----------------------------------------------------------------------
    // Filesystem root lookup
    // -----------------------------------------------------------------------

    @Test
    void findsTemplateInFirstRoot(@TempDir Path root) throws Exception {
        Path file = root.resolve("my-template.xsl");
        write(file, "<xsl/>");

        TemplateResolver resolver = new TemplateResolver(Collections.singletonList(root));
        Source source = resolver.resolve("my-template.xsl", "");

        assertThat(source).isNotNull();
        assertThat(source.getSystemId()).endsWith("my-template.xsl");
    }

    @Test
    void fallsThroughToSecondRootWhenFirstMisses(@TempDir Path root1, @TempDir Path root2) throws Exception {
        Path file = root2.resolve("found-in-second.xsl");
        write(file, "<xsl/>");

        TemplateResolver resolver = new TemplateResolver(Arrays.asList(root1, root2));
        Source source = resolver.resolve("found-in-second.xsl", "");

        assertThat(source).isNotNull();
        assertThat(source.getSystemId()).endsWith("found-in-second.xsl");
    }

    @Test
    void templateInFirstRootShadowsSecondRoot(@TempDir Path root1, @TempDir Path root2) throws Exception {
        write(root1.resolve("template.xsl"), "root1");
        write(root2.resolve("template.xsl"), "root2");

        TemplateResolver resolver = new TemplateResolver(Arrays.asList(root1, root2));
        Source source = resolver.resolve("template.xsl", "");

        assertThat(source).isInstanceOf(StreamSource.class);
        StreamSource ss = (StreamSource) source;
        try (InputStream is = ss.getInputStream()) {
            String content = new String(IOUtils.toByteArray(is), StandardCharsets.UTF_8);
            assertThat(content).isEqualTo("root1");
        }
    }

    // -----------------------------------------------------------------------
    // Classpath fallback
    // -----------------------------------------------------------------------

    @Test
    void fallsBackToClasspathWhenNoRootMatches() throws Exception {
        TemplateResolver resolver = new TemplateResolver(Collections.emptyList());
        Source source = resolver.resolve("templates/fa3/ksef_invoice.xsl", "");

        assertThat(source).isNotNull();
        assertThat(source.getSystemId()).endsWith("templates/fa3/ksef_invoice.xsl");
    }

    @Test
    void emptyRootListUsesClasspathOnly() throws Exception {
        TemplateResolver resolver = new TemplateResolver(Collections.emptyList());
        Source source = resolver.resolve("templates/fa2/ksef_invoice.xsl", "");

        assertThat(source).isNotNull();
    }

    @Test
    void findsTemplateWithSpaceInName(@TempDir Path root) throws Exception {
        Path file = root.resolve("my template.xsl");
        write(file, "<xsl/>");

        TemplateResolver resolver = new TemplateResolver(Collections.singletonList(root));
        Source source = resolver.resolve("my template.xsl", "");

        assertThat(source).isNotNull();
        assertThat(source.getSystemId()).contains("my template.xsl");
    }

    // -----------------------------------------------------------------------
    // Relative include chain
    // -----------------------------------------------------------------------

    @Test
    void resolvesRelativeIncludeAgainstBase() throws Exception {
        TemplateResolver resolver = new TemplateResolver(Collections.emptyList());
        Source source = resolver.resolve("common-functions.xsl", "vfs:///templates/fa3/ksef_invoice.xsl");

        assertThat(source).isNotNull();
        assertThat(source.getSystemId()).isEqualTo("vfs:///templates/fa3/common-functions.xsl");
    }

    @Test
    void resolvesRelativeIncludeFromFilesystemRoot(@TempDir Path root) throws Exception {
        Path subDir = root.resolve("templates/custom");
        Files.createDirectories(subDir);
        write(subDir.resolve("main.xsl"), "<main/>");
        write(subDir.resolve("included.xsl"), "<included/>");
        write(root.resolve("templates").resolve("shared.xsl"), "<shared/>");

        TemplateResolver resolver = new TemplateResolver(Collections.singletonList(root));

        // First: entry point
        Source main = resolver.resolve("templates/custom/main.xsl", "");
        assertThat(main).isNotNull();

        // Second: relative include resolved against entry-point system ID
        Source included = resolver.resolve("included.xsl", "vfs:///templates/custom/main.xsl");
        assertThat(included).isNotNull();
        assertThat(included.getSystemId()).isEqualTo("vfs:///templates/custom/included.xsl");

        // Third: parent-relative include from templates/custom/ up to templates/
        Source shared = resolver.resolve("../shared.xsl", "vfs:///templates/custom/included.xsl");
        assertThat(shared).isNotNull();
        assertThat(shared.getSystemId()).isEqualTo("vfs:///templates/shared.xsl");
    }

    // -----------------------------------------------------------------------
    // Catalog / HTTP URIs
    // -----------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "http://crd.gov.pl/xml/schematy/dziedzinowe/mf/2022/01/05/eD/DefinicjeTypy/KodyKrajow_v10-0E.xsd",
            "https://example.com/KodyKrajow_v10-0E.xsd"
    })
    void resolvesKodyKrajowViaXmlCatalog(String url) throws Exception {
        TemplateResolver resolver = new TemplateResolver(Collections.emptyList());
        Source source = resolver.resolve(url, "");

        assertThat(source).isNotNull();
    }

    @Test
    void unknownHttpUriThrows() throws Exception {
        TemplateResolver resolver = new TemplateResolver(Collections.emptyList());

        assertThatThrownBy(() ->
                resolver.resolve("http://attacker.example.com/evil.xsl", ""))
                .isInstanceOf(TransformerException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "file:///etc/passwd",
            "ftp://example.com/evil.xsl",
            "data:text/xml,<xsl/>",
            "vfs:///templates/fa3/ksef_invoice.xsl"
    })
    void nonHttpSchemeInHrefThrows(String href) throws Exception {
        TemplateResolver resolver = new TemplateResolver(Collections.emptyList());

        assertThatThrownBy(() ->
                resolver.resolve(href, ""))
                .isInstanceOf(TransformerException.class);
    }

    // -----------------------------------------------------------------------
    // Security: path traversal
    // -----------------------------------------------------------------------

    @Test
    void dotDotTraversalIsRejected(@TempDir Path root) throws Exception {
        TemplateResolver resolver = new TemplateResolver(Collections.singletonList(root));

        assertThatThrownBy(() ->
                resolver.resolve("../../etc/passwd", null))
                .isInstanceOf(TransformerException.class);
    }

    @Test
    void dotDotInBaseDoesNotEscapeRoot(@TempDir Path root) throws Exception {
        // Trying to escape via a crafted base
        TemplateResolver resolver = new TemplateResolver(Collections.singletonList(root));

        assertThatThrownBy(() ->
                resolver.resolve("../../../etc/passwd", "vfs:///templates/fa3/ksef_invoice.xsl"))
                .isInstanceOf(TransformerException.class);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void symlinkEscapingRootIsRejected(@TempDir Path root, @TempDir Path outside) throws Exception {
        // Create a file outside the root
        Path target = outside.resolve("secret.xsl");
        write(target, "<secret/>");

        // Create a symlink inside root pointing outside
        Path link = root.resolve("evil.xsl");
        Files.createSymbolicLink(link, target);

        TemplateResolver resolver = new TemplateResolver(Collections.singletonList(root));

        assertThatThrownBy(() ->
                resolver.resolve("evil.xsl", ""))
                .isInstanceOf(TransformerException.class);
    }

    // -----------------------------------------------------------------------
    // Missing template
    // -----------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {"", "vfs:///templates"})
    void missingTemplateThrows(String base, @TempDir Path root) throws Exception {
        TemplateResolver resolver = new TemplateResolver(Collections.singletonList(root));

        assertThatThrownBy(() ->
                resolver.resolve("nonexistent.xsl", base))
                .isInstanceOf(TransformerException.class)
                .hasMessageContaining("nonexistent.xsl");
    }

    // -----------------------------------------------------------------------
    // Invalid base/href
    // -----------------------------------------------------------------------

    @Test
    void invalidBaseThrows() throws Exception {
        TemplateResolver resolver = new TemplateResolver(Collections.emptyList());

        assertThatThrownBy(() ->
                resolver.resolve("", "invalid URI"))
                .isInstanceOf(TransformerException.class);
    }

    @Test
    void invalidHrefThrows() throws Exception {
        TemplateResolver resolver = new TemplateResolver(Collections.emptyList());

        assertThatThrownBy(() ->
                resolver.resolve(null, ""))
                .isInstanceOf(TransformerException.class);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static void write(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void nonVfsBaseThrows() throws Exception {
        TemplateResolver resolver = new TemplateResolver(Collections.emptyList());

        assertThatThrownBy(() ->
                resolver.resolve("template.xsl", "file:///some/path/main.xsl"))
                .isInstanceOf(TransformerException.class);
    }
}
