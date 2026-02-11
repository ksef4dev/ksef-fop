<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:crd="http://crd.gov.pl/wzor/2025/06/25/13775/"
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
        <!-- Optional columns: Indeks (8%), P_6A (9%), P_9A (10%), P_9B (10%), P_10 (7%), P_12 (8%), P_11 (10%), P_11Vat (7%), P_11A (10%) -->
        <xsl:variable name="nameColumnWidth">
            <xsl:variable name="fixedWidth" select="17"/> <!-- Lp + Quantity + Unit -->
            <xsl:variable name="indeksWidth" select="if ($faWiersz/crd:Indeks) then 8 else 0"/>
            <xsl:variable name="p6aWidth" select="if ($faWiersz/crd:P_6A) then 9 else 0"/>
            <xsl:variable name="p9aWidth" select="if ($faWiersz/crd:P_9A) then 10 else 0"/>
            <xsl:variable name="p9bWidth" select="if ($faWiersz/crd:P_9B) then 10 else 0"/>
            <xsl:variable name="p10Width" select="if ($faWiersz/crd:P_10) then 7 else 0"/>
            <xsl:variable name="p12Width" select="if ($faWiersz/crd:P_12) then 8 else 0"/>
            <xsl:variable name="p11Width" select="if ($faWiersz/crd:P_11) then 10 else 0"/>
            <xsl:variable name="p11vatWidth" select="if ($faWiersz/crd:P_11Vat) then 7 else 0"/>
            <xsl:variable name="p11aWidth" select="if ($faWiersz/crd:P_11A) then 10 else 0"/>
            <xsl:variable name="calculatedWidth" select="100 - $fixedWidth - $indeksWidth - $p6aWidth - $p9aWidth - $p9bWidth - $p10Width - $p12Width - $p11Width - $p11vatWidth - $p11aWidth"/>
            <xsl:value-of select="concat($calculatedWidth, '%')"/>
        </xsl:variable>

        <!-- Define the table structure -->
        <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-after="5mm">
            <!-- Define table columns -->
            <fo:table-column column-width="4%"/> <!-- Lp. -->
            <xsl:if test="$faWiersz/crd:Indeks">
                <fo:table-column column-width="8%"/> <!-- Indeks -->
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
                    <xsl:if test="$faWiersz/crd:Indeks">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.indeks', $labels)"/></fo:block>
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
            <!-- Apply templates to each position -->
            <xsl:apply-templates select="$faWiersz"/>
        </fo:table-body>
        </fo:table>
    </xsl:template>

    <!-- Template for each position -->
    <xsl:template match="crd:FaWiersz">
        <fo:table-row>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:value-of select="crd:NrWierszaFa"/> <!-- Lp -->
                </fo:block>
            </fo:table-cell>
            <xsl:if test="//crd:FaWiersz/crd:Indeks">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                    <fo:block>
                        <xsl:value-of select="crd:Indeks"/> <!-- Indeks -->
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" padding-left="3pt">
                <fo:block>
                    <xsl:value-of select="crd:P_7"/> <!-- Nazwa -->
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                <xsl:variable name="formattedQty" select="translate(format-number(number(crd:P_8B), '#,##0.######'), ',', '&#160;')"/>
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
            <xsl:if test="//crd:FaWiersz/crd:P_9A">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_9A">
                                <xsl:variable name="formattedNumber">
                                    <xsl:value-of select="translate(format-number(number(crd:P_9A), '#,##0.########'), ',.', ' ,')"/>
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
            <xsl:if test="//crd:FaWiersz/crd:P_9B">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_9B">
                                <xsl:variable name="formattedNumber">
                                    <xsl:value-of select="translate(format-number(number(crd:P_9B), '#,##0.########'), ',.', ' ,')"/>
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
            <xsl:if test="//crd:FaWiersz/crd:P_10">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_10">
                                <xsl:variable name="formattedNumber" select="translate(format-number(number(crd:P_10), '#,##0.########'), ',.', ' ,')"/>
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
            <xsl:if test="//crd:FaWiersz/crd:P_12">
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
            <xsl:if test="//crd:FaWiersz/crd:P_11">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_11">
                                <xsl:variable name="formattedNumber" select="translate(format-number(number(crd:P_11), '#,##0.00'), ',.', ' ,')"/>
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
            <xsl:if test="//crd:FaWiersz/crd:P_11Vat">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_11Vat">
                                <xsl:variable name="formattedNumber" select="translate(format-number(number(crd:P_11Vat), '#,##0.00'), ',.', ' ,')"/>
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
            <xsl:if test="//crd:FaWiersz/crd:P_11A">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_11A">
                                <xsl:variable name="formattedNumber" select="translate(format-number(number(crd:P_11A), '#,##0.00'), ',.', ' ,')"/>
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
            <xsl:if test="//crd:FaWiersz/crd:P_6A">
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

        <!-- Calculate column width for name based on presence of other columns -->
        <!-- Fixed columns: Lp (4%), Quantity (8%), Unit (5%) = 17% -->
        <!-- Optional columns: Indeks (8%), P_6A (9%), P_9A (10%), P_9B (10%), P_10 (7%), P_12 (8% - always shown in differencesTable), P_11 (10%), P_11Vat (7%), P_11A (10%) -->
        <xsl:variable name="nameColumnWidth">
            <xsl:variable name="fixedWidth" select="17"/> <!-- Lp + Quantity + Unit -->
            <xsl:variable name="indeksWidth" select="if ($faWierszAfter/crd:Indeks) then 8 else 0"/>
            <xsl:variable name="p6aWidth" select="if ($faWierszAfter/crd:P_6A) then 9 else 0"/>
            <xsl:variable name="p9aWidth" select="if ($faWierszAfter/crd:P_9A) then 10 else 0"/>
            <xsl:variable name="p9bWidth" select="if ($faWierszAfter/crd:P_9B) then 10 else 0"/>
            <xsl:variable name="p10Width" select="if ($faWierszAfter/crd:P_10) then 7 else 0"/>
            <xsl:variable name="p12Width" select="8"/> <!-- Always shown in differencesTable -->
            <xsl:variable name="p11Width" select="if ($faWierszAfter/crd:P_11) then 10 else 0"/>
            <xsl:variable name="p11vatWidth" select="if ($faWierszAfter/crd:P_11Vat) then 7 else 0"/>
            <xsl:variable name="p11aWidth" select="if ($faWierszAfter/crd:P_11A) then 10 else 0"/>
            <xsl:variable name="calculatedWidth" select="100 - $fixedWidth - $indeksWidth - $p6aWidth - $p9aWidth - $p9bWidth - $p10Width - $p12Width - $p11Width - $p11vatWidth - $p11aWidth"/>
            <xsl:value-of select="concat($calculatedWidth, '%')"/>
        </xsl:variable>

        <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-after="5mm">
            <fo:table-column column-width="4%"/> <!-- Lp. -->
            <xsl:if test="$faWierszAfter/crd:Indeks">
                <fo:table-column column-width="8%"/> <!-- Indeks -->
            </xsl:if>
            <fo:table-column column-width="{$nameColumnWidth}"/> <!-- Nazwa - dynamiczna szerokość -->
            <fo:table-column column-width="8%"/> <!-- Ilość -->
            <fo:table-column column-width="5%"/> <!-- Jednostka -->
            <xsl:if test="$faWierszAfter/crd:P_9A">
                <fo:table-column column-width="10%"/> <!-- Cena jednostkowa netto -->
            </xsl:if>
            <xsl:if test="$faWierszAfter/crd:P_9B">
                <fo:table-column column-width="10%"/> <!-- Cena jednostkowa brutto -->
            </xsl:if>
            <xsl:if test="$faWierszAfter/crd:P_10">
                <fo:table-column column-width="7%"/> <!-- Rabat -->
            </xsl:if>
            <fo:table-column column-width="8%"/> <!-- Stawka podatku -->
            <xsl:if test="$faWierszAfter/crd:P_11">
                <fo:table-column column-width="10%"/> <!-- Wartość sprzedaży netto-->
            </xsl:if>
            <xsl:if test="$faWierszAfter/crd:P_11Vat">
                <fo:table-column column-width="7%"/> <!-- Kwota VAT-->
            </xsl:if>
            <xsl:if test="$faWierszAfter/crd:P_11A">
                <fo:table-column column-width="10%"/> <!-- Wartość sprzedaży brutto-->
            </xsl:if>
            <xsl:if test="$faWierszAfter/crd:P_6A">
                <fo:table-column column-width="9%"/> <!-- Data dostawy (P_6A) -->
            </xsl:if>

            <!-- Table header - identical to positionsTable -->
            <fo:table-header>
                <fo:table-row background-color="#f5f5f5" font-weight="bold">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.lp', $labels)"/></fo:block>
                    </fo:table-cell>
                    <xsl:if test="$faWierszAfter/crd:Indeks">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.indeks', $labels)"/></fo:block>
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
                    <xsl:if test="$faWierszAfter/crd:P_9A">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.unitPriceNet', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWierszAfter/crd:P_9B">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.unitPriceGross', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWierszAfter/crd:P_10">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.discount', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.taxRate', $labels)"/></fo:block>
                    </fo:table-cell>
                    <xsl:if test="$faWierszAfter/crd:P_11">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.netValue', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWierszAfter/crd:P_11Vat">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.vatAmount', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWierszAfter/crd:P_11A">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.grossValue', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWierszAfter/crd:P_6A">
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
                            <xsl:if test="$faWierszAfter/crd:Indeks">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                    <fo:block><xsl:value-of select="$after/crd:Indeks"/></fo:block>
                                </fo:table-cell>
                            </xsl:if>
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                <fo:block><xsl:value-of select="$after/crd:P_7"/></fo:block>
                            </fo:table-cell>
                            
                            <!-- Quantity - for new rows, show exact "after" value, otherwise show difference -->
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                <xsl:variable name="formattedQty">
                                    <xsl:choose>
                                        <xsl:when test="$isNewRow">
                                            <xsl:value-of select="translate(format-number(number($after/crd:P_8B), '#,##0.######'), ',', '&#160;')"/>
                                        </xsl:when>
                                        <xsl:when test="$before/crd:P_8B and $after/crd:P_8B">
                                            <xsl:value-of select="translate(format-number(number($after/crd:P_8B) - number($before/crd:P_8B), '#,##0.######'), ',', '&#160;')"/>
                                        </xsl:when>
                                        <xsl:when test="not($before/crd:P_8B) and $after/crd:P_8B">
                                            <xsl:value-of select="translate(format-number(number($after/crd:P_8B), '#,##0.######'), ',', '&#160;')"/>
                                        </xsl:when>
                                        <xsl:when test="$before/crd:P_8B and not($after/crd:P_8B)">
                                            <xsl:value-of select="translate(format-number(-number($before/crd:P_8B), '#,##0.######'), ',', '&#160;')"/>
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
                            <xsl:if test="$faWierszAfter/crd:P_9A">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="formattedNumber">
                                            <xsl:choose>
                                                <xsl:when test="$isNewRow and $after/crd:P_9A">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_9A), '#,##0.########'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_9A and $after/crd:P_9A and $before/crd:P_9A != $after/crd:P_9A">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_9A) - number($before/crd:P_9A), '#,##0.########'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_9A) and $after/crd:P_9A">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_9A), '#,##0.########'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_9A and not($after/crd:P_9A)">
                                                    <xsl:value-of select="translate(format-number(-number($before/crd:P_9A), '#,##0.########'), ',.', ' ,')"/>
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
                            <xsl:if test="$faWierszAfter/crd:P_9B">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="formattedNumber">
                                            <xsl:choose>
                                                <xsl:when test="$isNewRow and $after/crd:P_9B">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_9B), '#,##0.########'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_9B and $after/crd:P_9B and $before/crd:P_9B != $after/crd:P_9B">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_9B) - number($before/crd:P_9B), '#,##0.########'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_9B) and $after/crd:P_9B">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_9B), '#,##0.########'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_9B and not($after/crd:P_9B)">
                                                    <xsl:value-of select="translate(format-number(-number($before/crd:P_9B), '#,##0.########'), ',.', ' ,')"/>
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
                            <xsl:if test="$faWierszAfter/crd:P_10">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="formattedNumber">
                                            <xsl:choose>
                                                <xsl:when test="$isNewRow and $after/crd:P_10">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_10), '#,##0.########'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_10 and $after/crd:P_10 and $before/crd:P_10 != $after/crd:P_10">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_10) - number($before/crd:P_10), '#,##0.########'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_10) and $after/crd:P_10">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_10), '#,##0.########'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_10 and not($after/crd:P_10)">
                                                    <xsl:value-of select="translate(format-number(-number($before/crd:P_10), '#,##0.########'), ',.', ' ,')"/>
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
                            <xsl:if test="$faWierszAfter/crd:P_11">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="formattedNumber">
                                            <xsl:choose>
                                                <xsl:when test="$isNewRow and $after/crd:P_11">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_11), '#,##0.00'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_11 and $after/crd:P_11 and $before/crd:P_11 != $after/crd:P_11">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_11) - number($before/crd:P_11), '#,##0.00'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_11) and $after/crd:P_11">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_11), '#,##0.00'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_11 and not($after/crd:P_11)">
                                                    <xsl:value-of select="translate(format-number(-number($before/crd:P_11), '#,##0.00'), ',.', ' ,')"/>
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
                            <xsl:if test="$faWierszAfter/crd:P_11Vat">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="formattedNumber">
                                            <xsl:choose>
                                                <xsl:when test="$isNewRow and $after/crd:P_11Vat">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_11Vat), '#,##0.00'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_11Vat and $after/crd:P_11Vat and $before/crd:P_11Vat != $after/crd:P_11Vat">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_11Vat) - number($before/crd:P_11Vat), '#,##0.00'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_11Vat) and $after/crd:P_11Vat">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_11Vat), '#,##0.00'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_11Vat and not($after/crd:P_11Vat)">
                                                    <xsl:value-of select="translate(format-number(-number($before/crd:P_11Vat), '#,##0.00'), ',.', ' ,')"/>
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
                            <xsl:if test="$faWierszAfter/crd:P_11A">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="formattedNumber">
                                            <xsl:choose>
                                                <xsl:when test="$isNewRow and $after/crd:P_11A">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_11A), '#,##0.00'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_11A and $after/crd:P_11A and $before/crd:P_11A != $after/crd:P_11A">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_11A) - number($before/crd:P_11A), '#,##0.00'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_11A) and $after/crd:P_11A">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_11A), '#,##0.00'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_11A and not($after/crd:P_11A)">
                                                    <xsl:value-of select="translate(format-number(-number($before/crd:P_11A), '#,##0.00'), ',.', ' ,')"/>
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
                            <xsl:if test="$faWierszAfter/crd:P_6A">
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
