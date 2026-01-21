package io.alapierre.ksef.fop;

import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class ClasspathResourceResolver implements ResourceResolver {

    private final ResourceResolver delegate;

    public ClasspathResourceResolver() {
        this.delegate = ResourceResolverFactory.createDefaultResourceResolver();
    }

    @Override
    public Resource getResource(URI uri) throws IOException {
        if (classpath(uri)) {
            String path = uri.getSchemeSpecificPart();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            final InputStream is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(path);

            if (is == null) {
                throw new IllegalArgumentException("Classpath resource not found: " + uri);
            }

            return new Resource(is);
        }

        // fallback to default FOP resolver
        return delegate.getResource(uri);
    }

    @Override
    public OutputStream getOutputStream(URI uri) throws IOException {
        if (classpath(uri)) {
            throw new UnsupportedOperationException("Writing to classpath resources is not supported: " + uri);
        }

        return delegate.getOutputStream(uri);
    }

    private static boolean classpath(URI uri) {
        return uri != null && "classpath".equals(uri.getScheme());
    }
}
