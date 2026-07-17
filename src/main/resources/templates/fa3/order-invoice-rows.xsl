<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:crd="http://crd.gov.pl/wzor/2025/06/25/13775/"
                xmlns:local="urn:local">

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

    <!-- Template for rendering one set of order rows. The before/after correction split
         (StanPrzedZ) is decided by the caller, so this table has no "Stan przed" column. -->
    <xsl:template name="zamowienieTable">
        <xsl:param name="zamowienieWiersz"/>

        <!-- Only output table when there is at least one row (avoids empty fo:table-body which is invalid in XSL-FO) -->
        <xsl:if test="$zamowienieWiersz">
        <!-- Check if columns should be displayed -->
        <xsl:variable name="showP9AZ" select="boolean($zamowienieWiersz[crd:P_9AZ])"/>
        <xsl:variable name="showP11NettoZ" select="boolean($zamowienieWiersz[crd:P_11NettoZ])"/>
        <xsl:variable name="showP11VatZ" select="boolean($zamowienieWiersz[crd:P_11VatZ])"/>
        <xsl:variable name="showUU_IDZ" select="boolean($zamowienieWiersz[crd:UU_IDZ])"/>

        <xsl:variable name="fixedColumnsSum" select="local:orderFixedColumnsSum($showUU_IDZ, $showP9AZ, $showP11NettoZ, $showP11VatZ)"/>
        <xsl:variable name="columnScale" select="local:orderColumnScale($fixedColumnsSum, true())"/>
        <xsl:variable name="nameColumnWidth" select="local:colPct(local:orderNameColumnWidth($fixedColumnsSum, $columnScale, true()))"/>
        <xsl:variable name="lpColumnWidth" select="local:scaledCol(4, $columnScale)"/>
        <xsl:variable name="uuIdzColumnWidth" select="local:scaledCol(14, $columnScale)"/>
        <xsl:variable name="qtyColumnWidth" select="local:scaledCol(12, $columnScale)"/>
        <xsl:variable name="unitColumnWidthPct" select="local:scaledCol(6, $columnScale)"/>
        <xsl:variable name="p9azColumnWidth" select="local:scaledCol(12, $columnScale)"/>
        <xsl:variable name="p12zColumnWidth" select="local:scaledCol(8, $columnScale)"/>
        <xsl:variable name="p11nettozColumnWidth" select="local:scaledCol(12, $columnScale)"/>
        <xsl:variable name="p11vatzColumnWidth" select="local:scaledCol(10, $columnScale)"/>

        <!-- Define the table structure -->
        <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-after="5mm">
            <!-- Define table columns -->
            <fo:table-column column-width="{$lpColumnWidth}"/> <!-- Lp. -->
            <xsl:if test="$showUU_IDZ">
                <fo:table-column column-width="{$uuIdzColumnWidth}"/> <!-- Unikalny numer wiersza -->
            </xsl:if>
            <fo:table-column column-width="{$nameColumnWidth}"/> <!-- Nazwa -->
            <fo:table-column column-width="{$qtyColumnWidth}"/> <!-- Ilość -->
            <fo:table-column column-width="{$unitColumnWidthPct}"/> <!-- Jednostka -->
            <xsl:if test="$showP9AZ">
                <fo:table-column column-width="{$p9azColumnWidth}"/> <!-- Cena jednostkowa netto -->
            </xsl:if>
            <fo:table-column column-width="{$p12zColumnWidth}"/> <!-- Stawka podatku -->
            <xsl:if test="$showP11NettoZ">
                <fo:table-column column-width="{$p11nettozColumnWidth}"/> <!-- Wartość netto -->
            </xsl:if>
            <xsl:if test="$showP11VatZ">
                <fo:table-column column-width="{$p11vatzColumnWidth}"/> <!-- Kwota VAT -->
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
                </fo:table-row>
            </fo:table-header>

            <!-- Table body -->
            <fo:table-body>
                <xsl:for-each select="$zamowienieWiersz">
                    <fo:table-row>
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
                            <xsl:variable name="formattedQty" select="translate(format-number(number(crd:P_8BZ), '#,##0.######'), '.,', ',&#160;')"/>
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
                    </fo:table-row>
                </xsl:for-each>
            </fo:table-body>
        </fo:table>
        </xsl:if>
    </xsl:template>

    <!-- Differences table for order rows: shows after-minus-before per NrWierszaZam,
         mirroring differencesTable but on the Z-suffixed order fields. -->
    <xsl:template name="zamowienieDifferencesTable">
        <xsl:param name="zamowienieWierszBefore"/>
        <xsl:param name="zamowienieWierszAfter"/>

        <xsl:variable name="showP9AZ" select="boolean($zamowienieWierszAfter[crd:P_9AZ])"/>
        <xsl:variable name="showP11NettoZ" select="boolean($zamowienieWierszAfter[crd:P_11NettoZ])"/>
        <xsl:variable name="showP11VatZ" select="boolean($zamowienieWierszAfter[crd:P_11VatZ])"/>

        <xsl:variable name="diffFixedColumnsSum" select="local:orderFixedColumnsSum(false(), $showP9AZ, $showP11NettoZ, $showP11VatZ)"/>
        <xsl:variable name="diffColumnScale" select="local:orderColumnScale($diffFixedColumnsSum, true())"/>
        <xsl:variable name="nameColumnWidth" select="local:colPct(local:orderNameColumnWidth($diffFixedColumnsSum, $diffColumnScale, true()))"/>
        <xsl:variable name="lpColumnWidth" select="local:scaledCol(4, $diffColumnScale)"/>
        <xsl:variable name="qtyColumnWidth" select="local:scaledCol(12, $diffColumnScale)"/>
        <xsl:variable name="unitColumnWidthPct" select="local:scaledCol(6, $diffColumnScale)"/>
        <xsl:variable name="p9azColumnWidth" select="local:scaledCol(12, $diffColumnScale)"/>
        <xsl:variable name="p12zColumnWidth" select="local:scaledCol(8, $diffColumnScale)"/>
        <xsl:variable name="p11nettozColumnWidth" select="local:scaledCol(12, $diffColumnScale)"/>
        <xsl:variable name="p11vatzColumnWidth" select="local:scaledCol(10, $diffColumnScale)"/>

        <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-after="5mm">
            <fo:table-column column-width="{$lpColumnWidth}"/> <!-- Lp. -->
            <fo:table-column column-width="{$nameColumnWidth}"/> <!-- Nazwa -->
            <fo:table-column column-width="{$qtyColumnWidth}"/> <!-- Ilość -->
            <fo:table-column column-width="{$unitColumnWidthPct}"/> <!-- Jednostka -->
            <xsl:if test="$showP9AZ">
                <fo:table-column column-width="{$p9azColumnWidth}"/> <!-- Cena jednostkowa netto -->
            </xsl:if>
            <fo:table-column column-width="{$p12zColumnWidth}"/> <!-- Stawka podatku -->
            <xsl:if test="$showP11NettoZ">
                <fo:table-column column-width="{$p11nettozColumnWidth}"/> <!-- Wartość netto -->
            </xsl:if>
            <xsl:if test="$showP11VatZ">
                <fo:table-column column-width="{$p11vatzColumnWidth}"/> <!-- Kwota VAT -->
            </xsl:if>

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
                </fo:table-row>
            </fo:table-header>

            <fo:table-body>
                <xsl:for-each select="$zamowienieWierszAfter">
                    <xsl:variable name="lineNum" select="crd:NrWierszaZam"/>
                    <xsl:variable name="after" select="."/>
                    <xsl:variable name="before" select="$zamowienieWierszBefore[crd:NrWierszaZam = $lineNum]"/>
                    <xsl:variable name="isNewRow" select="not($before)"/>
                    <fo:table-row>
                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="$lineNum"/></fo:block>
                        </fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="$after/crd:P_7Z"/></fo:block>
                        </fo:table-cell>

                        <!-- Quantity difference -->
                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                            <xsl:variable name="formattedQty">
                                <xsl:choose>
                                    <xsl:when test="$isNewRow">
                                        <xsl:value-of select="translate(format-number(number($after/crd:P_8BZ), '#,##0.######'), '.,', ',&#160;')"/>
                                    </xsl:when>
                                    <xsl:when test="$before/crd:P_8BZ and $after/crd:P_8BZ">
                                        <xsl:value-of select="translate(format-number(number($after/crd:P_8BZ) - number($before/crd:P_8BZ), '#,##0.######'), '.,', ',&#160;')"/>
                                    </xsl:when>
                                    <xsl:when test="not($before/crd:P_8BZ) and $after/crd:P_8BZ">
                                        <xsl:value-of select="translate(format-number(number($after/crd:P_8BZ), '#,##0.######'), '.,', ',&#160;')"/>
                                    </xsl:when>
                                    <xsl:when test="$before/crd:P_8BZ and not($after/crd:P_8BZ)">
                                        <xsl:value-of select="translate(format-number(-number($before/crd:P_8BZ), '#,##0.######'), '.,', ',&#160;')"/>
                                    </xsl:when>
                                    <xsl:otherwise>0</xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <xsl:variable name="qtyLength" select="string-length($formattedQty)"/>
                            <xsl:choose>
                                <xsl:when test="$qtyLength &gt; 14">
                                    <fo:block font-size="5pt"><xsl:value-of select="$formattedQty"/></fo:block>
                                </xsl:when>
                                <xsl:when test="$qtyLength &gt; 10">
                                    <fo:block font-size="6pt"><xsl:value-of select="$formattedQty"/></fo:block>
                                </xsl:when>
                                <xsl:otherwise>
                                    <fo:block><xsl:value-of select="$formattedQty"/></fo:block>
                                </xsl:otherwise>
                            </xsl:choose>
                        </fo:table-cell>

                        <!-- Unit - show only "after" value -->
                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                            <fo:block>
                                <xsl:value-of select="$after/crd:P_8AZ"/>
                            </fo:block>
                        </fo:table-cell>

                        <!-- Net unit price difference -->
                        <xsl:if test="$showP9AZ">
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                <fo:block>
                                    <xsl:variable name="formattedNumber">
                                        <xsl:choose>
                                            <xsl:when test="$isNewRow and $after/crd:P_9AZ">
                                                <xsl:value-of select="translate(format-number(number($after/crd:P_9AZ), '#,##0.########'), ',.', ' ,')"/>
                                            </xsl:when>
                                            <xsl:when test="$before/crd:P_9AZ and $after/crd:P_9AZ and $before/crd:P_9AZ != $after/crd:P_9AZ">
                                                <xsl:value-of select="translate(format-number(number($after/crd:P_9AZ) - number($before/crd:P_9AZ), '#,##0.########'), ',.', ' ,')"/>
                                            </xsl:when>
                                            <xsl:when test="not($before/crd:P_9AZ) and $after/crd:P_9AZ">
                                                <xsl:value-of select="translate(format-number(number($after/crd:P_9AZ), '#,##0.########'), ',.', ' ,')"/>
                                            </xsl:when>
                                            <xsl:when test="$before/crd:P_9AZ and not($after/crd:P_9AZ)">
                                                <xsl:value-of select="translate(format-number(-number($before/crd:P_9AZ), '#,##0.########'), ',.', ' ,')"/>
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
                                <xsl:variable name="taxRate" select="$after/crd:P_12Z"/>
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

                        <!-- Net value difference -->
                        <xsl:if test="$showP11NettoZ">
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                <fo:block>
                                    <xsl:variable name="formattedNumber">
                                        <xsl:choose>
                                            <xsl:when test="$isNewRow and $after/crd:P_11NettoZ">
                                                <xsl:value-of select="translate(format-number(number($after/crd:P_11NettoZ), '#,##0.00'), ',.', ' ,')"/>
                                            </xsl:when>
                                            <xsl:when test="$before/crd:P_11NettoZ and $after/crd:P_11NettoZ and $before/crd:P_11NettoZ != $after/crd:P_11NettoZ">
                                                <xsl:value-of select="translate(format-number(number($after/crd:P_11NettoZ) - number($before/crd:P_11NettoZ), '#,##0.00'), ',.', ' ,')"/>
                                            </xsl:when>
                                            <xsl:when test="not($before/crd:P_11NettoZ) and $after/crd:P_11NettoZ">
                                                <xsl:value-of select="translate(format-number(number($after/crd:P_11NettoZ), '#,##0.00'), ',.', ' ,')"/>
                                            </xsl:when>
                                            <xsl:when test="$before/crd:P_11NettoZ and not($after/crd:P_11NettoZ)">
                                                <xsl:value-of select="translate(format-number(-number($before/crd:P_11NettoZ), '#,##0.00'), ',.', ' ,')"/>
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

                        <!-- VAT amount difference -->
                        <xsl:if test="$showP11VatZ">
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                <fo:block>
                                    <xsl:variable name="formattedNumber">
                                        <xsl:choose>
                                            <xsl:when test="$isNewRow and $after/crd:P_11VatZ">
                                                <xsl:value-of select="translate(format-number(number($after/crd:P_11VatZ), '#,##0.00'), ',.', ' ,')"/>
                                            </xsl:when>
                                            <xsl:when test="$before/crd:P_11VatZ and $after/crd:P_11VatZ and $before/crd:P_11VatZ != $after/crd:P_11VatZ">
                                                <xsl:value-of select="translate(format-number(number($after/crd:P_11VatZ) - number($before/crd:P_11VatZ), '#,##0.00'), ',.', ' ,')"/>
                                            </xsl:when>
                                            <xsl:when test="not($before/crd:P_11VatZ) and $after/crd:P_11VatZ">
                                                <xsl:value-of select="translate(format-number(number($after/crd:P_11VatZ), '#,##0.00'), ',.', ' ,')"/>
                                            </xsl:when>
                                            <xsl:when test="$before/crd:P_11VatZ and not($after/crd:P_11VatZ)">
                                                <xsl:value-of select="translate(format-number(-number($before/crd:P_11VatZ), '#,##0.00'), ',.', ' ,')"/>
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
