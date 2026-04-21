package io.alapierre.ksef.fop.i18n;

import io.alapierre.ksef.fop.Language;
import io.alapierre.ksef.fop.internal.Strings;
import io.alapierre.ksef.fop.internal.TemplateResolver;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class TranslationService {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = createSecureDocumentBuilderFactory();
    private final Map<String, Document> DOCUMENT_CACHE = new ConcurrentHashMap<>();

    public static final String LABELS_BASE_NAME = "i18n/labels";
    public static final String LABELS_EXTENSION = ".xml";
    public static final String LABELS_BASE_PATH = "/" + LABELS_BASE_NAME + LABELS_EXTENSION;

    private static final String DEFAULT_LANGUAGE = Language.DEFAULT_LANGUAGE_TAG;

    private final URIResolver resolver;
    private final DocumentBuilder documentBuilder;

    /** No-override service: labels come from the library classpath only. */
    public TranslationService() {
        this(newClasspathOnlyResolver());
    }

    /**
     * Creates a translation service that loads labels through the supplied resolver.
     *
     * <p>Pass a {@link TemplateResolver} configured with the same root list as the
     * rendering transformer to share a single resolution strategy across XSLT templates
     * and translations.</p>
     */
    public TranslationService(@NotNull URIResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
        try {
            this.documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Failed to initialize TranslationService", e);
        }
    }

    /**
     * Returns the merged label document for {@code lang}, built from the most-specific
     * locale file plus anything not shadowed from the base file.
     *
     * <p>The returned document is cached per language — callers must not mutate it.</p>
     */
    public Document getTranslationsAsXml(String lang) {
        String targetLang = Strings.defaultIfEmpty(lang, DEFAULT_LANGUAGE);
        return DOCUMENT_CACHE.computeIfAbsent(targetLang, this::loadMergedDocument);
    }

    /**
     * Single-key lookup. An unknown key is returned as-is.
     */
    public String getTranslation(String lang, String key) {
        Document doc = getTranslationsAsXml(lang);
        NodeList entries = doc.getElementsByTagName("entry");
        for (int i = 0; i < entries.getLength(); i++) {
            Element entry = (Element) entries.item(i);
            if (key.equals(entry.getAttribute("key"))) {
                return entry.getTextContent();
            }
        }
        return key;
    }

    // -----------------------------------------------------------------------
    // Loading & merging
    // -----------------------------------------------------------------------

    /**
     * Builds the merged label document for {@code lang} by overlaying every resolvable
     * variant in priority order.
     *
     * <p>For each candidate file name (most-specific locale first, falling back to the
     * base file) we consult <em>all</em> filesystem roots <em>and</em> the classpath —
     * each individual root's partial override is copied on top of the classpath default
     * for the same file name. Priority (earlier wins) is:</p>
     * <ol>
     *   <li>Locale-specific file in each user root (in insertion order)</li>
     *   <li>Locale-specific file on the classpath</li>
     *   <li>Base file in each user root (in insertion order)</li>
     *   <li>Base file on the classpath</li>
     * </ol>
     *
     * <p>This is the critical difference from a plain {@code URIResolver.resolve} that
     * stops at the first hit: a partial filesystem override must <em>never</em> shadow
     * the full classpath defaults, otherwise missing keys render as empty text.</p>
     */
    private Document loadMergedDocument(String lang) {
        List<String> candidates = candidateResourceNames(lang);

        Document merged = null;
        for (String name : candidates) {
            for (Document doc : loadAllVariants(name)) {
                if (merged == null) {
                    merged = cloneAsMergeTarget(doc);
                } else {
                    mergeMissingEntries(merged, doc);
                }
            }
        }
        if (merged == null) {
            log.warn("No label files found for language '{}'; serving an empty document", lang);
            merged = emptyLabelsDocument();
        }
        return merged;
    }

    /**
     * Loads every parse-able XML document behind {@code relativePath} (user roots first,
     * then classpath). Missing or unreadable sources are skipped silently — callers treat
     * the result as "best-effort" and a language with zero hits falls back to an empty doc.
     */
    private List<Document> loadAllVariants(String relativePath) {
        List<Source> sources;
        try {
            if (resolver instanceof TemplateResolver) {
                sources = ((TemplateResolver) resolver).resolveAll(relativePath);
            } else {
                // Custom resolver: can only give us a single source, so there's no
                // layering across roots / classpath available from here.
                Source single = resolveSingle(relativePath);
                sources = (single == null) ? Collections.emptyList() : Collections.singletonList(single);
            }
        } catch (TransformerException e) {
            log.debug("Resolver error for {}: {}", relativePath, e.getMessage());
            return Collections.emptyList();
        }

        List<Document> docs = new ArrayList<>(sources.size());
        for (Source source : sources) {
            Document doc = parseSource(source, relativePath);
            if (doc != null) docs.add(doc);
        }
        return docs;
    }

    @Nullable
    private Document parseSource(Source source, String relativePath) {
        try (InputStream in = openSourceStream(source, relativePath)) {
            if (in == null) return null;
            InputSource sax = new InputSource(in);
            sax.setSystemId(relativePath);
            documentBuilder.reset();
            return documentBuilder.parse(sax);
        } catch (IOException | SAXException e) {
            log.error("Failed to load labels resource {}", relativePath, e);
            return null;
        }
    }

    @Nullable
    private InputStream openSourceStream(Source source, String href) throws IOException {
        if (source instanceof StreamSource) {
            StreamSource ss = (StreamSource) source;
            if (ss.getInputStream() != null) return ss.getInputStream();
            if (ss.getSystemId() != null) return new java.net.URL(ss.getSystemId()).openStream();
        }
        if (source instanceof SAXSource) {
            SAXSource ss = (SAXSource) source;
            if (ss.getInputSource() != null && ss.getInputSource().getByteStream() != null) {
                return ss.getInputSource().getByteStream();
            }
        }
        if (source instanceof DOMSource) {
            log.warn("DOMSource returned for labels resource {}; ignoring", href);
            return null;
        }
        String systemId = source.getSystemId();
        if (systemId != null) return new java.net.URL(systemId).openStream();
        return null;
    }

    @Nullable
    private Source resolveSingle(String href) {
        try {
            return resolver.resolve(href, null);
        } catch (TransformerException e) {
            log.debug("Resolver miss for {}: {}", href, e.getMessage());
            return null;
        }
    }

    private Document cloneAsMergeTarget(Document source) {
        Document clone = documentBuilder.newDocument();
        Element root = clone.createElement("labels");
        clone.appendChild(root);
        NodeList entries = source.getElementsByTagName("entry");
        for (int i = 0; i < entries.getLength(); i++) {
            Node imported = clone.importNode(entries.item(i), true);
            root.appendChild(imported);
        }
        return clone;
    }

    /**
     * Copies entries from {@code source} into {@code target} only for keys that
     * {@code target} does not already have.
     */
    private static void mergeMissingEntries(Document target, Document source) {
        Set<String> existing = new HashSet<>();
        NodeList targetEntries = target.getElementsByTagName("entry");
        for (int i = 0; i < targetEntries.getLength(); i++) {
            existing.add(((Element) targetEntries.item(i)).getAttribute("key"));
        }

        Element targetRoot = target.getDocumentElement();
        NodeList sourceEntries = source.getElementsByTagName("entry");
        for (int i = 0; i < sourceEntries.getLength(); i++) {
            Element src = (Element) sourceEntries.item(i);
            String key = src.getAttribute("key");
            if (existing.contains(key)) continue;
            Element copy = target.createElement("entry");
            copy.setAttribute("key", key);
            copy.setTextContent(src.getTextContent());
            targetRoot.appendChild(copy);
            existing.add(key);
        }
    }

    private Document emptyLabelsDocument() {
        Document doc = documentBuilder.newDocument();
        doc.appendChild(doc.createElement("labels"));
        return doc;
    }

    // -----------------------------------------------------------------------
    // Locale candidate file names (no ResourceBundle)
    // -----------------------------------------------------------------------

    /**
     * Returns candidate label file names, most specific first.
     *
     * <p>Mirrors the standard Java locale-bundle fallback (language + country + variant
     * → language + country → language → base) without touching
     * {@link java.util.ResourceBundle}. BCP 47 language tags with {@code -} as separator
     * are accepted and normalised.</p>
     */
    static List<String> candidateResourceNames(String lang) {
        Locale locale = Locale.forLanguageTag(lang.replace('_', '-'));
        List<String> names = new ArrayList<>(4);
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        if (!language.isEmpty() && !country.isEmpty() && !variant.isEmpty()) {
            names.add("/" + LABELS_BASE_NAME + "_" + language + "_" + country + "_" + variant + LABELS_EXTENSION);
        }
        if (!language.isEmpty() && !country.isEmpty()) {
            names.add("/" + LABELS_BASE_NAME + "_" + language + "_" + country + LABELS_EXTENSION);
        }
        if (!language.isEmpty()) {
            names.add("/" + LABELS_BASE_NAME + "_" + language + LABELS_EXTENSION);
        }
        names.add(LABELS_BASE_PATH);
        return names;
    }

    // -----------------------------------------------------------------------
    // Default resolver (classpath only)
    // -----------------------------------------------------------------------

    private static URIResolver newClasspathOnlyResolver() {
        try {
            return new TemplateResolver(Collections.emptyList());
        } catch (TransformerException e) {
            throw new IllegalStateException("Failed to build default TranslationService resolver", e);
        }
    }

    // -----------------------------------------------------------------------
    // Secure parser configuration
    // -----------------------------------------------------------------------

    private static DocumentBuilderFactory createSecureDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Unable to secure XML parser for translations", e);
        }
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }
}
