package io.alapierre.ksef.fop.i18n;

import io.alapierre.ksef.fop.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class TranslationService {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final Map<String, Document> DOCUMENT_CACHE = new ConcurrentHashMap<>();
    private static final String DEFAULT_BUNDLE_BASE_NAME = "i18n/messages";

    private final String translationOverridesResourcePath;
    private final DocumentBuilder documentBuilder;

    public TranslationService() {
        this(null);
    }

    /**
     * Creates a TranslationService with user-provided translation overrides
     * loaded from a {@link ResourceBundle} on the classpath.
     * Only the keys present in the user's bundle will replace the built-in defaults;
     * all other keys fall back to the default translations shipped with the library.
     * <p>
     * The user should place their properties files on the classpath following the
     * standard {@link ResourceBundle} naming convention, e.g.:
     * <ul>
     *   <li>{@code custom/messages.properties} — default / Polish overrides</li>
     *   <li>{@code custom/messages_en.properties} — English overrides</li>
     * </ul>
     *
     * @param translationOverridesResourcePath classpath-relative path including the resource bundle base name
     *                                         (without the {@code .properties} extension and without the locale suffix),
     *                                         e.g. {@code "i18n/custom_messages"} which resolves to
     *                                         {@code i18n/custom_messages.properties}, {@code i18n/custom_messages_en.properties}, etc.;
     *                                         {@code null} to use only built-in defaults
     */
    public TranslationService(@Nullable String translationOverridesResourcePath) {
        this.translationOverridesResourcePath = translationOverridesResourcePath;
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
     * User-provided overrides take precedence over built-in defaults.
     *
     * @param lang the language code (e.g., "pl", "en")
     * @param key  the translation key
     * @return the translated value or the key itself if not found
     */
    public String getTranslation(String lang, String key) {
        String targetLang = Strings.defaultIfEmpty(lang, "pl");

        ResourceBundle userBundle = loadUserBundle(targetLang);
        if (userBundle != null && userBundle.containsKey(key)) {
            return userBundle.getString(key);
        }

        ResourceBundle defaultBundle = ResourceBundle.getBundle(DEFAULT_BUNDLE_BASE_NAME, Locale.forLanguageTag(targetLang), new Utf8Control());
        return defaultBundle.containsKey(key) ? defaultBundle.getString(key) : key;
    }

    private @Nullable ResourceBundle loadUserBundle(String lang) {
        if (translationOverridesResourcePath == null) return null;
        try {
            return ResourceBundle.getBundle(translationOverridesResourcePath, Locale.forLanguageTag(lang), new Utf8Control());
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private Document loadAndCreateDocument(String lang) {
        ResourceBundle defaultBundle = ResourceBundle.getBundle(DEFAULT_BUNDLE_BASE_NAME, Locale.forLanguageTag(lang), new Utf8Control());
        ResourceBundle userBundle = loadUserBundle(lang);
        return createDocumentFromBundles(defaultBundle, userBundle);
    }

    private Document createDocumentFromBundles(ResourceBundle defaultBundle, @Nullable ResourceBundle userBundle) {
        Document doc = documentBuilder.newDocument();
        Element rootElement = doc.createElement("labels");
        doc.appendChild(rootElement);

        Set<String> addedKeys = new HashSet<>();

        Enumeration<String> keys = defaultBundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            String value = (userBundle != null && userBundle.containsKey(key))
                    ? userBundle.getString(key)
                    : defaultBundle.getString(key);
            Element entry = doc.createElement("entry");
            entry.setAttribute("key", key);
            entry.setTextContent(value);
            rootElement.appendChild(entry);
            addedKeys.add(key);
        }

        if (userBundle != null) {
            Enumeration<String> userKeys = userBundle.getKeys();
            while (userKeys.hasMoreElements()) {
                String key = userKeys.nextElement();
                if (!addedKeys.contains(key)) {
                    Element entry = doc.createElement("entry");
                    entry.setAttribute("key", key);
                    entry.setTextContent(userBundle.getString(key));
                    rootElement.appendChild(entry);
                }
            }
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
