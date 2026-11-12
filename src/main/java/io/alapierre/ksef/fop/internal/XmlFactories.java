/*
 * SPDX-License-Identifier: Apache-2.0
 */
package io.alapierre.ksef.fop.internal;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.jaxp.SaxonTransformerFactory;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.trans.CompilerInfo;
import net.sf.saxon.trans.XPathException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.SAXException;

import java.util.List;
import java.util.Objects;

/**
 * Shared XML processing utilities with a hardened parser configuration.
 */
public final class XmlFactories {

    /**
     * A namespace-aware {@link SAXParserFactory} that disallows DOCTYPE declarations.
     *
     * <p>The following features are set:</p>
     * <ul>
     *   <li>{@code http://apache.org/xml/features/disallow-doctype-decl} = {@code true}</li>
     *   <li>{@value XMLConstants#FEATURE_SECURE_PROCESSING} = {@code true}</li>
     * </ul>
     */
    public static final SAXParserFactory SAX_PARSER_FACTORY;

    /**
     * A namespace-aware {@link DocumentBuilderFactory} that disallows DOCTYPE declarations.
     *
     * <p>The following features are set:</p>
     * <ul>
     *   <li>{@code http://apache.org/xml/features/disallow-doctype-decl} = {@code true}</li>
     *   <li>{@value XMLConstants#FEATURE_SECURE_PROCESSING} = {@code true}</li>
     * </ul>
     */
    public static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;

    /**
     * Secure configuration for Saxon {@link TransformerFactoryImpl} instances
     */
    private static final Configuration SAXON_CONFIGURATION;

    /** System property overriding the default size of the compiled template cache. */
    public static final String TEMPLATE_CACHE_MAX_SIZE_PROPERTY = "ksef.fop.template-cache.max-size";

    /** Default number of compiled stylesheets kept in memory. */
    public static final int DEFAULT_TEMPLATE_CACHE_MAX_SIZE = 32;

    private static final BoundedTemplateCache<TemplateKey, Templates> TEMPLATE_CACHE =
            new BoundedTemplateCache<TemplateKey, Templates>(readMaxSizeProperty());

