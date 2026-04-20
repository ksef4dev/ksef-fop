package io.alapierre.ksef.fop.i18n;

import io.alapierre.ksef.fop.internal.FilesystemRoots;
import io.alapierre.ksef.fop.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class TranslationService implements Closeable {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final Map<String, Document> DOCUMENT_CACHE = new ConcurrentHashMap<>();
    private static final String DEFAULT_BUNDLE_BASE_NAME = "i18n/messages";

    private final String userBundleBaseName;
    private final ClassLoader userBundleClassLoader;
    @Nullable
    private final URLClassLoader ownedClassLoader;
    private final String cacheKeyPrefix;
    private final DocumentBuilder documentBuilder;

    public TranslationService() {
        this(null, Collections.emptyList());
    }

    /**
     * Creates a TranslationService with user-provided translation overrides loaded from a
     * classpath {@link ResourceBundle}. Naming follows standard ResourceBundle convention, e.g.:
     * <ul>
     *   <li>{@code i18n/custom_messages.properties} — default / fallback overrides</li>
     *   <li>{@code i18n/custom_messages_en.properties} — English overrides</li>
     * </ul>
     *
     * @param bundleBaseName classpath base name without locale suffix and without
     *                       {@code .properties} extension; {@code null} to use only built-in defaults
     */
    public TranslationService(@Nullable String bundleBaseName) {
        this(bundleBaseName, Collections.emptyList());
    }

    /**
     * Creates a TranslationService with user-provided translation overrides.
     * <p>
     * Lookup order for override bundles:
     * <ol>
     *   <li>Each path in {@code translationRoots} (in order) — filesystem directories that are searched
     *       using standard {@link ResourceBundle} semantics, as if each root were added to the classpath.
     *       For root {@code /etc/ksef} and {@code bundleBaseName = "i18n/custom_messages"} the resolver
     *       looks for {@code /etc/ksef/i18n/custom_messages.properties} (and locale-specific variants).</li>
     *   <li>Application classpath (fallback).</li>
     * </ol>
     * Built-in library defaults ({@code i18n/messages.properties}) always act as the final fallback
     * when a key is missing from the override bundle.
     *
     * @param bundleBaseName   classpath-relative base name without locale suffix and without
     *                         {@code .properties} extension; must not contain a URI scheme,
     *                         absolute path, or {@code ..} path segment; {@code null} to use only
     *                         built-in defaults
     * @param translationRoots ordered list of filesystem roots searched before the classpath;
     *                         may be empty
     * @throws IllegalArgumentException if {@code bundleBaseName} contains a scheme, is absolute
     *                                  or contains a {@code ..} segment, or if a root is not an
     *                                  accessible directory
     */
    public TranslationService(@Nullable String bundleBaseName, @NotNull List<Path> translationRoots) {
        String normalizedBundleBaseName = validateAndNormalizeBundleBaseName(bundleBaseName);
        List<Path> canonicalRoots = canonicalizeRoots(translationRoots);

        this.userBundleBaseName = normalizedBundleBaseName;
        if (normalizedBundleBaseName == null) {
            this.ownedClassLoader = null;
            this.userBundleClassLoader = null;
        } else if (canonicalRoots.isEmpty()) {
            this.ownedClassLoader = null;
            this.userBundleClassLoader = Thread.currentThread().getContextClassLoader();
        } else {
            ContainedURLClassLoader loader = createContainedLoader(canonicalRoots);
            this.ownedClassLoader = loader;
            this.userBundleClassLoader = loader;
        }
        this.cacheKeyPrefix = buildCacheKeyPrefix(normalizedBundleBaseName, canonicalRoots);
        try {
            this.documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("Failed to initialize DocumentBuilder", e);
            throw new RuntimeException("Failed to initialize TranslationService", e);
        }
    }

    public Document getTranslationsAsXml(String lang) {
        String targetLang = Strings.defaultIfEmpty(lang, "pl");
        String cacheKey = cacheKeyPrefix + "|" + targetLang;
        return DOCUMENT_CACHE.computeIfAbsent(cacheKey, key -> loadAndCreateDocument(targetLang));
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

    /**
     * Releases resources held by this service. Safe to call multiple times.
     * After {@code close()}, further lookups may fail for bundles backed by the filesystem roots.
     */
    @Override
    public void close() throws IOException {
        if (ownedClassLoader != null) {
            ownedClassLoader.close();
        }
    }

    private @Nullable ResourceBundle loadUserBundle(String lang) {
        if (userBundleBaseName == null || userBundleClassLoader == null) return null;
        try {
            return ResourceBundle.getBundle(userBundleBaseName, Locale.forLanguageTag(lang), userBundleClassLoader, new Utf8Control());
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private Document loadAndCreateDocument(String lang) {
        ResourceBundle defaultBundle = ResourceBundle.getBundle(DEFAULT_BUNDLE_BASE_NAME, Locale.forLanguageTag(lang), new Utf8Control());
        ResourceBundle userBundle = loadUserBundle(lang);
        return createDocumentFromBundles(defaultBundle, userBundle);
    }

    // -----------------------------------------------------------------------
    // Validation & canonicalisation
    // -----------------------------------------------------------------------

    /**
     * Validates and normalizes the supplied {@code bundleBaseName}.
     *
     * <p>Normalisation unifies equivalent spellings so that the derived document-cache key is
     * stable. Concretely:</p>
     * <ul>
     *     <li>leading/trailing whitespace is trimmed;</li>
     *     <li>Windows-style {@code \} separators are converted to {@code /};</li>
     *     <li>repeated separators ({@code //}) are collapsed;</li>
     *     <li>redundant {@code .} segments are dropped.</li>
     * </ul>
     * <p>Unsafe inputs are rejected up front: URI schemes, absolute paths, empty names,
     * trailing separators, {@code ..} segments.</p>
     *
     * @return the normalized name, or {@code null} if the input was {@code null}
     */
    @Nullable
    private static String validateAndNormalizeBundleBaseName(@Nullable String name) {
        if (name == null) return null;
        String trimmed = sanitizeBundleName(name);
        List<String> segments = new ArrayList<>();
        for (String segment : trimmed.split("[/\\\\]")) {
            if (segment.isEmpty() || ".".equals(segment)) continue;
            if ("..".equals(segment)) {
                throw new IllegalArgumentException("bundleBaseName must not contain '..' segments: " + name);
            }
            segments.add(segment);
        }
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("bundleBaseName must not be empty after normalisation: " + name);
        }
        return String.join("/", segments);
    }

    private static @NotNull String sanitizeBundleName(@NotNull String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("bundleBaseName must not be empty");
        }
        if (trimmed.contains(":")) {
            throw new IllegalArgumentException("bundleBaseName must not contain a URI scheme: " + name);
        }
        if (trimmed.startsWith("/") || trimmed.startsWith("\\")) {
            throw new IllegalArgumentException("bundleBaseName must be relative: " + name);
        }
        if (trimmed.endsWith("/") || trimmed.endsWith("\\")) {
            throw new IllegalArgumentException("bundleBaseName must not end with a separator: " + name);
        }
        return trimmed;
    }

    private static List<Path> canonicalizeRoots(List<Path> roots) {
        try {
            return FilesystemRoots.canonicalize(roots);
        } catch (IOException e) {
            throw new IllegalArgumentException("Translation root is not accessible: " + e.getMessage(), e);
        }
    }

    private static ContainedURLClassLoader createContainedLoader(List<Path> canonicalRoots) {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        // URL[] is unused: ContainedURLClassLoader overrides findResource and ignores its parent URL list.
        // We still extend URLClassLoader to inherit Closeable semantics.
        return new ContainedURLClassLoader(new URL[0], parent, canonicalRoots);
    }

    private static String buildCacheKeyPrefix(@Nullable String bundleBaseName, List<Path> translationRoots) {
        if (bundleBaseName == null) return "default";
        String rootsPart = translationRoots.isEmpty()
                ? "cp"
                : translationRoots.stream().map(Path::toString).collect(Collectors.joining(":"));
        return bundleBaseName + "@" + rootsPart;
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

    private static final class ContainedURLClassLoader extends URLClassLoader {

        private final List<Path> canonicalRoots;

        ContainedURLClassLoader(URL[] urls, ClassLoader parent, List<Path> canonicalRoots) {
            super(urls, parent);
            this.canonicalRoots = canonicalRoots;
        }

        @Override
        public URL getResource(String name) {
            URL fromRoots = findResource(name);
            if (fromRoots != null) return fromRoots;
            ClassLoader parent = getParent();
            return parent != null ? parent.getResource(name) : null;
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            URL url = getResource(name);
            if (url == null) return null;
            try {
                return url.openStream();
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public URL findResource(String name) {
            if (name == null) return null;
            for (Path root : canonicalRoots) {
                URL candidate = FilesystemRoots.resolveFileWithin(root, name)
                        .map(ContainedURLClassLoader::toUrlOrNull)
                        .orElse(null);
                if (candidate != null) return candidate;
            }
            return null;
        }

        @Nullable
        private static URL toUrlOrNull(Path path) {
            try {
                return path.toUri().toURL();
            } catch (java.net.MalformedURLException e) {
                return null;
            }
        }
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
