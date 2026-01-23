package io.alapierre.ksef.fop.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TranslationServiceTest {

    private TranslationService translationService;

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
        assertEquals("Date of issue", key2);
        assertEquals("Seller", key3);
    }

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
}
