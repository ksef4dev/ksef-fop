package io.alapierre.ksef.fop.internal.chunk;

import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceRowCounterTest {

    @Test
    void countsOnlyDirectPositionsOfTheInvoice() throws Exception {
        String xml = "<Faktura xmlns=\"urn:test\"><Fa>"
                + "<FaWiersz/><FaWiersz/><FaWiersz/>"
                + "<Zamowienie><FaWiersz/></Zamowienie>"
                + "</Fa></Faktura>";

        assertEquals(3, InvoiceRowCounter.countRows(xml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void rejectsDoctypeDeclarations() {
        String xml = "<!DOCTYPE Faktura [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><Faktura><Fa>&x;</Fa></Faktura>";

        assertThrows(XMLStreamException.class, () -> InvoiceRowCounter.countRows(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
