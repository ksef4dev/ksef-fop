package io.alapierre.ksef.fop;

import org.junit.jupiter.api.Test;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import static org.junit.jupiter.api.Assertions.*;

class ClasspathUriResolverTest {

    @Test
    void shouldResolveXsdFromClasspath() throws Exception {
        ClasspathUriResolver resolver = new ClasspathUriResolver();

        String href = "http://crd.gov.pl/xml/schematy/dziedzinowe/mf/2022/01/05/eD/DefinicjeTypy/KodyKrajow_v10-0E.xsd";

        Source s = resolver.resolve(href, null);

        assertNotNull(s, "Resolver should return Source");
        assertTrue(s instanceof StreamSource);
        assertNotNull(((StreamSource) s).getInputStream(), "InputStream should not be null");
    }
}
