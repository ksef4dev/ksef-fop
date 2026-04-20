package io.alapierre.ksef.fop.i18n;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TranslationServiceTest {

    private TranslationService translationService;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        translationService = new TranslationService();
        // Clear caches before each test
        clearCaches();
    }

    @Test
    void getTranslation_shouldReturnTranslationForPolish() {
        String result = translationService.getTranslation("pl", "invoice.number");
        assertEquals("Numer faktury", result);
    }

    @Test
    void getTranslation_shouldReturnTranslationForEnglish() {
        String result = translationService.getTranslation("en", "invoice.number");
        assertEquals("Invoice Number", result);
    }

    @Test
    void getTranslation_shouldReturnKeyWhenKeyNotFound() {
        String result = translationService.getTranslation("pl", "nonexistent.key");
        assertEquals("nonexistent.key", result);
    }

    @Test
    void getTranslation_shouldFallbackToPolishWhenLanguageNotFound() {
        String result = translationService.getTranslation("fr", "invoice.number");
        // Should fallback to Polish
        assertEquals("Numer faktury", result);
    }

    @Test
    void getTranslation_shouldUsePolishAsDefaultWhenLangIsNull() {
        String result = translationService.getTranslation(null, "invoice.number");
        assertEquals("Numer faktury", result);
    }

    @Test
    void getTranslation_shouldUsePolishAsDefaultWhenLangIsBlank() {
        String result = translationService.getTranslation("", "invoice.number");
        assertEquals("Numer faktury", result);
    }

    @Test
    void getTranslation_shouldUsePolishAsDefaultWhenLangIsWhitespace() {
        String result = translationService.getTranslation("   ", "invoice.number");
        assertEquals("Numer faktury", result);
    }

    @Test
    void getTranslationsAsXml_shouldReturnValidXmlDocument() {
        Document doc = translationService.getTranslationsAsXml("pl");
        assertNotNull(doc);
        assertNotNull(doc.getDocumentElement());
        assertEquals("labels", doc.getDocumentElement().getNodeName());
    }

    @Test
    void getTranslationsAsXml_shouldContainTranslationEntries() {
        Document doc = translationService.getTranslationsAsXml("pl");
        NodeList entries = doc.getElementsByTagName("entry");
        assertTrue(entries.getLength() > 0);
    }

    @Test
    void getTranslationsAsXml_shouldHaveCorrectEntryStructure() {
        Document doc = translationService.getTranslationsAsXml("pl");
        NodeList entries = doc.getElementsByTagName("entry");
        
        if (entries.getLength() > 0) {
            Element entry = (Element) entries.item(0);
            assertTrue(entry.hasAttribute("key"));
            assertNotNull(entry.getTextContent());
        }
    }

    @Test
    void getTranslationsAsXml_shouldFallbackToPolishWhenLanguageNotFound() {
        Document doc = translationService.getTranslationsAsXml("fr");
        assertNotNull(doc);
        NodeList entries = doc.getElementsByTagName("entry");
        assertTrue(entries.getLength() > 0);
    }

    @Test
    void getTranslationsAsXml_shouldUsePolishAsDefaultWhenLangIsNull() {
        Document doc = translationService.getTranslationsAsXml(null);
        assertNotNull(doc);
        assertEquals("labels", doc.getDocumentElement().getNodeName());
    }

    @Test
    void getTranslationsAsXml_shouldUsePolishAsDefaultWhenLangIsBlank() {
        Document doc = translationService.getTranslationsAsXml("");
        assertNotNull(doc);
        assertEquals("labels", doc.getDocumentElement().getNodeName());
    }

    @Test
    void getTranslationsAsXml_shouldCacheDocuments() {
        Document doc1 = translationService.getTranslationsAsXml("pl");
        Document doc2 = translationService.getTranslationsAsXml("pl");
        
        // Should return the same cached instance
        assertSame(doc1, doc2);
    }

    @Test
    void getTranslationsAsXml_shouldReturnDifferentDocumentsForDifferentLanguages() {
        Document docPl = translationService.getTranslationsAsXml("pl");
        Document docEn = translationService.getTranslationsAsXml("en");
        
        assertNotSame(docPl, docEn);
        
        // Both should be valid documents
        assertNotNull(docPl);
        assertNotNull(docEn);
    }

    @Test
    void getTranslation_shouldHandleMultipleKeys() {
        String key1 = translationService.getTranslation("pl", "invoice.number");
        String key2 = translationService.getTranslation("pl", "invoice.date");
        String key3 = translationService.getTranslation("pl", "seller");
        
        assertEquals("Numer faktury", key1);
        assertEquals("Data wystawienia", key2);
        assertEquals("Sprzedawca", key3);
    }

    @Test
    void getTranslation_shouldHandleEnglishTranslations() {
        String key1 = translationService.getTranslation("en", "invoice.number");
        String key2 = translationService.getTranslation("en", "invoice.date");
        String key3 = translationService.getTranslation("en", "seller");
        
        assertEquals("Invoice Number", key1);
        assertEquals("Issue date", key2);
        assertEquals("Seller", key3);
    }

    // --- Override tests ---

    @Test
    void getTranslation_withOverrides_shouldReturnOverriddenValue() throws IOException {
        try (TranslationService withOverrides = new TranslationService("i18n/test_overrides")) {
            assertEquals("Nadpisany Sprzedawca", withOverrides.getTranslation("pl", "seller"));
        }
    }

    @Test
    void getTranslation_withOverrides_shouldFallbackToDefaultForNonOverriddenKey() throws IOException {
        try (TranslationService withOverrides = new TranslationService("i18n/test_overrides")) {
            assertEquals("Numer faktury", withOverrides.getTranslation("pl", "invoice.number"));
        }
    }

    @Test
    void getTranslation_withOverrides_shouldReturnOverriddenValueForEnglish() throws IOException {
        try (TranslationService withOverrides = new TranslationService("i18n/test_overrides")) {
            assertEquals("Overridden Seller", withOverrides.getTranslation("en", "seller"));
        }
    }

    @Test
    void getTranslation_withOverrides_shouldFallbackToDefaultForNonOverriddenEnglishKey() throws IOException {
        try (TranslationService withOverrides = new TranslationService("i18n/test_overrides")) {
            assertEquals("Invoice Number", withOverrides.getTranslation("en", "invoice.number"));
        }
    }

    @Test
    void getTranslation_withOverrides_shouldReturnCustomKey() throws IOException {
        try (TranslationService withOverrides = new TranslationService("i18n/test_overrides")) {
            assertEquals("Własna wartość", withOverrides.getTranslation("pl", "custom.key"));
        }
    }

    @Test
    void getTranslation_withOverrides_shouldReturnKeyWhenNotFoundAnywhere() throws IOException {
        try (TranslationService withOverrides = new TranslationService("i18n/test_overrides")) {
            assertEquals("nonexistent.key", withOverrides.getTranslation("pl", "nonexistent.key"));
        }
    }

    @Test
    void getTranslation_withNonExistentOverridePath_shouldFallbackToDefaults() throws IOException {
        try (TranslationService withOverrides = new TranslationService("i18n/no_such_bundle")) {
            assertEquals("Numer faktury", withOverrides.getTranslation("pl", "invoice.number"));
            assertEquals("Invoice Number", withOverrides.getTranslation("en", "invoice.number"));
        }
    }

    @Test
    void getTranslationsAsXml_withOverrides_shouldContainOverriddenValue() throws IOException {
        clearCaches();
        try (TranslationService withOverrides = new TranslationService("i18n/test_overrides")) {
            Document doc = withOverrides.getTranslationsAsXml("pl");
            assertEquals("Nadpisany Sprzedawca", findEntryValue(doc, "seller"));
        }
    }

    @Test
    void getTranslationsAsXml_withOverrides_shouldKeepDefaultForNonOverriddenKey() throws IOException {
        clearCaches();
        try (TranslationService withOverrides = new TranslationService("i18n/test_overrides")) {
            Document doc = withOverrides.getTranslationsAsXml("pl");
            assertEquals("Numer faktury", findEntryValue(doc, "invoice.number"));
        }
    }

    @Test
    void getTranslationsAsXml_withOverrides_shouldContainCustomKey() throws IOException {
        clearCaches();
        try (TranslationService withOverrides = new TranslationService("i18n/test_overrides")) {
            Document doc = withOverrides.getTranslationsAsXml("pl");
            assertEquals("Własna wartość", findEntryValue(doc, "custom.key"));
        }
    }

    @Test
    void getTranslationsAsXml_withOverrides_shouldWorkForEnglish() throws IOException {
        clearCaches();
        try (TranslationService withOverrides = new TranslationService("i18n/test_overrides")) {
            Document doc = withOverrides.getTranslationsAsXml("en");
            assertEquals("Overridden Seller", findEntryValue(doc, "seller"));
            assertEquals("Invoice Number", findEntryValue(doc, "invoice.number"));
            assertEquals("Custom value", findEntryValue(doc, "custom.key"));
        }
    }

    @Test
    void getTranslation_withOverrides_multipleOverriddenKeys() throws IOException {
        try (TranslationService withOverrides = new TranslationService("i18n/test_overrides")) {
            assertEquals("Nadpisany Sprzedawca", withOverrides.getTranslation("pl", "seller"));
            assertEquals("Nadpisany Nabywca", withOverrides.getTranslation("pl", "buyer"));
        }
    }

    @Test
    void getTranslation_withNullOverridePath_shouldBehaveAsDefault() throws IOException {
        try (TranslationService withNull = new TranslationService(null)) {
            assertEquals("Sprzedawca", withNull.getTranslation("pl", "seller"));
            assertEquals("Seller", withNull.getTranslation("en", "seller"));
        }
    }

    @Test
    void getTranslation_withFileSystemRoots_shouldReturnOverriddenValue() throws IOException {
        clearCaches();
        createFileSystemOverrides();
        List<Path> translationRoots = Collections.singletonList(tempDir);
        try (TranslationService withFileOverrides = new TranslationService("custom_messages", translationRoots)) {
            assertEquals("Zewnetrzny Sprzedawca", withFileOverrides.getTranslation("pl", "seller"));
            assertEquals("External Seller", withFileOverrides.getTranslation("en", "seller"));
        }
    }

    @Test
    void getTranslation_withFileSystemRoots_shouldFallbackToDefaultForMissingLanguageFile() throws IOException {
        clearCaches();
        createFileSystemOverrides();
        List<Path> translationRoots = Collections.singletonList(tempDir);
        try (TranslationService withFileOverrides = new TranslationService("custom_messages", translationRoots)) {
            assertEquals("Zewnetrzny Sprzedawca", withFileOverrides.getTranslation("fr", "seller"));
            assertEquals("Numer faktury", withFileOverrides.getTranslation("fr", "invoice.number"));
        }
    }

    @Test
    void getTranslationsAsXml_withFileSystemRoots_shouldContainCustomAndFallbackValues() throws IOException {
        clearCaches();
        createFileSystemOverrides();
        List<Path> translationRoots = Collections.singletonList(tempDir);
        try (TranslationService withFileOverrides = new TranslationService("custom_messages", translationRoots)) {
            Document doc = withFileOverrides.getTranslationsAsXml("en");
            assertEquals("External Seller", findEntryValue(doc, "seller"));
            assertEquals("Invoice Number", findEntryValue(doc, "invoice.number"));
            assertEquals("External custom", findEntryValue(doc, "custom.key"));
        }
    }

    @Test
    void getTranslation_withFileSystemRoots_shouldFallBackToClasspathWhenRootsMissBundle() throws IOException {
        clearCaches();
        Path unrelatedRoot = tempDir.resolve("empty");
        Files.createDirectories(unrelatedRoot);
        List<Path> translationRoots = Collections.singletonList(unrelatedRoot);
        try (TranslationService withRoots = new TranslationService("i18n/test_overrides", translationRoots)) {
            assertEquals("Nadpisany Sprzedawca", withRoots.getTranslation("pl", "seller"));
            assertEquals("Overridden Seller", withRoots.getTranslation("en", "seller"));
        }
    }

    @Test
    void getTranslation_withFileSystemRoots_shouldPreferFirstRoot() throws IOException {
        clearCaches();
        Path rootA = tempDir.resolve("a");
        Path rootB = tempDir.resolve("b");
        Files.createDirectories(rootA);
        Files.createDirectories(rootB);
        Files.write(rootA.resolve("custom_messages.properties"),
                "seller=Seller-A\n".getBytes(StandardCharsets.UTF_8));
        Files.write(rootB.resolve("custom_messages.properties"),
                "seller=Seller-B\n".getBytes(StandardCharsets.UTF_8));
        List<Path> translationRoots = Arrays.asList(rootA, rootB);
        try (TranslationService withRoots = new TranslationService("custom_messages", translationRoots)) {
            assertEquals("Seller-A", withRoots.getTranslation("pl", "seller"));
        }
    }

    // --- Security / lifecycle tests ---

    @Test
    void constructor_shouldRejectBundleBaseNameWithScheme() {
        assertThrows(IllegalArgumentException.class, () -> new TranslationService("file:secret"));
    }

    @Test
    void constructor_shouldRejectAbsoluteBundleBaseName() {
        assertThrows(IllegalArgumentException.class, () -> new TranslationService("/etc/passwd"));
    }

    @Test
    void constructor_shouldRejectBundleBaseNameWithTraversalSegment() {
        assertThrows(IllegalArgumentException.class, () -> new TranslationService("../secret"));
        assertThrows(IllegalArgumentException.class, () -> new TranslationService("i18n/../secret"));
    }

    @Test
    void constructor_shouldRejectBundleBaseNameWithTrailingSeparator() {
        assertThrows(IllegalArgumentException.class, () -> new TranslationService("i18n/"));
        assertThrows(IllegalArgumentException.class, () -> new TranslationService("i18n\\"));
    }

    @Test
    void constructor_shouldRejectBundleBaseNameThatIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new TranslationService(""));
        assertThrows(IllegalArgumentException.class, () -> new TranslationService("   "));
        assertThrows(IllegalArgumentException.class, () -> new TranslationService("/"));
        assertThrows(IllegalArgumentException.class, () -> new TranslationService("./"));
    }

    @Test
    void constructor_shouldNormalizeEquivalentBundleBaseNamesToSameCacheKey() throws IOException {
        clearCaches();
        // All four spellings refer to the same bundle; after normalisation they must share
        // a single cache entry.
        String[] equivalentSpellings = {
                "i18n/test_overrides",
                "i18n//test_overrides",
                "i18n\\test_overrides",
                "i18n/./test_overrides"
        };

        for (String spelling : equivalentSpellings) {
            try (TranslationService svc = new TranslationService(spelling)) {
                // Load via getTranslationsAsXml to populate DOCUMENT_CACHE.
                assertEquals("Nadpisany Sprzedawca",
                        findEntryValue(svc.getTranslationsAsXml("pl"), "seller"),
                        "unexpected value for spelling: " + spelling);
            }
        }

        // Each spelling should resolve to the same (lang-prefixed) cache key, so the shared
        // DOCUMENT_CACHE holds at most one entry per language we have looked up (here: "pl").
        Map<String, ?> cache = getDocumentCache();
        assertEquals(1, cache.size(),
                "Equivalent bundle name spellings produced distinct cache entries: " + cache.keySet());
    }

    @Test
    void constructor_shouldRejectNonExistentRoot() {
        List<Path> roots = Collections.singletonList(tempDir.resolve("does-not-exist"));
        assertThrows(IllegalArgumentException.class,
                () -> new TranslationService("custom_messages", roots));
    }

    @Test
    void constructor_shouldRejectRootThatIsNotDirectory() throws IOException {
        Path file = tempDir.resolve("not-a-dir.txt");
        Files.write(file, new byte[]{1});
        List<Path> roots = Collections.singletonList(file);
        assertThrows(IllegalArgumentException.class,
                () -> new TranslationService("custom_messages", roots));
    }

    @Test
    void close_shouldBeNoOpWhenNoResourcesHeld() throws IOException {
        try (TranslationService svc = new TranslationService()) {
            assertEquals("Numer faktury", svc.getTranslation("pl", "invoice.number"));
        }
    }

    @Test
    void close_shouldReleaseLoaderAndBeIdempotent() throws IOException {
        clearCaches();
        createFileSystemOverrides();
        TranslationService svc = new TranslationService("custom_messages", Collections.singletonList(tempDir));
        assertEquals("Zewnetrzny Sprzedawca", svc.getTranslation("pl", "seller"));

        svc.close();
        svc.close();
    }

    @Test
    void getTranslation_withFileSystemRoots_shouldPreferRootsOverClasspath() throws IOException {
        clearCaches();
        Path root = tempDir.resolve("roots-win");
        Files.createDirectories(root.resolve("i18n"));
        Files.write(
                root.resolve("i18n/test_overrides.properties"),
                "seller=From-Root\n".getBytes(StandardCharsets.UTF_8)
        );
        try (TranslationService svc = new TranslationService("i18n/test_overrides", Collections.singletonList(root))) {
            assertEquals("From-Root", svc.getTranslation("pl", "seller"));
        }
    }

    @Test
    void getTranslation_withSymlinkEscape_shouldNotLeakAndFallBackToClasspath() throws IOException {
        clearCaches();
        Path outside = tempDir.resolve("outside");
        Files.createDirectories(outside);
        Path secret = outside.resolve("test_overrides.properties");
        Files.write(secret, "seller=Compromised\n".getBytes(StandardCharsets.UTF_8));

        Path root = tempDir.resolve("root");
        Path i18n = Files.createDirectories(root.resolve("i18n"));

        boolean symlinkCreated = false;
        try {
            Files.createSymbolicLink(i18n.resolve("test_overrides.properties"), secret);
            symlinkCreated = true;
        } catch (UnsupportedOperationException | IOException e) {
            // Symlinks may be unsupported (e.g. Windows without privilege); skip the test.
        }
        Assumptions.assumeTrue(symlinkCreated, "Symlinks not supported in this environment");

        try (TranslationService svc = new TranslationService("i18n/test_overrides", Collections.singletonList(root))) {
            // The symlink escape must be rejected; classpath override kicks in instead.
            assertEquals("Nadpisany Sprzedawca", svc.getTranslation("pl", "seller"));
        }
    }

    private String findEntryValue(Document doc, String key) {
        NodeList entries = doc.getElementsByTagName("entry");
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            if (key.equals(entry.getAttribute("key"))) {
                return entry.getTextContent();
            }
        }
        return null;
    }

    // --- Cache helpers ---

    @SuppressWarnings("unchecked")
    private Map<String, ?> getDocumentCache() {
        try {
            Field field = TranslationService.class.getDeclaredField("DOCUMENT_CACHE");
            field.setAccessible(true);
            return (Map<String, ?>) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access cache", e);
        }
    }

    private void clearCaches() {
        try {
            Map<String, ?> docCache = getDocumentCache();
            docCache.clear();
        } catch (Exception e) {
            // Ignore if clearing fails
        }
    }

    private void createFileSystemOverrides() throws IOException {
        Files.write(
                tempDir.resolve("custom_messages.properties"),
                "seller=Zewnetrzny Sprzedawca\ncustom.key=External custom\n".getBytes(StandardCharsets.UTF_8)
        );
        Files.write(
                tempDir.resolve("custom_messages_en.properties"),
                "seller=External Seller\n".getBytes(StandardCharsets.UTF_8)
        );
    }

}
