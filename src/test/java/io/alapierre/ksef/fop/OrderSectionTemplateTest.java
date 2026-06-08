package io.alapierre.ksef.fop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Node;
import org.xmlunit.assertj3.XmlAssert;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the transformation of the {@code crd:Zamowienie} ("Zamówienie") order section.
 *
 * <p>Each fixture below carries a {@code <Zamowienie>} on a different code path:</p>
 *
 * <ul>
 *   <li>{@code basic} — plain VAT invoice (faktura podstawowa) with invoice lines and an order list.</li>
 *   <li>{@code correction} — correction invoice (korekta, KOR) carrying an order list.</li>
 *   <li>{@code advance} — ZAL.</li>
 *   <li>{@code settlement} — ROZ, a non-ZAL invoice.</li>
 *   <li>{@code correction_stan_przed} — KOR with before/after invoice lines (the order has no StanPrzedZ).</li>
 *   <li>{@code order_correction} — order rows carry StanPrzedZ, so the section splits into before/after subsections.</li>
 *   <li>{@code order_before_only} — order rows carry only StanPrzedZ=1, so only the before subsection shows.</li>
 * </ul>
 *
 * @see <a href="https://github.com/ksef4dev/ksef-fop/issues/136">issue #136</a>
 */
class OrderSectionTemplateTest extends AbstractStyleSheetTest {

    @ParameterizedTest
    @ValueSource(strings = {"basic", "correction", "advance", "settlement", "correction_stan_przed", "order_correction", "order_before_only"})
    void testTemplate(String resourceName) throws Exception {
        URL inputUrl = resource("OrderSectionTemplateTest/" + resourceName + ".xml");
        URL expectedUrl = resource("OrderSectionTemplateTest/" + resourceName + "-expected.xml");

        Node actual = transformFa3Invoice(inputUrl, "/fa3:Faktura", "//fo:block[@id='Zamowienie']");

        XmlAssert.assertThat(actual)
                .and(expectedUrl)
                .ignoreWhitespace()
                .areSimilar();
    }

    /** With showCorrectionDifferences on, the order section adds a differences table between before and after. */
    @Test
    void orderSectionShowsDifferencesWhenEnabled() throws Exception {
        URL inputUrl = resource("OrderSectionTemplateTest/order_correction.xml");
        URL expectedUrl = resource("OrderSectionTemplateTest/order_correction-differences-expected.xml");

        Node actual = transformFa3Invoice(inputUrl, "/fa3:Faktura", "//fo:block[@id='Zamowienie']", true);

        XmlAssert.assertThat(actual)
                .and(expectedUrl)
                .ignoreWhitespace()
                .areSimilar();
    }

    /** No {@code <Zamowienie>} in the source ⇒ no order block in the output. */
    @Test
    void orderSectionIsAbsentWhenNoOrder() throws Exception {
        URL inputUrl = resource("OrderSectionTemplateTest/no_order.xml");

        Node orderBlock = transformFa3Invoice(inputUrl, "/fa3:Faktura", "//fo:block[@id='Zamowienie']");

        assertNull(orderBlock, "Order section must not be emitted when the invoice has no <Zamowienie>");
    }
}
