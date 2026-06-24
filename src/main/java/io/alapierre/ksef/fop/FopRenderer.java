package io.alapierre.ksef.fop;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.configuration.Configuration;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.OutputStream;
import java.net.URI;

class FopRenderer {

    private static final String MIME_PDF = "application/pdf";

    private final FopFactory fopFactory;
    private final Object renderLock = new Object();

    FopRenderer(Configuration fopConfiguration) {
        this.fopFactory = createFopFactory(fopConfiguration);
    }

    void render(OutputStream out, RenderOperation operation) throws FOPException, TransformerException {
        synchronized (renderLock) {
            operation.render(newFop(out));
        }
    }

    private Fop newFop(OutputStream out) throws FOPException {
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        return fopFactory.newFop(MIME_PDF, foUserAgent, out);
    }

    private static FopFactory createFopFactory(Configuration fopConfiguration) {
        URI baseUri = new File(".").toURI();
        ClasspathResourceResolver resourceResolver = new ClasspathResourceResolver();
        InternalResourceResolver internalResourceResolver =
                ResourceResolverFactory.createInternalResourceResolver(baseUri, resourceResolver);
        FopFactoryBuilder builder = new FopFactoryBuilder(baseUri, resourceResolver);

        builder.setConfiguration(fopConfiguration);
        builder.getFontManager().setResourceResolver(internalResourceResolver);
        return builder.build();
    }

    interface RenderOperation {
        void render(Fop fop) throws FOPException, TransformerException;
    }
}
