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

    <xsl:function name="local:softWrap" as="xs:string">
        <xsl:param name="value" as="xs:string?"/>
        <xsl:param name="chunkSize" as="xs:integer"/>
        <xsl:variable name="v" select="string($value)"/>
        <xsl:sequence select="replace($v, concat('(.{', $chunkSize, '})'), concat('$1', '&#x200B;'))"/>
    </xsl:function>

    <!-- Attribute sets required for table styling -->
    <xsl:attribute-set name="tableBorder">
        <xsl:attribute name="border">solid 0.2mm black</xsl:attribute>
    </xsl:attribute-set>
    <xsl:variable name="tableHeaderFontSize" select="7"/>
    <xsl:variable name="tableRowFontSize" select="7"/>
    <xsl:variable name="tableFontSize" select="max(($tableHeaderFontSize, $tableRowFontSize))"/>
    <xsl:attribute-set name="tableHeaderFont">
        <xsl:attribute name="font-size" select="concat($tableHeaderFontSize, 'pt')"/>
    </xsl:attribute-set>
    <xsl:attribute-set name="tableFont">
        <xsl:attribute name="font-size" select="concat($tableRowFontSize, 'pt')"/>
    </xsl:attribute-set>
    <xsl:attribute-set name="table.cell.padding">
        <xsl:attribute name="padding-left">4pt</xsl:attribute>
        <xsl:attribute name="padding-right">4pt</xsl:attribute>
        <xsl:attribute name="padding-top">4pt</xsl:attribute>
        <xsl:attribute name="padding-bottom">4pt</xsl:attribute>
    </xsl:attribute-set>

    <!-- Template to create the columns and header row of the table -->
    <xsl:template name="tableHeader">
        <xsl:param name="hasIndeks"      as="xs:boolean"/>
        <xsl:param name="hasGTIN"        as="xs:boolean"/>
        <xsl:param name="hasPKWiU"       as="xs:boolean"/>
        <xsl:param name="hasCN"          as="xs:boolean"/>
        <xsl:param name="hasPKOB"        as="xs:boolean"/>
        <xsl:param name="hasKwotaAkcyzy" as="xs:boolean"/>
        <xsl:param name="hasP9A"         as="xs:boolean"/>
        <xsl:param name="hasP9B"         as="xs:boolean"/>
        <xsl:param name="hasP10"         as="xs:boolean"/>
        <xsl:param name="hasP12"         as="xs:boolean"/>
        <xsl:param name="hasP11"         as="xs:boolean"/>
        <xsl:param name="hasP11Vat"      as="xs:boolean"/>
        <xsl:param name="hasP11A"        as="xs:boolean"/>
        <xsl:param name="hasP6A"         as="xs:boolean"/>

        <!--
          In the table below, we approximate the size of some columns width fixed or almost fixed width.
          Other columns are expressed as percentage of table.
          Finally, `Nazwa` takes up the remaining space.

          For evaluation purposes:
          - 1 digit = 0.6em (0.5718 in OpenSans)
          - 1 dot or space = 0.3em (0.2661 in OpenSans)
          - 1.2em padding added

          If label is larger, we expand to fit the label.

          Field         Width    Max chars
          ───────────────────────────────────────────────────────
          CN            6.3em      CN code: 8 digits with optional space
          GTIN          9.0em      GTIN: 13 digits for EAN
          PKOB          4.0em      PKOB code: up to 4 digits (e.g. "0000"), label is larger
          PKWiU         6.3em      PKWiU code: up to 7 digits (e.g. "00.00.00.0")
          ───────────────────────────────────────────────────────
        -->
        <fo:table-column column-width="4%"/> <!-- Lp. -->
        <xsl:if test="$hasIndeks">
            <fo:table-column column-width="8%"/> <!-- Indeks -->
        </xsl:if>
        <xsl:if test="$hasGTIN">
            <fo:table-column column-width="9.0em"/> <!-- GTIN -->
        </xsl:if>
        <xsl:if test="$hasPKWiU">
            <fo:table-column column-width="6.3em"/> <!-- PKWiU -->
        </xsl:if>
        <xsl:if test="$hasCN">
            <fo:table-column column-width="6.3em"/> <!-- CN -->
        </xsl:if>
        <xsl:if test="$hasPKOB">
            <fo:table-column column-width="4.0em"/> <!-- PKOB -->
        </xsl:if>
        <xsl:if test="$hasKwotaAkcyzy">
            <fo:table-column column-width="8%"/> <!-- Kwota akcyzy -->
        </xsl:if>
        <fo:table-column column-width="proportional-column-width(1)"/> <!-- Nazwa -->
        <fo:table-column column-width="8%"/> <!-- Ilość -->
        <fo:table-column column-width="5%"/> <!-- Jednostka -->
        <xsl:if test="$hasP9A">
            <fo:table-column column-width="10%"/> <!-- Cena jednostkowa netto -->
        </xsl:if>
        <xsl:if test="$hasP9B">
            <fo:table-column column-width="10%"/> <!-- Cena jednostkowa brutto -->
        </xsl:if>
        <xsl:if test="$hasP10">
            <fo:table-column column-width="7%"/> <!-- Rabat -->
        </xsl:if>
        <xsl:if test="$hasP12">
            <fo:table-column column-width="8%"/> <!-- Stawka podatku -->
        </xsl:if>
        <xsl:if test="$hasP11">
            <fo:table-column column-width="10%"/> <!-- Wartość sprzedaży netto -->
        </xsl:if>
        <xsl:if test="$hasP11Vat">
            <fo:table-column column-width="7%"/> <!-- Kwota VAT -->
        </xsl:if>
        <xsl:if test="$hasP11A">
            <fo:table-column column-width="10%"/> <!-- Wartość sprzedaży brutto -->
        </xsl:if>
        <xsl:if test="$hasP6A">
            <fo:table-column column-width="9%"/> <!-- Data dostawy (P_6A) -->
        </xsl:if>

        <!-- Table header -->
        <fo:table-header>
            <fo:table-row background-color="#f5f5f5" font-weight="bold">
                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                    <fo:block><xsl:value-of select="key('kLabels', 'row.lp', $labels)"/></fo:block>
                </fo:table-cell>
                <xsl:if test="$hasIndeks">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.indeks', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
                <xsl:if test="$hasGTIN">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.gtin', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
                <xsl:if test="$hasPKWiU">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.pkwiu', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
                <xsl:if test="$hasCN">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.cn', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
                <xsl:if test="$hasPKOB">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.pkob', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
                <xsl:if test="$hasKwotaAkcyzy">
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
                <xsl:if test="$hasP9A">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.unitPriceNet', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
                <xsl:if test="$hasP9B">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.unitPriceGross', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
                <xsl:if test="$hasP10">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.discount', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
                <xsl:if test="$hasP12">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.taxRate', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
                <xsl:if test="$hasP11">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.netValue', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
                <xsl:if test="$hasP11Vat">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.vatAmount', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
                <xsl:if test="$hasP11A">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.grossValue', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
                <xsl:if test="$hasP6A">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.deliveryDate', $labels)"/></fo:block>
                    </fo:table-cell>
                </xsl:if>
            </fo:table-row>
        </fo:table-header>
    </xsl:template>

    <!-- Template for rendering the positions table -->
    <xsl:template name="positionsTable">
        <xsl:param name="faWiersz"/>

        <!-- Table header -->
        <fo:table table-layout="fixed"
                  font-size="{$tableFontSize}pt"
                  width="100%"
                  border-collapse="separate"
                  space-after="5mm">
            <xsl:call-template name="tableHeader">
                <xsl:with-param name="hasIndeks"      select="boolean($faWiersz/crd:Indeks)"/>
                <xsl:with-param name="hasGTIN"        select="boolean($faWiersz/crd:GTIN)"/>
                <xsl:with-param name="hasPKWiU"       select="boolean($faWiersz/crd:PKWiU)"/>
                <xsl:with-param name="hasCN"          select="boolean($faWiersz/crd:CN)"/>
                <xsl:with-param name="hasPKOB"        select="boolean($faWiersz/crd:PKOB)"/>
                <xsl:with-param name="hasKwotaAkcyzy" select="boolean($faWiersz/crd:KwotaAkcyzy)"/>
                <xsl:with-param name="hasP9A"         select="boolean($faWiersz/crd:P_9A)"/>
                <xsl:with-param name="hasP9B"         select="boolean($faWiersz/crd:P_9B)"/>
                <xsl:with-param name="hasP10"         select="boolean($faWiersz/crd:P_10)"/>
                <xsl:with-param name="hasP12"         select="boolean($faWiersz/crd:P_12)"/>
                <xsl:with-param name="hasP11"         select="boolean($faWiersz/crd:P_11)"/>
                <xsl:with-param name="hasP11Vat"      select="boolean($faWiersz/crd:P_11Vat)"/>
                <xsl:with-param name="hasP11A"        select="boolean($faWiersz/crd:P_11A)"/>
                <xsl:with-param name="hasP6A"         select="boolean($faWiersz/crd:P_6A)"/>
            </xsl:call-template>

        <!-- Table body -->
        <fo:table-body>
            <!-- Apply templates to each position; tunnel column visibility so row output matches this table's columns (correction before/after can differ) -->
            <xsl:apply-templates select="$faWiersz">
                <xsl:with-param name="showIndeks" select="boolean($faWiersz/crd:Indeks)" tunnel="yes"/>
                <xsl:with-param name="showGTIN" select="boolean($faWiersz/crd:GTIN)" tunnel="yes"/>
                <xsl:with-param name="showPKWiU" select="boolean($faWiersz/crd:PKWiU)" tunnel="yes"/>
                <xsl:with-param name="showCN" select="boolean($faWiersz/crd:CN)" tunnel="yes"/>
                <xsl:with-param name="showPKOB" select="boolean($faWiersz/crd:PKOB)" tunnel="yes"/>
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
        <xsl:param name="showIndeks" select="boolean(//crd:FaWiersz/crd:Indeks)" tunnel="yes"/>
        <xsl:param name="showGTIN" select="boolean(//crd:FaWiersz/crd:GTIN)" tunnel="yes"/>
        <xsl:param name="showPKWiU" select="boolean(//crd:FaWiersz/crd:PKWiU)" tunnel="yes"/>
        <xsl:param name="showCN" select="boolean(//crd:FaWiersz/crd:CN)" tunnel="yes"/>
        <xsl:param name="showPKOB" select="boolean(//crd:FaWiersz/crd:PKOB)" tunnel="yes"/>
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
            <xsl:if test="$showIndeks">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                    <xsl:variable name="indeksValue" select="string(crd:Indeks)"/>
                    <xsl:choose>
                        <xsl:when test="string-length($indeksValue) &gt; 12">
                            <fo:block font-size="6pt" wrap-option="wrap" white-space-collapse="false" linefeed-treatment="preserve">
                                <xsl:value-of select="local:softWrap($indeksValue, 8)"/> <!-- Indeks -->
                            </fo:block>
                        </xsl:when>
                        <xsl:otherwise>
                            <fo:block wrap-option="wrap" white-space-collapse="false" linefeed-treatment="preserve">
                                <xsl:value-of select="local:softWrap($indeksValue, 8)"/> <!-- Indeks -->
                            </fo:block>
                        </xsl:otherwise>
                    </xsl:choose>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="$showGTIN">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                    <fo:block>
                        <xsl:value-of select="crd:GTIN"/>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="$showPKWiU">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                    <fo:block>
                        <xsl:value-of select="crd:PKWiU"/>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="$showCN">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                    <fo:block>
                        <xsl:value-of select="crd:CN"/>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="$showPKOB">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                    <fo:block>
                        <xsl:value-of select="crd:PKOB"/>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <xsl:if test="$showKwotaAkcyzy">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:KwotaAkcyzy">
                                <xsl:variable name="formattedNumber" select="translate(format-number(number(crd:KwotaAkcyzy), '#,##0.00'), ',.', ' ,')"/>
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
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                <xsl:variable name="formattedQty" select="translate(format-number(number(crd:P_8B), '#,##0.######'), '.,', ',&#160;')"/>
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
            <xsl:if test="$showP9B">
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
            <xsl:if test="$showP10">
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
            <xsl:if test="$showP11Vat">
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
            <xsl:if test="$showP11A">
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
        <xsl:variable name="diffShowIndeks" select="$faWierszBefore/crd:Indeks or $faWierszAfter/crd:Indeks"/>
        <xsl:variable name="diffShowGTIN" select="$faWierszBefore/crd:GTIN or $faWierszAfter/crd:GTIN"/>
        <xsl:variable name="diffShowPKWiU" select="$faWierszBefore/crd:PKWiU or $faWierszAfter/crd:PKWiU"/>
        <xsl:variable name="diffShowCN" select="$faWierszBefore/crd:CN or $faWierszAfter/crd:CN"/>
        <xsl:variable name="diffShowPKOB" select="$faWierszBefore/crd:PKOB or $faWierszAfter/crd:PKOB"/>
        <xsl:variable name="diffShowKwotaAkcyzy" select="$faWierszBefore/crd:KwotaAkcyzy or $faWierszAfter/crd:KwotaAkcyzy"/>
        <xsl:variable name="diffShowP6A" select="$faWierszBefore/crd:P_6A or $faWierszAfter/crd:P_6A"/>
        <xsl:variable name="diffShowP9A" select="$faWierszBefore/crd:P_9A or $faWierszAfter/crd:P_9A"/>
        <xsl:variable name="diffShowP9B" select="$faWierszBefore/crd:P_9B or $faWierszAfter/crd:P_9B"/>
        <xsl:variable name="diffShowP10" select="$faWierszBefore/crd:P_10 or $faWierszAfter/crd:P_10"/>
        <xsl:variable name="diffShowP11" select="$faWierszBefore/crd:P_11 or $faWierszAfter/crd:P_11"/>
        <xsl:variable name="diffShowP11Vat" select="$faWierszBefore/crd:P_11Vat or $faWierszAfter/crd:P_11Vat"/>
        <xsl:variable name="diffShowP11A" select="$faWierszBefore/crd:P_11A or $faWierszAfter/crd:P_11A"/>

        <fo:table table-layout="fixed"
                  font-size="{$tableFontSize}pt"
                  width="100%"
                  border-collapse="separate"
                  space-after="5mm">
            <xsl:call-template name="tableHeader">
                <xsl:with-param name="hasIndeks" select="$diffShowIndeks"/>
                <xsl:with-param name="hasGTIN" select="$diffShowGTIN"/>
                <xsl:with-param name="hasPKWiU" select="$diffShowPKWiU"/>
                <xsl:with-param name="hasCN" select="$diffShowCN"/>
                <xsl:with-param name="hasPKOB" select="$diffShowPKOB"/>
                <xsl:with-param name="hasKwotaAkcyzy" select="$diffShowKwotaAkcyzy"/>
                <xsl:with-param name="hasP9A" select="$diffShowP9A"/>
                <xsl:with-param name="hasP9B" select="$diffShowP9B"/>
                <xsl:with-param name="hasP10" select="$diffShowP10"/>
                <xsl:with-param name="hasP12" select="true()"/>
                <xsl:with-param name="hasP11" select="$diffShowP11"/>
                <xsl:with-param name="hasP11Vat" select="$diffShowP11Vat"/>
                <xsl:with-param name="hasP11A" select="$diffShowP11A"/>
                <xsl:with-param name="hasP6A" select="$diffShowP6A"/>
            </xsl:call-template>

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
                            <xsl:if test="$diffShowIndeks">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                    <xsl:variable name="indeksValue" select="string($after/crd:Indeks)"/>
                                    <xsl:choose>
                                        <xsl:when test="string-length($indeksValue) &gt; 12">
                                            <fo:block font-size="6pt" wrap-option="wrap" white-space-collapse="false" linefeed-treatment="preserve">
                                                <xsl:value-of select="local:softWrap($indeksValue, 8)"/>
                                            </fo:block>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <fo:block wrap-option="wrap" white-space-collapse="false" linefeed-treatment="preserve">
                                                <xsl:value-of select="local:softWrap($indeksValue, 8)"/>
                                            </fo:block>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:table-cell>
                            </xsl:if>
                            <xsl:if test="$diffShowGTIN">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                    <fo:block>
                                        <xsl:value-of select="$after/crd:GTIN"/>
                                    </fo:block>
                                </fo:table-cell>
                            </xsl:if>
                            <xsl:if test="$diffShowPKWiU">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                    <fo:block>
                                        <xsl:value-of select="$after/crd:PKWiU"/>
                                    </fo:block>
                                </fo:table-cell>
                            </xsl:if>
                            <xsl:if test="$diffShowCN">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                    <fo:block>
                                        <xsl:value-of select="$after/crd:CN"/>
                                    </fo:block>
                                </fo:table-cell>
                            </xsl:if>
                            <xsl:if test="$diffShowPKOB">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                    <fo:block>
                                        <xsl:value-of select="$after/crd:PKOB"/>
                                    </fo:block>
                                </fo:table-cell>
                            </xsl:if>
                            <xsl:if test="$diffShowKwotaAkcyzy">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:choose>
                                            <xsl:when test="$after/crd:KwotaAkcyzy">
                                                <xsl:variable name="formattedNumber" select="translate(format-number(number($after/crd:KwotaAkcyzy), '#,##0.00'), ',.', ' ,')"/>
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
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                <fo:block><xsl:value-of select="$after/crd:P_7"/></fo:block>
                            </fo:table-cell>
                            
                            <!-- Quantity - for new rows, show exact "after" value, otherwise show difference -->
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                <xsl:variable name="formattedQty">
                                    <xsl:choose>
                                        <xsl:when test="$isNewRow">
                                            <xsl:value-of select="translate(format-number(number($after/crd:P_8B), '#,##0.######'), '.,', ',&#160;')"/>
                                        </xsl:when>
                                        <xsl:when test="$before/crd:P_8B and $after/crd:P_8B">
                                            <xsl:value-of select="translate(format-number(number($after/crd:P_8B) - number($before/crd:P_8B), '#,##0.######'), '.,', ',&#160;')"/>
                                        </xsl:when>
                                        <xsl:when test="not($before/crd:P_8B) and $after/crd:P_8B">
                                            <xsl:value-of select="translate(format-number(number($after/crd:P_8B), '#,##0.######'), '.,', ',&#160;')"/>
                                        </xsl:when>
                                        <xsl:when test="$before/crd:P_8B and not($after/crd:P_8B)">
                                            <xsl:value-of select="translate(format-number(-number($before/crd:P_8B), '#,##0.######'), '.,', ',&#160;')"/>
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
                            <xsl:if test="$diffShowP9B">
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
                            <xsl:if test="$diffShowP10">
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
                            <xsl:if test="$diffShowP11">
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
                            <xsl:if test="$diffShowP11Vat">
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
                            <xsl:if test="$diffShowP11A">
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
