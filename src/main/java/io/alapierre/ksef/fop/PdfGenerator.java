package io.alapierre.ksef.fop;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PdfGenerator {

    private final String fopConfig;

    public void generateUpo(Source upoXML, OutputStream out) throws IOException, TransformerException, ConfigurationException, FOPException {

        FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI());
        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        Configuration cfg = cfgBuilder.build(loadResource(fopConfig));
        builder.setConfiguration(cfg);
        FopFactory fopFactory = builder.build();
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        Fop fop = fopFactory.newFop("application/pdf", foUserAgent, out);
        TransformerFactory factory = TransformerFactory.newInstance();

        Transformer transformer = factory.newTransformer(new StreamSource(loadResource("ksef_upo.fop")));
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(upoXML, res);
    }

    private InputStream loadResource(String resource) throws IOException {
        val res = PdfGenerator.class.getClassLoader().getResourceAsStream(resource);
        if(res == null) throw new IOException("Can't load classpath resource " + resource);
        return res;
    }

}
