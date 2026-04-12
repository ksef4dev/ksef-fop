<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:crd="http://crd.gov.pl/wzor/2025/06/25/13775/"
                xmlns:local="urn:local"
				xmlns:xs="http://www.w3.org/2001/XMLSchema"
				>

    <!-- Parameter for controlling decimal places in unit prices -->
    <xsl:param name="useExtendedDecimalPlaces" select="false()"/>

    <!-- Note: $labels parameter and kLabels key are defined in the main ksef_invoice.xsl -->

    <!-- Attribute sets required for table styling -->
    <xsl:attribute-set name="tableBorder">
        <xsl:attribute name="border">solid 0.2mm black</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="tableHeaderFont">
        <xsl:attribute name="font-size">7</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="tableFont">
        <xsl:attribute name="font-size">7</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="table.cell.padding">
        <xsl:attribute name="padding-left">4pt</xsl:attribute>
        <xsl:attribute name="padding-right">4pt</xsl:attribute>
        <xsl:attribute name="padding-top">4pt</xsl:attribute>
        <xsl:attribute name="padding-bottom">4pt</xsl:attribute>
    </xsl:attribute-set>

    <!-- Template for rendering the positions table -->
    <xsl:template name="positionsTable">
        <xsl:param name="faWiersz"/>

        <!-- Calculate column width for name based on presence of other columns -->
        <!-- Fixed columns: Lp (4%), Quantity (8%), Unit (5%) = 17% -->
        <!-- Optional columns: KwotaAkcyzy (8%), P_6A (9%), P_9A (10%), P_9B (10%), P_10 (7%), P_12 (8%), P_11 (10%), P_11Vat (7%), P_11A (10%) -->
        <!-- Classification codes (Indeks, GTIN, PKWiU, CN, PKOB) are displayed under the product name -->
        <xsl:variable name="nameColumnWidth">
            <xsl:variable name="fixedWidth" select="17"/> <!-- Lp + Quantity + Unit -->
            <xsl:variable name="kwotaAkcyzyWidth" select="if ($faWiersz/crd:KwotaAkcyzy) then 8 else 0"/>
            <xsl:variable name="p6aWidth" select="if ($faWiersz/crd:P_6A) then 9 else 0"/>
            <xsl:variable name="p9aWidth" select="if ($faWiersz/crd:P_9A) then 10 else 0"/>
            <xsl:variable name="p9bWidth" select="if ($faWiersz/crd:P_9B) then 10 else 0"/>
            <xsl:variable name="p10Width" select="if ($faWiersz/crd:P_10) then 7 else 0"/>
            <xsl:variable name="p12Width" select="if ($faWiersz/crd:P_12) then 8 else 0"/>
            <xsl:variable name="p11Width" select="if ($faWiersz/crd:P_11) then 10 else 0"/>
            <xsl:variable name="p11vatWidth" select="if ($faWiersz/crd:P_11Vat) then 7 else 0"/>
            <xsl:variable name="p11aWidth" select="if ($faWiersz/crd:P_11A) then 10 else 0"/>
            <xsl:variable name="calculatedWidth" select="100 - $fixedWidth - $kwotaAkcyzyWidth - $p6aWidth - $p9aWidth - $p9bWidth - $p10Width - $p12Width - $p11Width - $p11vatWidth - $p11aWidth"/>
            <xsl:value-of select="concat($calculatedWidth, '%')"/>
        </xsl:variable>

        <!-- Define the table structure -->
        <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-after="5mm">
            <!-- Define table columns -->
            <fo:table-column column-width="4%"/> <!-- Lp. -->
            <xsl:if test="$faWiersz/crd:KwotaAkcyzy">
                <fo:table-column column-width="8%"/> <!-- Kwota akcyzy -->
            </xsl:if>
            <fo:table-column column-width="{$nameColumnWidth}"/> <!-- Nazwa - dynamiczna szerokość -->
            <fo:table-column column-width="8%"/> <!-- Ilość -->
            <fo:table-column column-width="5%"/> <!-- Jednostka -->
            <xsl:if test="$faWiersz/crd:P_9A">
                <fo:table-column column-width="10%"/> <!-- Cena jednostkowa netto -->
            </xsl:if>
            <xsl:if test="$faWiersz/crd:P_9B">
                <fo:table-column column-width="10%"/> <!-- Cena jednostkowa brutto -->
            </xsl:if>
            <xsl:if test="$faWiersz/crd:P_10">
                <fo:table-column column-width="7%"/> <!-- Rabat -->
            </xsl:if>
            <xsl:if test="$faWiersz/crd:P_12">
                <fo:table-column column-width="8%"/> <!-- Stawka podatku -->
            </xsl:if>
            <xsl:if test="$faWiersz/crd:P_11">
                <fo:table-column column-width="10%"/> <!-- Wartość sprzedaży netto-->
            </xsl:if>
            <xsl:if test="$faWiersz/crd:P_11Vat">
                <fo:table-column column-width="7%"/> <!-- Kwota VAT-->
            </xsl:if>
            <xsl:if test="$faWiersz/crd:P_11A">
                <fo:table-column column-width="10%"/> <!-- Wartość sprzedaży brutto-->
            </xsl:if>
            <xsl:if test="$faWiersz/crd:P_6A">
                <fo:table-column column-width="9%"/> <!-- Data dostawy (P_6A) -->
            </xsl:if>

            <!-- Table header -->
            <fo:table-header>
                <fo:table-row background-color="#f5f5f5" font-weight="bold">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.lp', $labels)"/></fo:block>
                    </fo:table-cell>
                    <xsl:if test="$faWiersz/crd:KwotaAkcyzy">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.exciseAmount', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.productName', $labels)"/></fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.quantity', $labels)"/></fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.unit', $labels)"/></fo:block>
                    </fo:table-cell>
                    <xsl:if test="$faWiersz/crd:P_9A">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.unitPriceNet', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_9B">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.unitPriceGross', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_10">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.discount', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_12">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.taxRate', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_11">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.netValue', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_11Vat">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.vatAmount', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_11A">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.grossValue', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_6A">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.deliveryDate', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                </fo:table-row>
            </fo:table-header>

                    <!-- Table body -->
        <fo:table-body>
            <!-- Apply templates to each position; tunnel column visibility so row output matches this table's columns (correction before/after can differ) -->
            <xsl:apply-templates select="$faWiersz">
                <xsl:with-param name="showKwotaAkcyzy" select="boolean($faWiersz/crd:KwotaAkcyzy)" tunnel="yes"/>
                <xsl:with-param name="showP6A" select="boolean($faWiersz/crd:P_6A)" tunnel="yes"/>
                <xsl:with-param name="showP9A" select="boolean($faWiersz/crd:P_9A)" tunnel="yes"/>
                <xsl:with-param name="showP9B" select="boolean($faWiersz/crd:P_9B)" tunnel="yes"/>
                <xsl:with-param name="showP10" select="boolean($faWiersz/crd:P_10)" tunnel="yes"/>
                <xsl:with-param name="showP12" select="boolean($faWiersz/crd:P_12)" tunnel="yes"/>
                <xsl:with-param name="showP11" select="boolean($faWiersz/crd:P_11)" tunnel="yes"/>
                <xsl:with-param name="showP11Vat" select="boolean($faWiersz/crd:P_11Vat)" tunnel="yes"/>
                <xsl:with-param name="showP11A" select="boolean($faWiersz/crd:P_11A)" tunnel="yes"/>
            </xsl:apply-templates>
        </fo:table-body>
        </fo:table>
    </xsl:template>

    <!-- Template for each position. Column visibility from tunnel params so correction before/after tables can have different columns. -->
    <xsl:template match="crd:FaWiersz">
        <xsl:param name="showKwotaAkcyzy" select="boolean(//crd:FaWiersz/crd:KwotaAkcyzy)" tunnel="yes"/>
        <xsl:param name="showP6A" select="boolean(//crd:FaWiersz/crd:P_6A)" tunnel="yes"/>
        <xsl:param name="showP9A" select="boolean(//crd:FaWiersz/crd:P_9A)" tunnel="yes"/>
        <xsl:param name="showP9B" select="boolean(//crd:FaWiersz/crd:P_9B)" tunnel="yes"/>
        <xsl:param name="showP10" select="boolean(//crd:FaWiersz/crd:P_10)" tunnel="yes"/>
        <xsl:param name="showP12" select="boolean(//crd:FaWiersz/crd:P_12)" tunnel="yes"/>
        <xsl:param name="showP11" select="boolean(//crd:FaWiersz/crd:P_11)" tunnel="yes"/>
        <xsl:param name="showP11Vat" select="boolean(//crd:FaWiersz/crd:P_11Vat)" tunnel="yes"/>
        <xsl:param name="showP11A" select="boolean(//crd:FaWiersz/crd:P_11A)" tunnel="yes"/>
        <fo:table-row>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:value-of select="crd:NrWierszaFa"/> <!-- Lp -->
                </fo:block>
            </fo:table-cell>
            <xsl:if test="$showKwotaAkcyzy">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:KwotaAkcyzy">
                                <xsl:variable name="formattedNumber" select="local:format-amount(crd:KwotaAkcyzy)"/>
                                <xsl:choose>
                                    <xsl:when test="string-length($formattedNumber) &gt; 8">
                                        <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$formattedNumber"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <fo:block/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" padding-left="3pt">
                <fo:block>
                    <xsl:value-of select="crd:P_7"/> <!-- Nazwa -->
                </fo:block>
                <xsl:if test="crd:Indeks">
                    <fo:block font-size="6pt" color="#555555">
                        <xsl:value-of select="key('kLabels', 'row.indeks', $labels)"/>: <xsl:value-of select="crd:Indeks"/>
                    </fo:block>
                </xsl:if>
                <xsl:if test="crd:GTIN">
                    <fo:block font-size="6pt" color="#555555">
                        GTIN: <xsl:value-of select="crd:GTIN"/>
                    </fo:block>
                </xsl:if>
                <xsl:if test="crd:PKWiU">
                    <fo:block font-size="6pt" color="#555555">
                        PKWiU: <xsl:value-of select="crd:PKWiU"/>
                    </fo:block>
                </xsl:if>
                <xsl:if test="crd:CN">
                    <fo:block font-size="6pt" color="#555555">
                        CN: <xsl:value-of select="crd:CN"/>
                    </fo:block>
                </xsl:if>
                <xsl:if test="crd:PKOB">
                    <fo:block font-size="6pt" color="#555555">
                        PKOB: <xsl:value-of select="crd:PKOB"/>
                    </fo:block>
                </xsl:if>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                <xsl:variable name="formattedQty" select="local:format-quantity(crd:P_8B)"/>
                <xsl:variable name="qtyLength" select="string-length($formattedQty)"/>
                <xsl:choose>
                    <xsl:when test="$qtyLength &gt; 14">
                        <fo:block font-size="5pt">
                            <xsl:value-of select="$formattedQty"/> <!-- Ilość -->
                        </fo:block>
                    </xsl:when>
                    <xsl:when test="$qtyLength &gt; 10">
                        <fo:block font-size="6pt">
                            <xsl:value-of select="$formattedQty"/> <!-- Ilość -->
                        </fo:block>
                    </xsl:when>
                    <xsl:otherwise>
                        <fo:block>
                            <xsl:value-of select="$formattedQty"/> <!-- Ilość -->
                        </fo:block>
                    </xsl:otherwise>
                </xsl:choose>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                <fo:block>
                    <xsl:value-of select="crd:P_8A"/> <!-- Jednostka -->
                </fo:block>
            </fo:table-cell>
            <xsl:if test="$showP9A">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_9A">
                                <xsl:variable name="formattedNumber">
                                    <xsl:value-of select="local:format-unit-price(crd:P_9A)"/>
                                </xsl:variable>
                                <xsl:choose>
                                    <xsl:when test="string-length($formattedNumber) > 8">
                                        <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$formattedNumber"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <fo:block/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="$showP9B">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_9B">
                                <xsl:variable name="formattedNumber">
                                    <xsl:value-of select="local:format-unit-price(crd:P_9B)"/>
                                </xsl:variable>
                                <xsl:choose>
                                    <xsl:when test="string-length($formattedNumber) > 8">
                                        <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$formattedNumber"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <fo:block/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="$showP10">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_10">
                                <xsl:variable name="formattedNumber" select="local:format-unit-price(crd:P_10)"/>
                                <xsl:choose>
                                    <xsl:when test="string-length($formattedNumber) > 8">
                                        <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$formattedNumber"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <fo:block/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="$showP12">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:variable name="taxRate" select="crd:P_12"/>
						<xsl:choose>
						  <xsl:when test="$taxRate castable as xs:decimal">
							<xsl:value-of select="$taxRate"/>%
						  </xsl:when>
						  <xsl:otherwise>
							<xsl:value-of select="$taxRate"/>
						  </xsl:otherwise>
						</xsl:choose>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="$showP11">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_11">
                                <xsl:variable name="formattedNumber" select="local:format-amount(crd:P_11)"/>
                                <xsl:choose>
                                    <xsl:when test="string-length($formattedNumber) > 8">
                                        <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$formattedNumber"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <fo:block/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="$showP11Vat">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_11Vat">
                                <xsl:variable name="formattedNumber" select="local:format-amount(crd:P_11Vat)"/>
                                <xsl:choose>
                                    <xsl:when test="string-length($formattedNumber) > 8">
                                        <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$formattedNumber"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <fo:block/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="$showP11A">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_11A">
                                <xsl:variable name="formattedNumber" select="local:format-amount(crd:P_11A)"/>
                                <xsl:choose>
                                    <xsl:when test="string-length($formattedNumber) > 8">
                                        <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$formattedNumber"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <fo:block/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="$showP6A">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="center">
                    <fo:block>
                        <xsl:value-of select="crd:P_6A"/> <!-- Data dostawy (P_6A) -->
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="differencesTable">
        <xsl:param name="faWierszBefore"/>
        <xsl:param name="faWierszAfter"/>

        <!-- Column visibility: show column if it appears in EITHER before OR after (union), so differing structures don't break the table -->
        <xsl:variable name="diffShowKwotaAkcyzy" select="boolean($faWierszBefore/crd:KwotaAkcyzy or $faWierszAfter/crd:KwotaAkcyzy)"/>
        <xsl:variable name="diffShowP6A" select="boolean($faWierszBefore/crd:P_6A or $faWierszAfter/crd:P_6A)"/>
        <xsl:variable name="diffShowP9A" select="boolean($faWierszBefore/crd:P_9A or $faWierszAfter/crd:P_9A)"/>
        <xsl:variable name="diffShowP9B" select="boolean($faWierszBefore/crd:P_9B or $faWierszAfter/crd:P_9B)"/>
        <xsl:variable name="diffShowP10" select="boolean($faWierszBefore/crd:P_10 or $faWierszAfter/crd:P_10)"/>
        <xsl:variable name="diffShowP11" select="boolean($faWierszBefore/crd:P_11 or $faWierszAfter/crd:P_11)"/>
        <xsl:variable name="diffShowP11Vat" select="boolean($faWierszBefore/crd:P_11Vat or $faWierszAfter/crd:P_11Vat)"/>
        <xsl:variable name="diffShowP11A" select="boolean($faWierszBefore/crd:P_11A or $faWierszAfter/crd:P_11A)"/>

        <!-- Calculate column width for name based on union of optional columns -->
        <xsl:variable name="nameColumnWidth">
            <xsl:variable name="fixedWidth" select="17"/>
            <xsl:variable name="kwotaAkcyzyWidth" select="if ($diffShowKwotaAkcyzy) then 8 else 0"/>
            <xsl:variable name="p6aWidth" select="if ($diffShowP6A) then 9 else 0"/>
            <xsl:variable name="p9aWidth" select="if ($diffShowP9A) then 10 else 0"/>
            <xsl:variable name="p9bWidth" select="if ($diffShowP9B) then 10 else 0"/>
            <xsl:variable name="p10Width" select="if ($diffShowP10) then 7 else 0"/>
            <xsl:variable name="p12Width" select="8"/>
            <xsl:variable name="p11Width" select="if ($diffShowP11) then 10 else 0"/>
            <xsl:variable name="p11vatWidth" select="if ($diffShowP11Vat) then 7 else 0"/>
            <xsl:variable name="p11aWidth" select="if ($diffShowP11A) then 10 else 0"/>
            <xsl:variable name="calculatedWidth" select="100 - $fixedWidth - $kwotaAkcyzyWidth - $p6aWidth - $p9aWidth - $p9bWidth - $p10Width - $p12Width - $p11Width - $p11vatWidth - $p11aWidth"/>
            <xsl:value-of select="concat($calculatedWidth, '%')"/>
        </xsl:variable>

        <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-after="5mm">
            <fo:table-column column-width="4%"/> <!-- Lp. -->
            <xsl:if test="$diffShowKwotaAkcyzy">
                <fo:table-column column-width="8%"/> <!-- Kwota akcyzy -->
            </xsl:if>
            <fo:table-column column-width="{$nameColumnWidth}"/> <!-- Nazwa -->
            <fo:table-column column-width="8%"/> <!-- Ilość -->
            <fo:table-column column-width="5%"/> <!-- Jednostka -->
            <xsl:if test="$diffShowP9A">
                <fo:table-column column-width="10%"/> <!-- Cena jednostkowa netto -->
            </xsl:if>
            <xsl:if test="$diffShowP9B">
                <fo:table-column column-width="10%"/> <!-- Cena jednostkowa brutto -->
            </xsl:if>
            <xsl:if test="$diffShowP10">
                <fo:table-column column-width="7%"/> <!-- Rabat -->
            </xsl:if>
            <fo:table-column column-width="8%"/> <!-- Stawka podatku -->
            <xsl:if test="$diffShowP11">
                <fo:table-column column-width="10%"/> <!-- Wartość sprzedaży netto-->
            </xsl:if>
            <xsl:if test="$diffShowP11Vat">
                <fo:table-column column-width="7%"/> <!-- Kwota VAT-->
            </xsl:if>
            <xsl:if test="$diffShowP11A">
                <fo:table-column column-width="10%"/> <!-- Wartość sprzedaży brutto-->
            </xsl:if>
            <xsl:if test="$diffShowP6A">
                <fo:table-column column-width="9%"/> <!-- Data dostawy (P_6A) -->
            </xsl:if>

            <!-- Table header -->
            <fo:table-header>
                <fo:table-row background-color="#f5f5f5" font-weight="bold">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.lp', $labels)"/></fo:block>
                    </fo:table-cell>
                    <xsl:if test="$diffShowKwotaAkcyzy">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.exciseAmount', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.productName', $labels)"/></fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.quantity', $labels)"/></fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.unit', $labels)"/></fo:block>
                    </fo:table-cell>
                    <xsl:if test="$diffShowP9A">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.unitPriceNet', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$diffShowP9B">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.unitPriceGross', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$diffShowP10">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.discount', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.taxRate', $labels)"/></fo:block>
                    </fo:table-cell>
                    <xsl:if test="$diffShowP11">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.netValue', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$diffShowP11Vat">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.vatAmount', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$diffShowP11A">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.grossValue', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$diffShowP6A">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.deliveryDate', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                </fo:table-row>
            </fo:table-header>

            <!-- Table body -->
            <fo:table-body>
                <!-- Process each row to calculate and show differences -->
                <xsl:for-each select="$faWierszAfter">
                    <xsl:variable name="lineNum" select="crd:NrWierszaFa"/>
                    <xsl:variable name="after" select="."/>
                    <xsl:variable name="before" select="$faWierszBefore[crd:NrWierszaFa = $lineNum]"/>

                    <!-- New flag to determine if this is a new row not in "before" -->
                    <xsl:variable name="isNewRow" select="not($before)"/>
                        <fo:table-row>
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                <fo:block><xsl:value-of select="$lineNum"/></fo:block>
                            </fo:table-cell>
                            <xsl:if test="$diffShowKwotaAkcyzy">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:choose>
                                            <xsl:when test="$after/crd:KwotaAkcyzy">
                                                <xsl:variable name="formattedNumber" select="local:format-amount($after/crd:KwotaAkcyzy)"/>
                                                <xsl:choose>
                                                    <xsl:when test="string-length($formattedNumber) &gt; 8">
                                                        <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="$formattedNumber"/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <fo:block/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </fo:block>
                                </fo:table-cell>
                            </xsl:if>
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" padding-left="3pt">
                                <fo:block><xsl:value-of select="$after/crd:P_7"/></fo:block>
                                <xsl:if test="$after/crd:Indeks">
                                    <fo:block font-size="6pt" color="#555555">
                                        <xsl:value-of select="key('kLabels', 'row.indeks', $labels)"/>: <xsl:value-of select="$after/crd:Indeks"/>
                                    </fo:block>
                                </xsl:if>
                                <xsl:if test="$after/crd:GTIN">
                                    <fo:block font-size="6pt" color="#555555">
                                        GTIN: <xsl:value-of select="$after/crd:GTIN"/>
                                    </fo:block>
                                </xsl:if>
                                <xsl:if test="$after/crd:PKWiU">
                                    <fo:block font-size="6pt" color="#555555">
                                        PKWiU: <xsl:value-of select="$after/crd:PKWiU"/>
                                    </fo:block>
                                </xsl:if>
                                <xsl:if test="$after/crd:CN">
                                    <fo:block font-size="6pt" color="#555555">
                                        CN: <xsl:value-of select="$after/crd:CN"/>
                                    </fo:block>
                                </xsl:if>
                                <xsl:if test="$after/crd:PKOB">
                                    <fo:block font-size="6pt" color="#555555">
                                        PKOB: <xsl:value-of select="$after/crd:PKOB"/>
                                    </fo:block>
                                </xsl:if>
                            </fo:table-cell>

                            <!-- Quantity - for new rows, show exact "after" value, otherwise show difference -->
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                <xsl:variable name="formattedQty">
                                    <xsl:choose>
                                        <xsl:when test="$isNewRow">
                                            <xsl:value-of select="local:format-quantity($after/crd:P_8B)"/>
                                        </xsl:when>
                                        <xsl:when test="$before/crd:P_8B and $after/crd:P_8B">
                                            <xsl:value-of select="local:format-quantity(number($after/crd:P_8B) - number($before/crd:P_8B))"/>
                                        </xsl:when>
                                        <xsl:when test="not($before/crd:P_8B) and $after/crd:P_8B">
                                            <xsl:value-of select="local:format-quantity($after/crd:P_8B)"/>
                                        </xsl:when>
                                        <xsl:when test="$before/crd:P_8B and not($after/crd:P_8B)">
                                            <xsl:value-of select="local:format-quantity(-number($before/crd:P_8B))"/>
                                        </xsl:when>
                                        <xsl:otherwise>0</xsl:otherwise>
                                    </xsl:choose>
                                </xsl:variable>
                                <xsl:variable name="qtyLength" select="string-length($formattedQty)"/>
                                <xsl:choose>
                                    <xsl:when test="$qtyLength &gt; 14">
                                        <fo:block font-size="5pt">
                                            <xsl:value-of select="$formattedQty"/>
                                        </fo:block>
                                    </xsl:when>
                                    <xsl:when test="$qtyLength &gt; 10">
                                        <fo:block font-size="6pt">
                                            <xsl:value-of select="$formattedQty"/>
                                        </fo:block>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <fo:block>
                                            <xsl:value-of select="$formattedQty"/>
                                        </fo:block>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </fo:table-cell>

                            <!-- Unit - show only "after" value -->
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                <fo:block>
                                    <xsl:value-of select="$after/crd:P_8A"/>
                                </fo:block>
                            </fo:table-cell>

                            <!-- Net unit price difference with font-size adjustment -->
                            <xsl:if test="$diffShowP9A">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="formattedNumber">
                                            <xsl:choose>
                                                <xsl:when test="$isNewRow and $after/crd:P_9A">
                                                    <xsl:value-of select="local:format-unit-price($after/crd:P_9A)"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_9A and $after/crd:P_9A and $before/crd:P_9A != $after/crd:P_9A">
                                                    <xsl:value-of select="local:format-unit-price(number($after/crd:P_9A) - number($before/crd:P_9A))"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_9A) and $after/crd:P_9A">
                                                    <xsl:value-of select="local:format-unit-price($after/crd:P_9A)"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_9A and not($after/crd:P_9A)">
                                                    <xsl:value-of select="local:format-unit-price(-number($before/crd:P_9A))"/>
                                                </xsl:when>
                                                <xsl:otherwise>0,00</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>

                                        <xsl:choose>
                                            <xsl:when test="string-length($formattedNumber) > 8">
                                                <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$formattedNumber"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </fo:block>
                                </fo:table-cell>
                            </xsl:if>

                            <!-- Gross unit price difference with font-size adjustment -->
                            <xsl:if test="$diffShowP9B">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="formattedNumber">
                                            <xsl:choose>
                                                <xsl:when test="$isNewRow and $after/crd:P_9B">
                                                    <xsl:value-of select="local:format-unit-price($after/crd:P_9B)"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_9B and $after/crd:P_9B and $before/crd:P_9B != $after/crd:P_9B">
                                                    <xsl:value-of select="local:format-unit-price(number($after/crd:P_9B) - number($before/crd:P_9B))"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_9B) and $after/crd:P_9B">
                                                    <xsl:value-of select="local:format-unit-price($after/crd:P_9B)"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_9B and not($after/crd:P_9B)">
                                                    <xsl:value-of select="local:format-unit-price(-number($before/crd:P_9B))"/>
                                                </xsl:when>
                                                <xsl:otherwise>0,00</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>

                                        <xsl:choose>
                                            <xsl:when test="string-length($formattedNumber) > 8">
                                                <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$formattedNumber"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </fo:block>
                                </fo:table-cell>
                            </xsl:if>

                            <!-- Discount difference with font-size adjustment -->
                            <xsl:if test="$diffShowP10">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="formattedNumber">
                                            <xsl:choose>
                                                <xsl:when test="$isNewRow and $after/crd:P_10">
                                                    <xsl:value-of select="local:format-unit-price($after/crd:P_10)"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_10 and $after/crd:P_10 and $before/crd:P_10 != $after/crd:P_10">
                                                    <xsl:value-of select="local:format-unit-price(number($after/crd:P_10) - number($before/crd:P_10))"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_10) and $after/crd:P_10">
                                                    <xsl:value-of select="local:format-unit-price($after/crd:P_10)"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_10 and not($after/crd:P_10)">
                                                    <xsl:value-of select="local:format-unit-price(-number($before/crd:P_10))"/>
                                                </xsl:when>
                                                <xsl:otherwise>0,00</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>

                                        <xsl:choose>
                                            <xsl:when test="string-length($formattedNumber) > 8">
                                                <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$formattedNumber"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </fo:block>
                                </fo:table-cell>
                            </xsl:if>

                            <!-- Tax rate - show only "after" value -->
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                <fo:block>
                                    <xsl:variable name="taxRate" select="$after/crd:P_12"/>
                                    <xsl:choose>
                                        <xsl:when test="number($taxRate) = $taxRate and $taxRate != ''">
                                            <xsl:value-of select="$taxRate"/>%
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="$taxRate"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:block>
                            </fo:table-cell>

                            <!-- Net value difference with font-size adjustment -->
                            <xsl:if test="$diffShowP11">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="formattedNumber">
                                            <xsl:choose>
                                                <xsl:when test="$isNewRow and $after/crd:P_11">
                                                    <xsl:value-of select="local:format-amount($after/crd:P_11)"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_11 and $after/crd:P_11 and $before/crd:P_11 != $after/crd:P_11">
                                                    <xsl:value-of select="local:format-amount(number($after/crd:P_11) - number($before/crd:P_11))"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_11) and $after/crd:P_11">
                                                    <xsl:value-of select="local:format-amount($after/crd:P_11)"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_11 and not($after/crd:P_11)">
                                                    <xsl:value-of select="local:format-amount(-number($before/crd:P_11))"/>
                                                </xsl:when>
                                                <xsl:otherwise>0,00</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>

                                        <xsl:choose>
                                            <xsl:when test="string-length($formattedNumber) > 8">
                                                <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$formattedNumber"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </fo:block>
                                </fo:table-cell>
                            </xsl:if>

                            <!-- VAT amount difference with font-size adjustment -->
                            <xsl:if test="$diffShowP11Vat">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="formattedNumber">
                                            <xsl:choose>
                                                <xsl:when test="$isNewRow and $after/crd:P_11Vat">
                                                    <xsl:value-of select="local:format-amount($after/crd:P_11Vat)"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_11Vat and $after/crd:P_11Vat and $before/crd:P_11Vat != $after/crd:P_11Vat">
                                                    <xsl:value-of select="local:format-amount(number($after/crd:P_11Vat) - number($before/crd:P_11Vat))"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_11Vat) and $after/crd:P_11Vat">
                                                    <xsl:value-of select="local:format-amount($after/crd:P_11Vat)"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_11Vat and not($after/crd:P_11Vat)">
                                                    <xsl:value-of select="local:format-amount(-number($before/crd:P_11Vat))"/>
                                                </xsl:when>
                                                <xsl:otherwise>0,00</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>

                                        <xsl:choose>
                                            <xsl:when test="string-length($formattedNumber) > 8">
                                                <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$formattedNumber"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </fo:block>
                                </fo:table-cell>
                            </xsl:if>

                            <!-- Gross value difference with font-size adjustment -->
                            <xsl:if test="$diffShowP11A">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="formattedNumber">
                                            <xsl:choose>
                                                <xsl:when test="$isNewRow and $after/crd:P_11A">
                                                    <xsl:value-of select="local:format-amount($after/crd:P_11A)"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_11A and $after/crd:P_11A and $before/crd:P_11A != $after/crd:P_11A">
                                                    <xsl:value-of select="local:format-amount(number($after/crd:P_11A) - number($before/crd:P_11A))"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_11A) and $after/crd:P_11A">
                                                    <xsl:value-of select="local:format-amount($after/crd:P_11A)"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_11A and not($after/crd:P_11A)">
                                                    <xsl:value-of select="local:format-amount(-number($before/crd:P_11A))"/>
                                                </xsl:when>
                                                <xsl:otherwise>0,00</xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:variable>
                                        
                                        <xsl:choose>
                                            <xsl:when test="string-length($formattedNumber) > 8">
                                                <fo:inline font-size="6pt"><xsl:value-of select="$formattedNumber"/></fo:inline>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$formattedNumber"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </fo:block>
                                </fo:table-cell>
                            </xsl:if>
                            <!-- P_6A (Data dostawy) - show only "after" value -->
                            <xsl:if test="$diffShowP6A">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="center">
                                    <fo:block>
                                        <xsl:value-of select="$after/crd:P_6A"/>
                                    </fo:block>
                                </fo:table-cell>
                            </xsl:if>
                        </fo:table-row>
                </xsl:for-each>
            </fo:table-body>
        </fo:table>
    </xsl:template>

</xsl:stylesheet>
