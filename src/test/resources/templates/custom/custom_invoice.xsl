<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <!-- Accept params that PdfGenerator always sets (even if unused here). -->
    <xsl:param name="labels"/>
    <xsl:param name="nrKsef"/>
    <xsl:param name="issuerUser"/>
    <xsl:param name="showFooter"/>
    <xsl:param name="useExtendedDecimalPlaces"/>
    <xsl:param name="showCorrectionDifferences"/>
    <xsl:param name="duplicateDate"/>
    <xsl:param name="currencyDate"/>
    <xsl:param name="qrCodesCount"/>
    <xsl:param name="customPropertyDemo"/>

    <xsl:template match="/">
        <fo:root>
            <fo:layout-master-set>
                <fo:simple-page-master master-name="A4"
                                       page-height="29.7cm"
                                       page-width="21cm"
                                       margin="1cm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="A4">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-size="12pt" font-family="Helvetica">
                        CUSTOM_TEMPLATE_MARKER
                    </fo:block>
                    <fo:block font-size="10pt" font-family="Helvetica">
                        <xsl:text>nrKsef=</xsl:text>
                        <xsl:value-of select="$nrKsef"/>
                    </fo:block>
                    <fo:block font-size="10pt" font-family="Helvetica">
                        <xsl:text>customPropertyDemo=</xsl:text>
                        <xsl:value-of select="$customPropertyDemo"/>
                    </fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>
