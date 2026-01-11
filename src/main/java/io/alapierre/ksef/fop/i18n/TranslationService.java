package io.alapierre.ksef.fop.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.alapierre.ksef.fop.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class TranslationService {

    private static final Map<String, Document> DOCUMENT_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, JsonNode> JSON_CACHE = new ConcurrentHashMap<>();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Document getTranslationsAsXml(String lang) {
        String targetLang = Strings.defaultIfBlank(lang, "pl");
        return DOCUMENT_CACHE.computeIfAbsent(targetLang, TranslationService::loadAndCreateDocument);
    }

    /**
     * Gets a single translation value for the given key and language.
     *
     * @param lang the language code (e.g., "pl", "en")
     * @param key  the translation key
     * @return the translated value or the key itself if not found
     */
    public String getTranslation(String lang, String key) {
        String targetLang = Strings.defaultIfBlank(lang, "pl");
        JsonNode json = JSON_CACHE.computeIfAbsent(targetLang, TranslationService::loadJsonCached);

        if (json == null || !json.has(key)) {
            log.warn("Translation key '{}' not found for language '{}'", key, targetLang);
            return key;
        }
        return json.get(key).asText();
    }

    private static JsonNode loadJsonCached(String lang) {
        String resourcePath = "i18n/messages_" + lang + ".json";
        JsonNode langNode = loadJson(resourcePath);

        if (langNode == null && !"pl".equals(lang)) {
            log.warn("Translations for '{}' not found, falling back to 'pl'", lang);
            langNode = loadJson("i18n/messages_pl.json");
        }

        return langNode;
    }

    private static Document loadAndCreateDocument(String lang) {
        String resourcePath = "i18n/messages_" + lang + ".json";
        JsonNode langNode = loadJson(resourcePath);

        if (langNode == null && !"pl".equals(lang)) {
            log.warn("Translations for '{}' not found, falling back to 'pl'", lang);
            langNode = loadJson("i18n/messages_pl.json");
        }

        if (langNode == null) {
            log.error("Default translations (pl) not found!");
            return createEmptyDocument();
        }

        return createDocumentFromJson(langNode);
    }

    private static JsonNode loadJson(String path) {
        try (InputStream is = TranslationService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                return null;
            }
            return MAPPER.readTree(is);
        } catch (IOException e) {
            log.error("Failed to load translations from {}", path, e);
            return null;
        }
    }

    private static Document createDocumentFromJson(JsonNode json) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("labels");
            doc.appendChild(rootElement);

            Iterator<Map.Entry<String, JsonNode>> fields = json.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                Element entry = doc.createElement("entry");
                entry.setAttribute("key", field.getKey());
                entry.setTextContent(field.getValue().asText());
                rootElement.appendChild(entry);
            }
            return doc;
        } catch (ParserConfigurationException e) {
             log.error("Failed to create XML document for translations", e);
             throw new RuntimeException("Failed to create translation XML", e);
        }
    }

    private static Document createEmptyDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
