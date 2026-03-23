package io.alapierre.ksef.fop;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the visualization of {@code Podmiot} element in the invoice XML.
 */
class PodmiotTemplateTest extends AbstractGeneratePdfTest {


    @ParameterizedTest
    @ValueSource(strings = {
            "minimal_3_parties",
            "full_3_parties",
            "correspondence_address",
            "court_bailiff",
            "enforcement_authority",
            "tax_representative",
            "multiple_rola_inna",
            "multiple_roles"
    })
    void testOptionalNazwaAndAdres(String resource) throws Exception {
        String actual = generateFa3InvoiceText("faktury/PodmiotTemplateTest/" + resource + ".xml");
        assertEquals(textResource("faktury/PodmiotTemplateTest/" + resource + "-expected.txt"), actual);
    }
}