    static {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            // Explicitly set even though it is the default, so it cannot be overridden via system properties.
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // Prevents XXE attacks; DOCTYPE declarations are not used in e-invoices.
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            SAX_PARSER_FACTORY = factory;
        } catch (ParserConfigurationException | SAXException e) {
            throw new ExceptionInInitializerError(e);
        }

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            // Explicitly set even though it is the default, so it cannot be overridden via system properties.
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // Prevents XXE attacks; DOCTYPE declarations are not used in e-invoices.
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DOCUMENT_BUILDER_FACTORY = dbf;
        } catch (ParserConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }

        SAXON_CONFIGURATION = new Configuration();
        ParseOptions parseOptions = SAXON_CONFIGURATION.getParseOptions()
                .withXMLReaderMaker(() -> {
                    try {
                        return SAX_PARSER_FACTORY.newSAXParser().getXMLReader();
                    } catch (ParserConfigurationException | SAXException e) {
                        throw new XPathException(e);
                    }
                });
        SAXON_CONFIGURATION.setParseOptions(parseOptions);
    }

    /**
     * Creates a Saxon {@link TransformerFactory} backed by the shared hardened
     * {@link Configuration}.
     *
     * <p>Every XML source parsed by transformers produced by this factory is read with
     * the SAX parser configured on {@link #SAX_PARSER_FACTORY} (no DOCTYPE, no external
     * entities). {@link XMLConstants#FEATURE_SECURE_PROCESSING} is also set on the
     * factory, which disables Saxon extension functions.</p>
     *
     * <p>Callers are expected to register a restrictive {@code URIResolver} before
     * compiling or running any stylesheet: the factory itself does not restrict
     * {@code xsl:import}, {@code xsl:include} or {@code document()} lookups.</p>
     */
    public static TransformerFactory createTransformerFactory() {
        TransformerFactory factory = new TransformerFactoryImpl(SAXON_CONFIGURATION);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (TransformerConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }
        return factory;
    }

    /**
     * Compiles a stylesheet with {@code resolver} used for {@code xsl:include}/{@code xsl:import}.
     *
     * <p>The resolver is attached to a per-compilation {@link CompilerInfo}, <strong>not</strong> to the
     * factory: {@code SaxonTransformerFactory.setURIResolver} stores the resolver in the shared
     * {@link #SAXON_CONFIGURATION}, so concurrent compilations for different resource roots would
     * overwrite each other's resolver and fail with {@code Unexpected URI scheme}.</p>
     */
    private static Templates compile(TemplateResolver resolver, String templatePath) throws TransformerException {
        SaxonTransformerFactory factory = (SaxonTransformerFactory) createTransformerFactory();
        CompilerInfo compilerInfo = new CompilerInfo(SAXON_CONFIGURATION.getDefaultXsltCompilerInfo());
        compilerInfo.setURIResolver(resolver);
        Source stylesheet = resolver.resolve(templatePath, null);
        return factory.newTemplates(stylesheet, compilerInfo);
    }

    /**
     * Returns a compiled {@link Templates} for {@code templatePath}, resolved by a
     * {@link TemplateResolver}. Cached per {@code (resource roots, templatePath)} in a bounded
     * LRU cache (see {@link #setCompiledTemplateCacheMaxSize(int)}).
     *
     * <p>Safe for concurrent use: compilation runs outside the cache lock, so a slow compilation
     * (e.g. remote includes) does not block cache hits for other templates.</p>
     */
    public static Templates getTemplate(TemplateResolver resolver, String templatePath) throws TransformerException {
        TemplateKey key = new TemplateKey(resolver.getResourceRoots(), templatePath);
        Templates cached = TEMPLATE_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        Templates compiled = compile(resolver, templatePath);
        return TEMPLATE_CACHE.putIfAbsent(key, compiled);
    }

    /**
     * Clears the in-memory cache of compiled XSLT {@link Templates}.
     * Call after template or include files change at runtime (e.g. management API upload).
     */
    public static void clearCompiledTemplateCache() {
        TEMPLATE_CACHE.clear();
    }

    /**
     * Sets the maximum number of compiled stylesheets kept in memory; least recently used entries
     * are evicted. A compiled FA(3) invoice stylesheet takes several MB of heap, so an unbounded
     * cache grows with every distinct resource root (e.g. one root per tenant).
     *
     * <p>Defaults to {@value #DEFAULT_TEMPLATE_CACHE_MAX_SIZE}, overridable with the
     * {@value #TEMPLATE_CACHE_MAX_SIZE_PROPERTY} system property.</p>
     *
     * @param maxSize maximum number of entries, at least 1
     */
    public static void setCompiledTemplateCacheMaxSize(int maxSize) {
        TEMPLATE_CACHE.setMaxSize(maxSize);
    }

    /** Current number of compiled stylesheets in the cache. */
    public static int compiledTemplateCacheSize() {
        return TEMPLATE_CACHE.size();
    }

    static int readMaxSizeProperty() {
        String value = System.getProperty(TEMPLATE_CACHE_MAX_SIZE_PROPERTY);
        if (value == null || value.trim().isEmpty()) {
            return DEFAULT_TEMPLATE_CACHE_MAX_SIZE;
        }
        try {
            return Math.max(1, Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return DEFAULT_TEMPLATE_CACHE_MAX_SIZE;
        }
    }

    private XmlFactories() {
        // utility class
    }

    /** Cache key made of values only — does not retain the resolver (and its catalog) per entry. */
    private static final class TemplateKey {

        private final List<ResourceRoot> resourceRoots;
        private final String templatePath;

        private TemplateKey(List<ResourceRoot> resourceRoots, String templatePath) {
            this.resourceRoots = resourceRoots;
            this.templatePath = templatePath;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TemplateKey)) return false;
            TemplateKey that = (TemplateKey) o;
            return Objects.equals(resourceRoots, that.resourceRoots)
                    && Objects.equals(templatePath, that.templatePath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resourceRoots, templatePath);
        }
    }
}
