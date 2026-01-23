package io.alapierre.ksef.fop;

import net.sf.saxon.lib.StandardURIResolver;
import net.sf.saxon.trans.XPathException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;


public class ClasspathUriResolver extends StandardURIResolver {

    private static final String BASE = "/xsd/";

    @Override
    public Source resolve(String href, String base) throws XPathException {

        if (href == null) {
            return super.resolve(href, base);
        }

        String fileName = href.substring(href.lastIndexOf('/') + 1);
        InputStream is = getClass().getResourceAsStream(BASE + fileName);

        if (is != null) {
            StreamSource source = new StreamSource(is);
            source.setSystemId(BASE + fileName);
            return source;
        }

        return super.resolve(href, base);
    }
}
