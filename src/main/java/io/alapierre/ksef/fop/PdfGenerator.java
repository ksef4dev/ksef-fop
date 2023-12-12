package io.alapierre.ksef.fop;

import lombok.val;
import org.apache.fop.apps.*;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.11.11
 */
public class PdfGenerator {

    private final FopFactory fopFactory;

    /**
     * Create generator with Apache FOP config file from classpath
     *
     * @param fopConfig config file name
     * @throws IOException throws when cant load given config file from classpath
     * @throws ConfigurationException throws when config file has errors
     */
    public PdfGenerator(String fopConfig) throws IOException, ConfigurationException {
        this(loadResource(fopConfig));
    }

    /**
     * Create generator with Apache FOP config
     *
     * @param fopConfig InputStream to read FOP config file
     *
     * @throws ConfigurationException  throws when config file has errors
     */
    public PdfGenerator(InputStream fopConfig) throws ConfigurationException {
        val builder = new FopFactoryBuilder(new File(".").toURI());
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        Configuration cfg = cfgBuilder.build(fopConfig);
        builder.setConfiguration(cfg);
        fopFactory = builder.build();
    }

    /**
     * Generates UPO PDF from given XML and OutputStream
     * @param upoXML UPO XML
     * @param out destination OutputStream
     * @throws IOException throws when IO error occurs
     * @throws TransformerException throws when XSLT transformer error occurs
     * @throws FOPException throws when FOP error occurs
     */
    public void generateUpo(Source upoXML, OutputStream out) throws IOException, TransformerException, FOPException {

        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        Fop fop = fopFactory.newFop("application/pdf", foUserAgent, out);
        TransformerFactory factory = TransformerFactory.newInstance();

        Transformer transformer = factory.newTransformer(new StreamSource(loadResource("ksef_upo.fop")));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(upoXML, res);
    }

    /**
     * Generates invoice PDF from given XML and OutputStream
     * @param invoiceXml e-invoice FA(2) XML
     * @param out destination OutputStream
     * @throws IOException throws when IO error occurs
     * @throws TransformerException throws when XSLT transformer error occurs
     * @throws FOPException throws when FOP error occurs
     */
    public void generateInvoice(Source invoiceXml, OutputStream out) throws IOException, TransformerException, FOPException {

        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        Fop fop = fopFactory.newFop("application/pdf", foUserAgent, out);
        TransformerFactory factory = TransformerFactory.newInstance();

        Transformer transformer = factory.newTransformer(new StreamSource(loadResource("ksef_invoice.xsl")));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(invoiceXml, res);
    }

    private static InputStream loadResource(String resource) throws IOException {
        val res = PdfGenerator.class.getClassLoader().getResourceAsStream(resource);
        if(res == null) throw new IOException("Can't load classpath resource " + resource);
        return res;
    }

}
