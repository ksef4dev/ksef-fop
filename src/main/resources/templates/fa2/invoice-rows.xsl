<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:crd="http://crd.gov.pl/wzor/2023/06/29/12648/">

    <!-- Parameter for controlling decimal places in unit prices -->
    <xsl:param name="useExtendedDecimalPlaces" select="false()"/>

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
        <xsl:variable name="nameColumnWidth">
            <xsl:choose>
                <!-- Base width is 31% -->
                <!-- Check all combinations of P_11Vat, P_11A, P_9B and P_10 -->
                <xsl:when test="$faWiersz/crd:P_11Vat and $faWiersz/crd:P_11A and $faWiersz/crd:P_9B and $faWiersz/crd:P_10">22%</xsl:when>
                <xsl:when test="$faWiersz/crd:P_11Vat and $faWiersz/crd:P_11A and $faWiersz/crd:P_9B and not($faWiersz/crd:P_10)">29%</xsl:when>
                
                <xsl:when test="$faWiersz/crd:P_11Vat and $faWiersz/crd:P_11A and not($faWiersz/crd:P_9B) and $faWiersz/crd:P_10">31%</xsl:when>
                <xsl:when test="$faWiersz/crd:P_11Vat and $faWiersz/crd:P_11A and not($faWiersz/crd:P_9B) and not($faWiersz/crd:P_10)">38%</xsl:when>
                
                <xsl:when test="$faWiersz/crd:P_11Vat and not($faWiersz/crd:P_11A) and $faWiersz/crd:P_9B and $faWiersz/crd:P_10">32%</xsl:when>
                <xsl:when test="$faWiersz/crd:P_11Vat and not($faWiersz/crd:P_11A) and $faWiersz/crd:P_9B and not($faWiersz/crd:P_10)">39%</xsl:when>
                
                <xsl:when test="$faWiersz/crd:P_11Vat and not($faWiersz/crd:P_11A) and not($faWiersz/crd:P_9B) and $faWiersz/crd:P_10">41%</xsl:when>
                <xsl:when test="$faWiersz/crd:P_11Vat and not($faWiersz/crd:P_11A) and not($faWiersz/crd:P_9B) and not($faWiersz/crd:P_10)">48%</xsl:when>
                
                <xsl:when test="not($faWiersz/crd:P_11Vat) and $faWiersz/crd:P_11A and $faWiersz/crd:P_9B and $faWiersz/crd:P_10">29%</xsl:when>
                <xsl:when test="not($faWiersz/crd:P_11Vat) and $faWiersz/crd:P_11A and $faWiersz/crd:P_9B and not($faWiersz/crd:P_10)">36%</xsl:when>
                
                <xsl:when test="not($faWiersz/crd:P_11Vat) and $faWiersz/crd:P_11A and not($faWiersz/crd:P_9B) and $faWiersz/crd:P_10">38%</xsl:when>
                <xsl:when test="not($faWiersz/crd:P_11Vat) and $faWiersz/crd:P_11A and not($faWiersz/crd:P_9B) and not($faWiersz/crd:P_10)">45%</xsl:when>
                
                <xsl:when test="not($faWiersz/crd:P_11Vat) and not($faWiersz/crd:P_11A) and $faWiersz/crd:P_9B and $faWiersz/crd:P_10">39%</xsl:when>
                <xsl:when test="not($faWiersz/crd:P_11Vat) and not($faWiersz/crd:P_11A) and $faWiersz/crd:P_9B and not($faWiersz/crd:P_10)">46%</xsl:when>
                
                <xsl:when test="not($faWiersz/crd:P_11Vat) and not($faWiersz/crd:P_11A) and not($faWiersz/crd:P_9B) and $faWiersz/crd:P_10">48%</xsl:when>
                <xsl:when test="not($faWiersz/crd:P_11Vat) and not($faWiersz/crd:P_11A) and not($faWiersz/crd:P_9B) and not($faWiersz/crd:P_10)">55%</xsl:when>
            </xsl:choose>
        </xsl:variable>

        <!-- Define the table structure -->
        <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-after="5mm">
            <!-- Define table columns -->
            <fo:table-column column-width="4%"/> <!-- Lp. -->
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

            <!-- Table header -->
            <fo:table-header>
                <fo:table-row background-color="#f5f5f5" font-weight="bold">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block>Lp.</fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block>Nazwa towaru lub usługi</fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block>Ilość</fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block>Jedn.</fo:block>
                    </fo:table-cell>
                    <xsl:if test="$faWiersz/crd:P_9A">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Cena jedn. netto</fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_9B">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Cena jedn. brutto</fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_10">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Rabat</fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_12">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Stawka podatku</fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_11">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Wartość sprzedaży netto</fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_11Vat">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Kwota VAT</fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$faWiersz/crd:P_11A">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Wartość sprzedaży brutto</fo:block>
                        </fo:table-cell>
                    </xsl:if>
                </fo:table-row>
            </fo:table-header>

                    <!-- Table body -->
        <fo:table-body>
            <!-- Apply templates to each position; tunnel column visibility so row output matches this table's columns -->
            <xsl:apply-templates select="$faWiersz">
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

    <!-- Template for each position. Column visibility comes from tunnel params (from positionsTable) so correction "before" vs "after" tables can have different columns. -->
    <xsl:template match="crd:FaWiersz">
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
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" padding-left="3pt">
                <fo:block>
                    <xsl:value-of select="crd:P_7"/> <!-- Nazwa -->
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                <fo:block>
                    <xsl:value-of select="crd:P_8B"/> <!-- Ilość -->
                </fo:block>
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
                                    <xsl:choose>
                                        <xsl:when test="$useExtendedDecimalPlaces">
                                            <xsl:value-of select="translate(format-number(number(crd:P_9A), '#,##0.0000'), ',.', ' ,')"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="translate(format-number(number(crd:P_9A), '#,##0.00'), ',.', ' ,')"/>
                                        </xsl:otherwise>
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
                                    <xsl:choose>
                                        <xsl:when test="$useExtendedDecimalPlaces">
                                            <xsl:value-of select="translate(format-number(number(crd:P_9B), '#,##0.0000'), ',.', ' ,')"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="translate(format-number(number(crd:P_9B), '#,##0.00'), ',.', ' ,')"/>
                                        </xsl:otherwise>
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
                                <xsl:variable name="formattedNumber" select="translate(format-number(number(crd:P_10), '#,##0.00'), ',.', ' ,')"/>
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
        </fo:table-row>
    </xsl:template>

    <xsl:template name="differencesTable">
        <xsl:param name="faWierszBefore"/>
        <xsl:param name="faWierszAfter"/>

        <!-- Column visibility: show column if it appears in EITHER before OR after (union), so differing structures don't break the table -->
        <xsl:variable name="diffShowP9A" select="boolean($faWierszBefore/crd:P_9A or $faWierszAfter/crd:P_9A)"/>
        <xsl:variable name="diffShowP9B" select="boolean($faWierszBefore/crd:P_9B or $faWierszAfter/crd:P_9B)"/>
        <xsl:variable name="diffShowP10" select="boolean($faWierszBefore/crd:P_10 or $faWierszAfter/crd:P_10)"/>
        <xsl:variable name="diffShowP11" select="boolean($faWierszBefore/crd:P_11 or $faWierszAfter/crd:P_11)"/>
        <xsl:variable name="diffShowP11Vat" select="boolean($faWierszBefore/crd:P_11Vat or $faWierszAfter/crd:P_11Vat)"/>
        <xsl:variable name="diffShowP11A" select="boolean($faWierszBefore/crd:P_11A or $faWierszAfter/crd:P_11A)"/>

        <xsl:variable name="nameColumnWidth">
            <xsl:choose>
                <xsl:when test="$diffShowP11Vat and $diffShowP11A and $diffShowP9B and $diffShowP10">22%</xsl:when>
                <xsl:when test="$diffShowP11Vat and $diffShowP11A and $diffShowP9B and not($diffShowP10)">29%</xsl:when>
                <xsl:when test="$diffShowP11Vat and $diffShowP11A and not($diffShowP9B) and $diffShowP10">31%</xsl:when>
                <xsl:when test="$diffShowP11Vat and $diffShowP11A and not($diffShowP9B) and not($diffShowP10)">38%</xsl:when>
                <xsl:when test="$diffShowP11Vat and not($diffShowP11A) and $diffShowP9B and $diffShowP10">32%</xsl:when>
                <xsl:when test="$diffShowP11Vat and not($diffShowP11A) and $diffShowP9B and not($diffShowP10)">39%</xsl:when>
                <xsl:when test="$diffShowP11Vat and not($diffShowP11A) and not($diffShowP9B) and $diffShowP10">41%</xsl:when>
                <xsl:when test="$diffShowP11Vat and not($diffShowP11A) and not($diffShowP9B) and not($diffShowP10)">48%</xsl:when>
                <xsl:when test="not($diffShowP11Vat) and $diffShowP11A and $diffShowP9B and $diffShowP10">29%</xsl:when>
                <xsl:when test="not($diffShowP11Vat) and $diffShowP11A and $diffShowP9B and not($diffShowP10)">36%</xsl:when>
                <xsl:when test="not($diffShowP11Vat) and $diffShowP11A and not($diffShowP9B) and $diffShowP10">38%</xsl:when>
                <xsl:when test="not($diffShowP11Vat) and $diffShowP11A and not($diffShowP9B) and not($diffShowP10)">45%</xsl:when>
                <xsl:when test="not($diffShowP11Vat) and not($diffShowP11A) and $diffShowP9B and $diffShowP10">39%</xsl:when>
                <xsl:when test="not($diffShowP11Vat) and not($diffShowP11A) and $diffShowP9B and not($diffShowP10)">46%</xsl:when>
                <xsl:when test="not($diffShowP11Vat) and not($diffShowP11A) and not($diffShowP9B) and $diffShowP10">48%</xsl:when>
                <xsl:when test="not($diffShowP11Vat) and not($diffShowP11A) and not($diffShowP9B) and not($diffShowP10)">55%</xsl:when>
            </xsl:choose>
        </xsl:variable>

        <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-after="5mm">
            <fo:table-column column-width="4%"/> <!-- Lp. -->
            <fo:table-column column-width="{$nameColumnWidth}"/> <!-- Nazwa - dynamiczna szerokość -->
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

            <!-- Table header - identical to positionsTable -->
            <fo:table-header>
                <fo:table-row background-color="#f5f5f5" font-weight="bold">
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block>Lp.</fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block>Nazwa towaru lub usługi</fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block>Ilość</fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block>Jedn.</fo:block>
                    </fo:table-cell>
                    <xsl:if test="$diffShowP9A">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Cena jedn. netto</fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$diffShowP9B">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Cena jedn. brutto</fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$diffShowP10">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Rabat</fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block>Stawka podatku</fo:block>
                    </fo:table-cell>
                    <xsl:if test="$diffShowP11">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Wartość sprzedaży netto</fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$diffShowP11Vat">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Kwota VAT</fo:block>
                        </fo:table-cell>
                    </xsl:if>
                    <xsl:if test="$diffShowP11A">
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>Wartość sprzedaży brutto</fo:block>
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
                                <fo:block>
                                    <xsl:variable name="formattedNumber">
                                        <xsl:choose>
                                            <xsl:when test="$isNewRow">
                                                <xsl:value-of select="translate(format-number(number($after/crd:P_8B), '#,##0.00'), ',.', ' ,')"/>
                                            </xsl:when>
                                            <xsl:when test="$before/crd:P_8B and $after/crd:P_8B">
                                                <xsl:value-of select="translate(format-number(number($after/crd:P_8B) - number($before/crd:P_8B), '#,##0.00'), ',.', ' ,')"/>
                                            </xsl:when>
                                            <xsl:when test="not($before/crd:P_8B) and $after/crd:P_8B">
                                                <xsl:value-of select="translate(format-number(number($after/crd:P_8B), '#,##0.00'), ',.', ' ,')"/>
                                            </xsl:when>
                                            <xsl:when test="$before/crd:P_8B and not($after/crd:P_8B)">
                                                <xsl:value-of select="translate(format-number(-number($before/crd:P_8B), '#,##0.00'), ',.', ' ,')"/>
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
                                                    <xsl:choose>
                                                        <xsl:when test="$useExtendedDecimalPlaces">
                                                            <xsl:value-of select="translate(format-number(number($after/crd:P_9A), '#,##0.0000'), ',.', ' ,')"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="translate(format-number(number($after/crd:P_9A), '#,##0.00'), ',.', ' ,')"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_9A and $after/crd:P_9A and $before/crd:P_9A != $after/crd:P_9A">
                                                    <xsl:choose>
                                                        <xsl:when test="$useExtendedDecimalPlaces">
                                                            <xsl:value-of select="translate(format-number(number($after/crd:P_9A) - number($before/crd:P_9A), '#,##0.0000'), ',.', ' ,')"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="translate(format-number(number($after/crd:P_9A) - number($before/crd:P_9A), '#,##0.00'), ',.', ' ,')"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_9A) and $after/crd:P_9A">
                                                    <xsl:choose>
                                                        <xsl:when test="$useExtendedDecimalPlaces">
                                                            <xsl:value-of select="translate(format-number(number($after/crd:P_9A), '#,##0.0000'), ',.', ' ,')"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="translate(format-number(number($after/crd:P_9A), '#,##0.00'), ',.', ' ,')"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_9A and not($after/crd:P_9A)">
                                                    <xsl:choose>
                                                        <xsl:when test="$useExtendedDecimalPlaces">
                                                            <xsl:value-of select="translate(format-number(-number($before/crd:P_9A), '#,##0.0000'), ',.', ' ,')"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="translate(format-number(-number($before/crd:P_9A), '#,##0.00'), ',.', ' ,')"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
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
                                                    <xsl:choose>
                                                        <xsl:when test="$useExtendedDecimalPlaces">
                                                            <xsl:value-of select="translate(format-number(number($after/crd:P_9B), '#,##0.0000'), ',.', ' ,')"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="translate(format-number(number($after/crd:P_9B), '#,##0.00'), ',.', ' ,')"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_9B and $after/crd:P_9B and $before/crd:P_9B != $after/crd:P_9B">
                                                    <xsl:choose>
                                                        <xsl:when test="$useExtendedDecimalPlaces">
                                                            <xsl:value-of select="translate(format-number(number($after/crd:P_9B) - number($before/crd:P_9B), '#,##0.0000'), ',.', ' ,')"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="translate(format-number(number($after/crd:P_9B) - number($before/crd:P_9B), '#,##0.00'), ',.', ' ,')"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_9B) and $after/crd:P_9B">
                                                    <xsl:choose>
                                                        <xsl:when test="$useExtendedDecimalPlaces">
                                                            <xsl:value-of select="translate(format-number(number($after/crd:P_9B), '#,##0.0000'), ',.', ' ,')"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="translate(format-number(number($after/crd:P_9B), '#,##0.00'), ',.', ' ,')"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_9B and not($after/crd:P_9B)">
                                                    <xsl:choose>
                                                        <xsl:when test="$useExtendedDecimalPlaces">
                                                            <xsl:value-of select="translate(format-number(-number($before/crd:P_9B), '#,##0.0000'), ',.', ' ,')"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="translate(format-number(-number($before/crd:P_9B), '#,##0.00'), ',.', ' ,')"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
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
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_10), '#,##0.00'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_10 and $after/crd:P_10 and $before/crd:P_10 != $after/crd:P_10">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_10) - number($before/crd:P_10), '#,##0.00'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="not($before/crd:P_10) and $after/crd:P_10">
                                                    <xsl:value-of select="translate(format-number(number($after/crd:P_10), '#,##0.00'), ',.', ' ,')"/>
                                                </xsl:when>
                                                <xsl:when test="$before/crd:P_10 and not($after/crd:P_10)">
                                                    <xsl:value-of select="translate(format-number(-number($before/crd:P_10), '#,##0.00'), ',.', ' ,')"/>
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
                        </fo:table-row>
                </xsl:for-each>
            </fo:table-body>
        </fo:table>
    </xsl:template>

</xsl:stylesheet>
