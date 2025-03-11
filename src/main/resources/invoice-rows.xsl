<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:crd="http://crd.gov.pl/wzor/2023/06/29/12648/">

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
            <fo:table-column column-width="8%"/> <!-- Stawka podatku -->
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
                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                        <fo:block>Stawka podatku</fo:block>
                    </fo:table-cell>
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
            <xsl:if test="//crd:FaWiersz/crd:P_9A">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_9A">
                                <xsl:value-of select="translate(format-number(number(crd:P_9A), '#,##0.00'), ',.', ' ,')"/> <!-- Wartość sprzedaży brutto -->
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
                                <xsl:value-of select="translate(format-number(number(crd:P_9B), '#,##0.00'), ',.', ' ,')"/> <!-- Wartość sprzedaży brutto -->
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
                                <xsl:value-of select="translate(format-number(number(crd:P_10), '#,##0.00'), ',.', ' ,')"/> <!-- Rabat-->
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
                    <xsl:variable name="taxRate" select="crd:P_12"/>
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
            <xsl:if test="//crd:FaWiersz/crd:P_11">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:P_11">
                                <xsl:value-of select="translate(format-number(number(crd:P_11), '#,##0.00'), ',.', ' ,')"/> <!-- Wartość sprzedaży netto -->
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
                                <xsl:value-of select="translate(format-number(number(crd:P_11Vat), '#,##0.00'), ',.', ' ,')"/> <!-- Kwota VAT-->
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
                                <xsl:value-of select="translate(format-number(number(crd:P_11A), '#,##0.00'), ',.', ' ,')"/> <!-- Wartość sprzedaży brutto -->
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

</xsl:stylesheet>
