/*
 * SPDX-License-Identifier: Apache-2.0
 */
package io.alapierre.ksef.fop.internal;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.trans.XPathException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.xml.sax.SAXException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * Precompiled templates
     */
    private static final Map<TemplateKey, Templates> TEMPLATE_CACHE = new ConcurrentHashMap<>();

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

    private static Templates createTemplates(TemplateKey key) {
        try {
            TransformerFactory factory = createTransformerFactory();
            factory.setURIResolver(key.resolver);
            return factory.newTemplates(key.resolver.resolve(key.templatePath, null));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a compiled {@link Templates} for {@code templatePath}, resolved against the given
     * {@code roots} (then the classpath) by a {@link TemplateResolver}.
     *
     * <p><strong>Caching:</strong> Compiled templates are cached indefinitely, keyed by
     * {@code (roots, templatePath)}. Once a template has been compiled, later changes to the
     * underlying stylesheet files are <em>not</em> picked up for the lifetime of the JVM.</p>
     */
    public static Templates getTemplate(TemplateResolver resolver, String templatePath) throws TransformerException {
        TemplateKey key = new TemplateKey(resolver, templatePath);
        try {
            return TEMPLATE_CACHE.computeIfAbsent(key, XmlFactories::createTemplates);
        } catch (RuntimeException wrapped) {
            Throwable cause = wrapped.getCause();
            if (cause instanceof TransformerException) {
                throw (TransformerException) cause;
            }
            throw wrapped;
        }
    }

    private XmlFactories() {
        // utility class
    }

    private static final class TemplateKey {

        private final TemplateResolver resolver;
        private final String templatePath;

        private TemplateKey(TemplateResolver resolver, String templatePath) {
            this.resolver = resolver;
            this.templatePath = templatePath;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TemplateKey)) return false;
            TemplateKey that = (TemplateKey) o;
            return Objects.equals(resolver.getRoots(), that.resolver.getRoots()) && Objects.equals(templatePath, that.templatePath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resolver.getRoots(), templatePath);
        }
    }
}
