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

    /**
     * Path that the XSLT should load for locale-specific labels, or the base path when
     * there is no locale-specific file available for {@code lang}. The returned value is
     * always resolvable by the same {@link URIResolver} passed to this service.
     */
    public String resolveLocaleLabelPath(String lang) {
        String targetLang = Strings.defaultIfEmpty(lang, DEFAULT_LANGUAGE);
        for (String candidate : candidateResourceNames(targetLang)) {
            if (candidate.equals(LABELS_BASE_PATH)) {
                continue; // Base file is reported separately via LABELS_BASE_PATH.
            }
            if (canResolve(candidate)) {
                return candidate;
            }
        }
        return LABELS_BASE_PATH;
    }

    // -----------------------------------------------------------------------
    // Loading & merging
    // -----------------------------------------------------------------------

    private Document loadMergedDocument(String lang) {
        List<String> candidates = candidateResourceNames(lang);

        Document merged = null;
        for (String name : candidates) {
            Document doc = loadDocument(name);
            if (doc == null) continue;
            if (merged == null) {
                merged = cloneAsMergeTarget(doc);
            } else {
                mergeMissingEntries(merged, doc);
            }
        }
        if (merged == null) {
            log.warn("No label files found for language '{}'; serving an empty document", lang);
            merged = emptyLabelsDocument();
        }
        return merged;
    }

    @Nullable
    private Document loadDocument(String relativePath) {
        try {
            Optional<InputStream> stream = openResource(relativePath);
            if (!stream.isPresent()) return null;
            try (InputStream in = stream.get()) {
                InputSource sax = new InputSource(in);
                sax.setSystemId(relativePath);
                documentBuilder.reset();
                return documentBuilder.parse(sax);
            }
        } catch (IOException | SAXException | TransformerException e) {
            log.error("Failed to load labels resource {}", relativePath, e);
            return null;
        }
    }

    private Optional<InputStream> openResource(String href) throws IOException, TransformerException {
        Source source = resolve(href);
        if (source == null) return Optional.empty();
        if (source instanceof StreamSource) {
            StreamSource ss = (StreamSource) source;
            if (ss.getInputStream() != null) return Optional.of(ss.getInputStream());
            if (ss.getSystemId() != null) return Optional.of(new java.net.URL(ss.getSystemId()).openStream());
        }
        if (source instanceof SAXSource) {
            SAXSource ss = (SAXSource) source;
            if (ss.getInputSource() != null && ss.getInputSource().getByteStream() != null) {
                return Optional.of(ss.getInputSource().getByteStream());
            }
        }
        if (source instanceof DOMSource) {
            log.warn("DOMSource returned for labels resource {}; ignoring", href);
            return Optional.empty();
        }
        String systemId = source.getSystemId();
        if (systemId != null) return Optional.of(new java.net.URL(systemId).openStream());
        return Optional.empty();
    }

    @Nullable
    private Source resolve(String href) throws TransformerException {
        if (resolver instanceof TemplateResolver) {
            return ((TemplateResolver) resolver).tryResolve(href, null).orElse(null);
        }
        try {
            return resolver.resolve(href, null);
        } catch (TransformerException e) {
            log.debug("Resolver miss for {}: {}", href, e.getMessage());
            return null;
        }
    }

    private boolean canResolve(String href) {
        try {
            return resolve(href) != null;
        } catch (TransformerException e) {
            return false;
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
