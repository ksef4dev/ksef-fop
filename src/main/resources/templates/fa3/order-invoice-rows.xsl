<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:crd="http://crd.gov.pl/wzor/2025/06/25/13775/">

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

    <!-- Template for rendering the order table -->
    <xsl:template name="zamowienieTable">
        <xsl:param name="zamowienieWiersz"/>
        <xsl:param name="faWierszBefore" select="()"/>
        <xsl:param name="faWierszAfter" select="()"/>

        <!-- Check if columns should be displayed -->
        <xsl:variable name="showP9AZ" select="boolean($zamowienieWiersz[crd:P_9AZ])"/>
        <xsl:variable name="showP11NettoZ" select="boolean($zamowienieWiersz[crd:P_11NettoZ])"/>
        <xsl:variable name="showP11VatZ" select="boolean($zamowienieWiersz[crd:P_11VatZ])"/>
        <xsl:variable name="showUU_IDZ" select="boolean($zamowienieWiersz[crd:UU_IDZ])"/>
        
        <!-- Check if any record has StanPrzedZ value -->
        <xsl:variable name="showStanPrzed" select="boolean($zamowienieWiersz[crd:StanPrzedZ])"/>

        <!-- Calculate column width for name based on presence of other columns -->
        <xsl:variable name="nameColumnWidth">
            <xsl:choose>
                <xsl:when test="$showP11VatZ and $showP9AZ and $showUU_IDZ and $showStanPrzed">16%</xsl:when>
                <xsl:when test="$showP11VatZ and $showP9AZ and $showUU_IDZ and not($showStanPrzed)">22%</xsl:when>
                <xsl:when test="$showP11VatZ and not($showP9AZ) and $showUU_IDZ and $showStanPrzed">24%</xsl:when>
                <xsl:when test="$showP11VatZ and not($showP9AZ) and $showUU_IDZ and not($showStanPrzed)">32%</xsl:when>
                <xsl:when test="not($showP11VatZ) and $showP9AZ and $showUU_IDZ and $showStanPrzed">22%</xsl:when>
                <xsl:when test="not($showP11VatZ) and $showP9AZ and $showUU_IDZ and not($showStanPrzed)">29%</xsl:when>
                <xsl:when test="not($showP11VatZ) and not($showP9AZ) and $showUU_IDZ and $showStanPrzed">30%</xsl:when>
                <xsl:when test="not($showP11VatZ) and not($showP9AZ) and $showUU_IDZ and not($showStanPrzed)">39%</xsl:when>
                <xsl:when test="$showP11VatZ and $showP9AZ and not($showUU_IDZ) and $showStanPrzed">28%</xsl:when>
                <xsl:when test="$showP11VatZ and $showP9AZ and not($showUU_IDZ) and not($showStanPrzed)">36%</xsl:when>
                <xsl:when test="$showP11VatZ and not($showP9AZ) and not($showUU_IDZ) and $showStanPrzed">36%</xsl:when>
                <xsl:when test="$showP11VatZ and not($showP9AZ) and not($showUU_IDZ) and not($showStanPrzed)">46%</xsl:when>
                <xsl:when test="not($showP11VatZ) and $showP9AZ and not($showUU_IDZ) and $showStanPrzed">34%</xsl:when>
                <xsl:when test="not($showP11VatZ) and $showP9AZ and not($showUU_IDZ) and not($showStanPrzed)">44%</xsl:when>
                <xsl:when test="not($showP11VatZ) and not($showP9AZ) and not($showUU_IDZ) and $showStanPrzed">44%</xsl:when>
                <xsl:otherwise>54%</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- Define the table structure -->
        <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-after="5mm">
            <!-- Define table columns -->
            <fo:table-column column-width="4%"/> <!-- Lp. -->
            <xsl:if test="$showUU_IDZ">
                <fo:table-column column-width="14%"/> <!-- Unikalny numer wiersza -->
            </xsl:if>
            <fo:table-column column-width="{$nameColumnWidth}"/> <!-- Nazwa -->
            <fo:table-column column-width="12%"/> <!-- Ilość -->
            <fo:table-column column-width="6%"/> <!-- Jednostka -->
            <xsl:if test="$showP9AZ">
                <fo:table-column column-width="12%"/> <!-- Cena jednostkowa netto -->
            </xsl:if>
            <fo:table-column column-width="8%"/> <!-- Stawka podatku -->
            <xsl:if test="$showP11NettoZ">
                <fo:table-column column-width="12%"/> <!-- Wartość netto -->
            </xsl:if>
            <xsl:if test="$showP11VatZ">
                <fo:table-column column-width="10%"/> <!-- Kwota VAT -->
            </xsl:if>
            <xsl:if test="$showStanPrzed">
                <fo:table-column column-width="8%"/> <!-- Stan przed -->
            </xsl:if>

            <!-- Table header -->
            <fo:table-header>
                <fo:table-row background-color="#f5f5f5" font-weight="bold">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.lp', $labels)"/></fo:block>
                    </fo:table-cell>
                    <xsl:if test="$showUU_IDZ">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.uniqueRowId', $labels)"/></fo:block>
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
                    <xsl:if test="$showP9AZ">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.unitPriceNet', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.taxRate', $labels)"/></fo:block>
                    </fo:table-cell>
                    <xsl:if test="$showP11NettoZ">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.netValueShort', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$showP11VatZ">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.vatAmount', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$showStanPrzed">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'row.stateBefore', $labels)"/></fo:block>
                        </fo:table-cell>
                    </xsl:if>
                </fo:table-row>
            </fo:table-header>

            <!-- Table body -->
            <fo:table-body>
                <xsl:choose>
                    <xsl:when test="$faWierszBefore and $faWierszAfter">
                        <!-- Differences mode -->
                        <xsl:call-template name="zaliczkoweDifferencesTable">
                            <xsl:with-param name="faWierszBefore" select="$faWierszBefore"/>
                            <xsl:with-param name="faWierszAfter" select="$faWierszAfter"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- Normal mode -->
                        <xsl:for-each select="$zamowienieWiersz">
                            <fo:table-row >
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                                    <fo:block>
                                        <xsl:value-of select="crd:NrWierszaZam"/>
                                    </fo:block>
                                </fo:table-cell>
                                <xsl:if test="$showUU_IDZ">
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                                        <fo:block wrap-option="wrap" white-space-collapse="false" hyphenation-character="-" hyphenation-push-character-count="2" hyphenation-remain-character-count="2">
                                            <xsl:value-of select="crd:UU_IDZ"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </xsl:if>
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                                    <fo:block>
                                        <xsl:value-of select="crd:P_7Z"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <xsl:variable name="formattedQty" select="translate(format-number(number(crd:P_8BZ), '#,##0.######'), ',', '&#160;')"/>
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
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:value-of select="crd:P_8AZ"/>
                                    </fo:block>
                                </fo:table-cell>
                                <xsl:if test="$showP9AZ">
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                        <fo:block>
                                            <xsl:if test="crd:P_9AZ">
                                                <xsl:value-of select="translate(format-number(number(crd:P_9AZ), '#,##0.########'), ',.', ' ,')"/>
                                            </xsl:if>
                                        </fo:block>
                                    </fo:table-cell>
                                </xsl:if>
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                    <fo:block>
                                        <xsl:variable name="taxRate" select="crd:P_12Z"/>
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
                                <xsl:if test="$showP11NettoZ">
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                        <fo:block>
                                            <xsl:if test="crd:P_11NettoZ">
                                                <xsl:value-of select="translate(format-number(number(crd:P_11NettoZ), '#,##0.00'), ',.', ' ,')"/>
                                            </xsl:if>
                                        </fo:block>
                                    </fo:table-cell>
                                </xsl:if>
                                <xsl:if test="$showP11VatZ">
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                        <fo:block>
                                            <xsl:if test="crd:P_11VatZ">
                                                <xsl:value-of select="translate(format-number(number(crd:P_11VatZ), '#,##0.00'), ',.', ' ,')"/>
                                            </xsl:if>
                                        </fo:block>
                                    </fo:table-cell>
                                </xsl:if>
                                <xsl:if test="$showStanPrzed">
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                                        <fo:block>
                                            <xsl:if test="crd:StanPrzedZ = '1'">
                                                <xsl:value-of select="key('kLabels', 'common.yes', $labels)"/>
                                            </xsl:if>
                                        </fo:block>
                                    </fo:table-cell>
                                </xsl:if>
                            </fo:table-row>
                        </xsl:for-each>
                    </xsl:otherwise>
                </xsl:choose>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <!-- Template for order positions -->
    <xsl:template match="crd:ZamowienieWiersz" mode="zamowienie">
        <fo:table-row>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:value-of select="crd:NrWierszaZam"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" padding-left="3pt">
                <fo:block>
                    <xsl:value-of select="crd:P_7Z"/>
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                <xsl:variable name="formattedQty" select="translate(format-number(number(crd:P_8BZ), '#,##0.######'), ',', '&#160;')"/>
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
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                <fo:block>
                    <xsl:value-of select="crd:P_8AZ"/>
                </fo:block>
            </fo:table-cell>
            <xsl:if test="//crd:ZamowienieWiersz/crd:P_9AZ">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_9AZ">
                                <xsl:variable name="formattedNumber">
                                    <xsl:value-of select="translate(format-number(number(crd:P_9AZ), '#,##0.########'), ',.', ' ,')"/>
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
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                <fo:block>
                    <xsl:variable name="taxRate" select="crd:P_12Z"/>
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
            <xsl:if test="//crd:ZamowienieWiersz/crd:P_11NettoZ">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_11NettoZ">
                                <xsl:variable name="formattedNumber" select="translate(format-number(number(crd:P_11NettoZ), '#,##0.00'), ',.', ' ,')"/>
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
            <xsl:if test="//crd:ZamowienieWiersz/crd:P_11VatZ">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_11VatZ">
                                <xsl:variable name="formattedNumber" select="translate(format-number(number(crd:P_11VatZ), '#,##0.00'), ',.', ' ,')"/>
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
            <xsl:if test="//crd:ZamowienieWiersz/crd:StanPrzedZ">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="center">
                    <fo:block>
                        <xsl:if test="crd:StanPrzedZ = '1'">
                            <xsl:value-of select="key('kLabels', 'common.yes', $labels)"/>
                        </xsl:if>
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="zaliczkoweDifferencesTable">
        <xsl:param name="faWierszBefore"/>
        <xsl:param name="faWierszAfter"/>

        <xsl:variable name="nameColumnWidth">
            <xsl:choose>
                <xsl:when test="$faWierszAfter/crd:P_11Vat and $faWierszAfter/crd:P_11A and $faWierszAfter/crd:P_9B and $faWierszAfter/crd:P_10">22%</xsl:when>
                <xsl:when test="$faWierszAfter/crd:P_11Vat and $faWierszAfter/crd:P_11A and $faWierszAfter/crd:P_9B and not($faWierszAfter/crd:P_10)">29%</xsl:when>
                
                <xsl:when test="$faWierszAfter/crd:P_11Vat and $faWierszAfter/crd:P_11A and not($faWierszAfter/crd:P_9B) and $faWierszAfter/crd:P_10">31%</xsl:when>
                <xsl:when test="$faWierszAfter/crd:P_11Vat and $faWierszAfter/crd:P_11A and not($faWierszAfter/crd:P_9B) and not($faWierszAfter/crd:P_10)">38%</xsl:when>
                
                <xsl:when test="$faWierszAfter/crd:P_11Vat and not($faWierszAfter/crd:P_11A) and $faWierszAfter/crd:P_9B and $faWierszAfter/crd:P_10">32%</xsl:when>
                <xsl:when test="$faWierszAfter/crd:P_11Vat and not($faWierszAfter/crd:P_11A) and $faWierszAfter/crd:P_9B and not($faWierszAfter/crd:P_10)">39%</xsl:when>
                
                <xsl:when test="$faWierszAfter/crd:P_11Vat and not($faWierszAfter/crd:P_11A) and not($faWierszAfter/crd:P_9B) and $faWierszAfter/crd:P_10">41%</xsl:when>
                <xsl:when test="$faWierszAfter/crd:P_11Vat and not($faWierszAfter/crd:P_11A) and not($faWierszAfter/crd:P_9B) and not($faWierszAfter/crd:P_10)">48%</xsl:when>
                
                <xsl:when test="not($faWierszAfter/crd:P_11Vat) and $faWierszAfter/crd:P_11A and $faWierszAfter/crd:P_9B and $faWierszAfter/crd:P_10">29%</xsl:when>
                <xsl:when test="not($faWierszAfter/crd:P_11Vat) and $faWierszAfter/crd:P_11A and $faWierszAfter/crd:P_9B and not($faWierszAfter/crd:P_10)">36%</xsl:when>
                
                <xsl:when test="not($faWierszAfter/crd:P_11Vat) and $faWierszAfter/crd:P_11A and not($faWierszAfter/crd:P_9B) and $faWierszAfter/crd:P_10">38%</xsl:when>
                <xsl:when test="not($faWierszAfter/crd:P_11Vat) and $faWierszAfter/crd:P_11A and not($faWierszAfter/crd:P_9B) and not($faWierszAfter/crd:P_10)">45%</xsl:when>
                
                <xsl:when test="not($faWierszAfter/crd:P_11Vat) and not($faWierszAfter/crd:P_11A) and $faWierszAfter/crd:P_9B and $faWierszAfter/crd:P_10">39%</xsl:when>
                <xsl:when test="not($faWierszAfter/crd:P_11Vat) and not($faWierszAfter/crd:P_11A) and $faWierszAfter/crd:P_9B and not($faWierszAfter/crd:P_10)">46%</xsl:when>
                
                <xsl:when test="not($faWierszAfter/crd:P_11Vat) and not($faWierszAfter/crd:P_11A) and not($faWierszAfter/crd:P_9B) and $faWierszAfter/crd:P_10">48%</xsl:when>
                <xsl:when test="not($faWierszAfter/crd:P_11Vat) and not($faWierszAfter/crd:P_11A) and not($faWierszAfter/crd:P_9B) and not($faWierszAfter/crd:P_10)">55%</xsl:when>
            </xsl:choose>
        </xsl:variable>

        <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-after="5mm">
            <fo:table-column column-width="4%"/> <!-- Lp. -->
            <fo:table-column column-width="{$nameColumnWidth}"/> <!-- Nazwa - dynamiczna szerokość -->
            <fo:table-column column-width="10%"/> <!-- Ilość -->
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

            <!-- Table header - identical to positionsTable -->
            <fo:table-header>
                <fo:table-row background-color="#f5f5f5" font-weight="bold">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block><xsl:value-of select="key('kLabels', 'row.lp', $labels)"/></fo:block>
                    </fo:table-cell>
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
                        </fo:table-row>
                </xsl:for-each>
            </fo:table-body>
        </fo:table>
    </xsl:template>

</xsl:stylesheet>
