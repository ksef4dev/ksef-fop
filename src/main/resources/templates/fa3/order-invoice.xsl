<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:crd="http://crd.gov.pl/wzor/2025/06/25/13775/"
                xmlns:local="urn:local">

    <!-- Note: $labels parameter, kLabels key and local: functions are defined in the main ksef_invoice.xsl -->

    <!-- Row-level tables (zamowienieTable) live here; included so the order section is self-contained and overridable on its own. -->
    <xsl:include href="order-invoice-rows.xsl"/>

    <!--
        Order ("Zamówienie") section.
    -->
    <xsl:template match="crd:Zamowienie">
        <fo:block id="Zamowienie">
            <!-- Section title -->
            <fo:block text-align="left" space-after="2mm" space-before="2mm">
                <fo:inline font-weight="bold" font-size="12pt"><xsl:value-of select="key('kLabels', 'order', $labels)"/></fo:inline>
            </fo:block>

            <!-- Wartość zamówienia lub umowy z uwzględnieniem kwoty podatku -->
            <xsl:if test="crd:WartoscZamowienia">
                <fo:block font-size="8pt" font-weight="bold" text-align="left" space-before="1mm" space-after="3mm">
                    <fo:inline><xsl:value-of select="key('kLabels', 'orderValueWithTax', $labels)"/>: </fo:inline>
                    <fo:inline>
                        <xsl:value-of select="local:format-amount(crd:WartoscZamowienia)"/>
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="../crd:KodWaluty"/>
                    </fo:inline>
                </fo:block>
            </xsl:if>

            <!-- Order rows: mirror the FaWiersz before/after correction layout, keyed on StanPrzedZ -->
            <xsl:choose>
                <!-- Correction with both states: before, optional differences, after -->
                <xsl:when test="crd:ZamowienieWiersz[crd:StanPrzedZ = 1] and crd:ZamowienieWiersz[not(crd:StanPrzedZ)]">
                    <fo:block text-align="left" space-after="1mm">
                        <fo:inline font-weight="bold" font-size="10pt"><xsl:value-of select="key('kLabels', 'orderBeforeCorrection', $labels)"/></fo:inline>
                    </fo:block>
                    <xsl:call-template name="zamowienieTable">
                        <xsl:with-param name="zamowienieWiersz" select="crd:ZamowienieWiersz[crd:StanPrzedZ = 1]"/>
                    </xsl:call-template>

                    <xsl:if test="$showCorrectionDifferences">
                        <fo:block text-align="left" space-after="1mm">
                            <fo:inline font-weight="bold" font-size="10pt"><xsl:value-of select="key('kLabels', 'difference', $labels)"/></fo:inline>
                        </fo:block>
                        <xsl:call-template name="zamowienieDifferencesTable">
                            <xsl:with-param name="zamowienieWierszBefore" select="crd:ZamowienieWiersz[crd:StanPrzedZ = 1]"/>
                            <xsl:with-param name="zamowienieWierszAfter" select="crd:ZamowienieWiersz[not(crd:StanPrzedZ)]"/>
                        </xsl:call-template>
                    </xsl:if>

                    <fo:block text-align="left" space-after="1mm">
                        <fo:inline font-weight="bold" font-size="10pt"><xsl:value-of select="key('kLabels', 'orderAfterCorrection', $labels)"/></fo:inline>
                    </fo:block>
                    <xsl:call-template name="zamowienieTable">
                        <xsl:with-param name="zamowienieWiersz" select="crd:ZamowienieWiersz[not(crd:StanPrzedZ)]"/>
                    </xsl:call-template>
                </xsl:when>
                <!-- Correction with only "before" rows -->
                <xsl:when test="crd:ZamowienieWiersz[crd:StanPrzedZ = 1]">
                    <fo:block text-align="left" space-after="1mm">
                        <fo:inline font-weight="bold" font-size="10pt"><xsl:value-of select="key('kLabels', 'orderBeforeCorrection', $labels)"/></fo:inline>
                    </fo:block>
                    <xsl:call-template name="zamowienieTable">
                        <xsl:with-param name="zamowienieWiersz" select="crd:ZamowienieWiersz[crd:StanPrzedZ = 1]"/>
                    </xsl:call-template>
                </xsl:when>
                <!-- No correction state: a single table -->
                <xsl:when test="crd:ZamowienieWiersz">
                    <xsl:call-template name="zamowienieTable">
                        <xsl:with-param name="zamowienieWiersz" select="crd:ZamowienieWiersz"/>
                    </xsl:call-template>
                </xsl:when>
            </xsl:choose>
        </fo:block>
    </xsl:template>

</xsl:stylesheet>