package io.alapierre.ksef.fop.i18n;

import io.alapierre.ksef.fop.internal.TemplateResolver;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TranslationServiceTest {

    @TempDir
    Path tempDir;

    // -----------------------------------------------------------------------
    // Built-in translations (no overrides)
    // -----------------------------------------------------------------------

    @Test
    void getTranslation_shouldReturnBuiltInPolishValue() {
        assertEquals("Numer faktury", new TranslationService().getTranslation("pl", "invoice.number"));
    }

    @Test
    void getTranslation_shouldReturnBuiltInEnglishValue() {
        assertEquals("Invoice Number", new TranslationService().getTranslation("en", "invoice.number"));
    }

    @Test
    void getTranslation_shouldReturnKeyWhenKeyNotFound() {
        assertEquals("nonexistent.key", new TranslationService().getTranslation("pl", "nonexistent.key"));
    }

    @Test
    void getTranslation_shouldFallBackToBaseLocaleWhenLanguageUnknown() {
        // Polish labels are the base file i18n/labels.xml (Locale.ROOT).
        assertEquals("Numer faktury", new TranslationService().getTranslation("fr", "invoice.number"));
    }

    @Test
    void getTranslation_shouldDefaultLangToPolishWhenNullOrBlank() {
        TranslationService svc = new TranslationService();
        assertEquals("Numer faktury", svc.getTranslation(null, "invoice.number"));
        assertEquals("Numer faktury", svc.getTranslation("", "invoice.number"));
        assertEquals("Numer faktury", svc.getTranslation("   ", "invoice.number"));
    }

    @Test
    void getTranslation_shouldHandleLocaleVariantsViaCandidateFallback() {
        // en_US does not have its own file; should fall back to en and find the English value.
        assertEquals("Invoice Number", new TranslationService().getTranslation("en-US", "invoice.number"));
    }

    // -----------------------------------------------------------------------
    // Document shape & caching
    // -----------------------------------------------------------------------

    @Test
    void getTranslationsAsXml_shouldReturnLabelsRootWithEntries() {
        Document doc = new TranslationService().getTranslationsAsXml("pl");
        assertEquals("labels", doc.getDocumentElement().getNodeName());
        NodeList entries = doc.getElementsByTagName("entry");
        assertTrue(entries.getLength() > 0);

        Element first = (Element) entries.item(0);
        assertTrue(first.hasAttribute("key"));
        assertNotNull(first.getTextContent());
    }

    @Test
    void getTranslationsAsXml_shouldCacheByLanguagePerInstance() {
        TranslationService svc = new TranslationService();
        assertSame(svc.getTranslationsAsXml("pl"), svc.getTranslationsAsXml("pl"));
    }

    @Test
    void getTranslationsAsXml_shouldDistinguishLanguages() {
        TranslationService svc = new TranslationService();
        assertNotSame(svc.getTranslationsAsXml("pl"), svc.getTranslationsAsXml("en"));
    }

    @Test
    void getTranslationsAsXml_shouldDefaultLangToPolishWhenBlank() {
        Document doc = new TranslationService().getTranslationsAsXml("");
        assertEquals("labels", doc.getDocumentElement().getNodeName());
        assertTrue(doc.getElementsByTagName("entry").getLength() > 0);
    }

    // -----------------------------------------------------------------------
    // Candidate resource names (inline locale fallback, no ResourceBundle)
    // -----------------------------------------------------------------------

    @Test
    void candidateResourceNames_shouldOrderFromSpecificToBase() {
        List<String> names = TranslationService.candidateResourceNames("en_US");
        assertEquals(
                Arrays.asList("/i18n/labels_en_US.xml", "/i18n/labels_en.xml", "/i18n/labels.xml"),
                names);
    }

    @Test
    void candidateResourceNames_shouldWorkForPlainLanguage() {
        assertEquals(
                Arrays.asList("/i18n/labels_en.xml", "/i18n/labels.xml"),
                TranslationService.candidateResourceNames("en"));
    }

    // -----------------------------------------------------------------------
    // Filesystem-root overrides (exercised via TemplateResolver)
    // -----------------------------------------------------------------------

    @Test
    void getTranslation_withRoot_shouldReturnOverriddenValue() throws IOException, TransformerException {
        Path root = writeLabels(tempDir, "labels_en.xml", "seller", "From-Root");
        TranslationService svc = serviceWithRoots(root);
        assertEquals("From-Root", svc.getTranslation("en", "seller"));
    }

    @Test
    void getTranslation_withRoot_shouldFallBackToBaseForMissingKey() throws IOException, TransformerException {
        // Partial English override — missing keys must fall back to the base (Polish) file.
        Path root = writeLabels(tempDir, "labels_en.xml", "seller", "From-Root");
        TranslationService svc = serviceWithRoots(root);
        assertEquals("From-Root", svc.getTranslation("en", "seller"));
        // Not overridden and English file didn't supply it, so it falls through to labels.xml (Polish).
        assertEquals("Numer faktury", svc.getTranslation("en", "invoice.number"));
    }

    @Test
    void getTranslation_withRoot_shouldFallBackToClasspathForLocaleWithoutOverrideFile() throws IOException, TransformerException {
        // Only an English override file present; Polish lookups come from the built-in classpath file.
        Path root = writeLabels(tempDir, "labels_en.xml", "seller", "From-Root");
        TranslationService svc = serviceWithRoots(root);
        assertEquals("Sprzedawca", svc.getTranslation("pl", "seller"));
    }

    @Test
    void getTranslation_withRoot_shouldAllowNewKeys() throws IOException, TransformerException {
        Path root = writeLabels(tempDir, "labels.xml", "custom.key", "Własna wartość");
        TranslationService svc = serviceWithRoots(root);
        assertEquals("Własna wartość", svc.getTranslation("pl", "custom.key"));
    }

    @Test
    void getTranslationsAsXml_withRoot_shouldMergeOverridesAndBuiltIns() throws IOException, TransformerException {
        Path root = writeLabels(tempDir, "labels_en.xml",
                "seller", "From-Root",
                "custom.key", "Custom value");
        TranslationService svc = serviceWithRoots(root);

        Document doc = svc.getTranslationsAsXml("en");
        assertEquals("From-Root", findEntryValue(doc, "seller"));
        // invoice.number not in English override → falls through to base (Polish) file.
        assertEquals("Numer faktury", findEntryValue(doc, "invoice.number"));
        assertEquals("Custom value", findEntryValue(doc, "custom.key"));
    }

    @Test
    void getTranslation_withRoot_shouldHonourRootsOrder() throws IOException, TransformerException {
        Path rootA = tempDir.resolve("a");
        Path rootB = tempDir.resolve("b");
        writeLabels(rootA, "labels_en.xml", "seller", "Seller-A");
        writeLabels(rootB, "labels_en.xml", "seller", "Seller-B");

        TranslationService svc = serviceWithRoots(rootA, rootB);
        assertEquals("Seller-A", svc.getTranslation("en", "seller"));
    }

    @Test
    void getTranslation_withEmptyRoot_shouldBehaveLikeClasspathOnly() throws IOException, TransformerException {
        Path empty = Files.createDirectories(tempDir.resolve("empty"));
        TranslationService svc = serviceWithRoots(empty);
        assertEquals("Invoice Number", svc.getTranslation("en", "invoice.number"));
    }

    // -----------------------------------------------------------------------
    // Security / containment — roots fall through the shared TemplateResolver
    // -----------------------------------------------------------------------

    @Test
    void getTranslation_withSymlinkEscape_shouldNotLeakAndFallBackToClasspath() throws IOException, TransformerException {
        Path outside = Files.createDirectories(tempDir.resolve("outside"));
        Path secret = outside.resolve("labels_en.xml");
        writeLabelsFile(secret, "seller", "Compromised");

        Path root = tempDir.resolve("root");
        Path i18n = Files.createDirectories(root.resolve("i18n"));

        boolean symlinkCreated = false;
        try {
            Files.createSymbolicLink(i18n.resolve("labels_en.xml"), secret);
            symlinkCreated = true;
        } catch (UnsupportedOperationException | IOException e) {
            // Symlinks may be unsupported (e.g. Windows without privilege); skip.
        }
        Assumptions.assumeTrue(symlinkCreated, "Symlinks not supported in this environment");

        TranslationService svc = serviceWithRoots(root);
        // Symlink escape rejected by TemplateResolver → classpath built-in kicks in.
        assertEquals("Seller", svc.getTranslation("en", "seller"));
    }

    // -----------------------------------------------------------------------
    // resolveLocaleLabelPath — used by PdfGenerator to wire XSLT params
    // -----------------------------------------------------------------------

    @Test
    void resolveLocaleLabelPath_shouldReturnLocaleFileWhenAvailable() {
        assertEquals("/i18n/labels_en.xml", new TranslationService().resolveLocaleLabelPath("en"));
    }

    @Test
    void resolveLocaleLabelPath_shouldReturnBasePathWhenLocaleMissing() {
        assertEquals(TranslationService.LABELS_BASE_PATH,
                new TranslationService().resolveLocaleLabelPath("fr"));
    }

    @Test
    void resolveLocaleLabelPath_shouldHonourRootOverrides() throws IOException, TransformerException {
        Path root = writeLabels(tempDir, "labels_fr.xml", "seller", "Vendeur");
        TranslationService svc = serviceWithRoots(root);
        // French file now exists in the override root, so it wins over the base fallback.
        assertEquals("/i18n/labels_fr.xml", svc.resolveLocaleLabelPath("fr"));
    }

    @Test
    void resolveLocaleLabelPath_shouldDefaultToBasePathForBlankLanguage() {
        TranslationService svc = new TranslationService();
        assertEquals(TranslationService.LABELS_BASE_PATH, svc.resolveLocaleLabelPath(""));
        assertEquals(TranslationService.LABELS_BASE_PATH, svc.resolveLocaleLabelPath(null));
    }

    // -----------------------------------------------------------------------
    // Constructor validation
    // -----------------------------------------------------------------------

    @Test
    void constructor_shouldRejectNullResolver() {
        assertThrows(NullPointerException.class, () -> new TranslationService(null));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static TranslationService serviceWithRoots(Path... roots) throws TransformerException {
        return new TranslationService(new TemplateResolver(Arrays.asList(roots)));
    }

    /**
     * Writes {@code i18n/<fileName>} with the given key/value pairs under {@code rootDir}
     * (which will be created if absent) and returns the root directory.
     */
    private static Path writeLabels(Path rootDir, String fileName, String... kvPairs) throws IOException {
        Files.createDirectories(rootDir.resolve("i18n"));
        Path file = rootDir.resolve("i18n").resolve(fileName);
        writeLabelsFile(file, kvPairs);
        return rootDir;
    }

    private static void writeLabelsFile(Path file, String... kvPairs) throws IOException {
        if (kvPairs.length % 2 != 0) {
            throw new IllegalArgumentException("expected key/value pairs");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<labels>\n");
        for (int i = 0; i < kvPairs.length; i += 2) {
            sb.append("  <entry key=\"").append(kvPairs[i]).append("\">")
                    .append(kvPairs[i + 1]).append("</entry>\n");
        }
        sb.append("</labels>\n");
        Files.createDirectories(file.getParent());
        Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String findEntryValue(Document doc, String key) {
        NodeList entries = doc.getElementsByTagName("entry");
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            if (key.equals(entry.getAttribute("key"))) {
                return entry.getTextContent();
            }
        }
        return null;
    }

    /** Silences unused-symbol warnings for helpers currently untouched. */
    @SuppressWarnings("unused")
    private static List<Path> asList(Path... p) { return Collections.unmodifiableList(Arrays.asList(p)); }
}
