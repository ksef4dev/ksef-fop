package io.alapierre.ksef.fop;

import lombok.val;
import org.apache.fop.apps.*;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Base64;
import java.util.Optional;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2023.11.11
 */
public class PdfGenerator {

    private final FopFactory fopFactory;
    private InvoicePdfConfig invoicePdfConfig = new InvoicePdfConfig();

    /**
     * Create generator with Apache FOP config file from classpath
     *
     * @param fopConfig config file name
     * @throws IOException throws when cant load given config file from classpath
     * @throws ConfigurationException throws when config file has errors
     */
    public PdfGenerator(String fopConfig, InvoicePdfConfig invoicePdfConfig) throws IOException, ConfigurationException {
        this(loadResource(fopConfig));
        this.invoicePdfConfig = invoicePdfConfig;
    }

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
     * @param ksefNumber KSeF number
     * @param verificationLink The verification link
     * @param qrCode Barcode
     * @throws IOException throws when IO error occurs
     * @throws TransformerException throws when XSLT transformer error occurs
     * @throws FOPException throws when FOP error occurs
     */
    public void generateInvoice(@NotNull Source invoiceXml,
                                @Nullable String ksefNumber,
                                @Nullable String verificationLink,
                                byte[] qrCode,
                                byte[] logo,
                                OutputStream out) throws IOException, TransformerException, FOPException {
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        Fop fop = fopFactory.newFop("application/pdf", foUserAgent, out);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(new StreamSource(loadResource("ksef_invoice.xsl")));

        insertAdditionalInvoiceData(ksefNumber, verificationLink, qrCode, logo, transformer);

        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(invoiceXml, res);
    }

    private void insertAdditionalInvoiceData(@Nullable String ksefNumber,
                                             @Nullable String verificationLink,
                                             byte[] qrCode,
                                             byte[] logo,
                                             @NotNull Transformer transformer) {
        Optional.ofNullable(qrCode)
                .map(Base64.getEncoder()::encodeToString)
                .ifPresent(encodedQrCode -> setParameterIfNotNull("qrCode", encodedQrCode, transformer));
        Optional.ofNullable(logo)
                .map(Base64.getEncoder()::encodeToString)
                .ifPresent(encodedLogo -> setParameterIfNotNull("logo", encodedLogo, transformer));
        setParameterIfNotNull("nrKsef", ksefNumber, transformer);
        setParameterIfNotNull("verificationLink", verificationLink, transformer);
        setParameterIfNotNull("showFooter", invoicePdfConfig.isShowFooter(), transformer);
    }

    private void setParameterIfNotNull(@NotNull String name,
                                       @Nullable Object value,
                                       @NotNull Transformer transformer) {
        if (value != null) {
            transformer.setParameter(name, value);
        }
    }

    private static InputStream loadResource(String resource) throws IOException {
        val res = PdfGenerator.class.getClassLoader().getResourceAsStream(resource);
        if(res == null) throw new IOException("Can't load classpath resource " + resource);
        return res;
    }

}
