package io.alapierre.ksef.fop.i18n;

import io.alapierre.ksef.fop.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class TranslationService {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final Map<String, Document> DOCUMENT_CACHE = new ConcurrentHashMap<>();

    private final DocumentBuilder documentBuilder;

    public TranslationService() {
        try {
            this.documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("Failed to initialize DocumentBuilder", e);
            throw new RuntimeException("Failed to initialize TranslationService", e);
        }
    }

    public Document getTranslationsAsXml(String lang) {
        String targetLang = Strings.defaultIfEmpty(lang, "pl");
        return DOCUMENT_CACHE.computeIfAbsent(targetLang, this::loadAndCreateDocument);
    }

    /**
     * Gets a single translation value for the given key and language.
     *
     * @param lang the language code (e.g., "pl", "en")
     * @param key  the translation key
     * @return the translated value or the key itself if not found
     */
    public String getTranslation(String lang, String key) {
        String targetLang = Strings.defaultIfEmpty(lang, "pl");
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/messages", Locale.forLanguageTag(targetLang), new Utf8Control());
        return bundle.containsKey(key) ? bundle.getString(key) : key;
    }

    private Document loadAndCreateDocument(String lang) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/messages", Locale.forLanguageTag(lang), new Utf8Control());
        return createDocumentFromBundle(bundle);
    }

    private Document createDocumentFromBundle(ResourceBundle bundle) {
        Document doc = documentBuilder.newDocument();
        Element rootElement = doc.createElement("labels");
        doc.appendChild(rootElement);

        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Element entry = doc.createElement("entry");
            entry.setAttribute("key", key);
            entry.setTextContent(bundle.getString(key));
            rootElement.appendChild(entry);
        }
        return doc;
    }

    /**
     * Ensures that property files are read using UTF-8, even on Java 8 and no fallback locale is used.
     */
    private static class Utf8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
                throws java.io.IOException {
            /*
             * Since Java 9, property files are read as UTF-8 by default, so this override is only necessary for Java 8 and below.
             */
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;

            URL url = loader.getResource(resourceName);
            if (url != null) {
                URLConnection connection = url.openConnection();
                if (reload) {
                    connection.setUseCaches(false);
                }
                try (InputStream stream = connection.getInputStream()) {
                    if (stream != null) {
                        try (InputStreamReader reader = new InputStreamReader(stream, UTF_8)) {
                            bundle = new PropertyResourceBundle(reader);
                        }
                    }
                }
            }
            return bundle;
        }

        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            /*
             * Disable fallback locale: always return null to avoid falling back to default locale.
             * This ensures that the lookup falls back to Locale.ROOT (which is Polish in our case) when the specific locale is not found.
             */
            return null;
        }
    }
}
