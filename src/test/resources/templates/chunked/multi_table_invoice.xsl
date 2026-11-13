<?xml version="1.0" encoding="UTF-8"?>
<!--
  Test template for chunked rendering (ChunkedInvoiceRenderingTest): two long tables in two page sequences,
  first/rest page masters, a portrait and a landscape sequence, rows that break across pages, a row span,
  text between the tables and page numbers in the footer.
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:param name="pageReference" select="'false'"/>

    <xsl:variable name="rows" select="//*[local-name() = 'Fa']/*[local-name() = 'FaWiersz']"/>

    <xsl:template match="/">
        <fo:root font-family="Helvetica" font-size="9pt">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="first" page-height="29.7cm" page-width="21cm" margin="1cm">
                    <fo:region-body margin-bottom="2cm"/>
                    <fo:region-after region-name="footer-first" extent="1.5cm"/>
                </fo:simple-page-master>
                <fo:simple-page-master master-name="rest" page-height="29.7cm" page-width="21cm" margin="1cm">
                    <fo:region-body margin-bottom="1cm"/>
                    <fo:region-after region-name="footer-rest" extent="0.8cm"/>
                </fo:simple-page-master>
                <fo:simple-page-master master-name="landscape" page-height="21cm" page-width="29.7cm" margin="1cm">
                    <fo:region-body margin-bottom="1cm"/>
                    <fo:region-after region-name="footer-rest" extent="0.8cm"/>
                </fo:simple-page-master>
                <fo:page-sequence-master master-name="invoice">
                    <fo:repeatable-page-master-alternatives>
                        <fo:conditional-page-master-reference page-position="first" master-reference="first"/>
                        <fo:conditional-page-master-reference page-position="rest" master-reference="rest"/>
                    </fo:repeatable-page-master-alternatives>
                </fo:page-sequence-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="invoice">
                <fo:static-content flow-name="footer-first">
                    <fo:block>FIRST PAGE FOOTER, page <fo:page-number/></fo:block>
                </fo:static-content>
                <fo:static-content flow-name="footer-rest">
                    <fo:block>page <fo:page-number/>
                        <xsl:if test="$pageReference = 'true'"> of <fo:page-number-citation ref-id="last"/></xsl:if>
                    </fo:block>
                </fo:static-content>
                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-size="16pt" space-after="1cm">INVOICE HEADER</fo:block>
                    <fo:block>
                        <fo:table table-layout="fixed" width="100%" border-collapse="collapse" space-after="5mm">
                            <fo:table-column column-width="10%"/>
                            <fo:table-column column-width="70%"/>
                            <fo:table-column column-width="20%"/>
                            <fo:table-header>
                                <fo:table-row>
                                    <fo:table-cell border="solid 0.5pt black"><fo:block>No</fo:block></fo:table-cell>
                                    <fo:table-cell border="solid 0.5pt black"><fo:block>Name</fo:block></fo:table-cell>
                                    <fo:table-cell border="solid 0.5pt black"><fo:block>Amount</fo:block></fo:table-cell>
                                </fo:table-row>
                            </fo:table-header>
                            <fo:table-body>
                                <xsl:for-each select="$rows">
                                    <fo:table-row keep-together.within-page="always">
                                        <fo:table-cell border="solid 0.5pt black">
                                            <fo:block><xsl:value-of select="position()"/></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell border="solid 0.5pt black">
                                            <fo:block><xsl:value-of select="*[local-name() = 'P_7']"/></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell border="solid 0.5pt black">
                                            <fo:block text-align="right"><xsl:value-of select="*[local-name() = 'P_11']"/></fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:for-each>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <fo:block space-before="5mm">SUMMARY BETWEEN TABLES: <xsl:value-of select="count($rows)"/> positions</fo:block>
                </fo:flow>
            </fo:page-sequence>

            <fo:page-sequence master-reference="landscape">
                <fo:static-content flow-name="footer-rest">
                    <fo:block>attachment page <fo:page-number/></fo:block>
                </fo:static-content>
                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-weight="bold" space-after="3mm">ATTACHMENT</fo:block>
                    <fo:table table-layout="fixed" width="100%" border-collapse="collapse">
                        <fo:table-column column-width="10%"/>
                        <fo:table-column column-width="60%"/>
                        <fo:table-column column-width="30%"/>
                        <fo:table-header>
                            <fo:table-row>
                                <fo:table-cell><fo:block>No</fo:block></fo:table-cell>
                                <fo:table-cell><fo:block>Description</fo:block></fo:table-cell>
                                <fo:table-cell><fo:block>Note</fo:block></fo:table-cell>
                            </fo:table-row>
                        </fo:table-header>
                        <fo:table-body>
                            <xsl:for-each select="$rows">
                                <xsl:variable name="no" select="position()"/>
                                <fo:table-row>
                                    <fo:table-cell border="solid 0.5pt black">
                                        <fo:block>A<xsl:value-of select="$no"/></fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell border="solid 0.5pt black">
                                        <xsl:for-each select="1 to ($no mod 4) + 1">
                                            <fo:block>ATT-<xsl:value-of select="$no"/> line <xsl:value-of select="."/></fo:block>
                                        </xsl:for-each>
                                    </fo:table-cell>
                                    <xsl:if test="$no mod 10 != 2">
                                        <fo:table-cell border="solid 0.5pt black">
                                            <xsl:if test="$no mod 10 = 1">
                                                <xsl:attribute name="number-rows-spanned" select="2"/>
                                            </xsl:if>
                                            <fo:block>note <xsl:value-of select="$no"/></fo:block>
                                        </fo:table-cell>
                                    </xsl:if>
                                </fo:table-row>
                            </xsl:for-each>
                        </fo:table-body>
                    </fo:table>
                    <fo:block id="last" space-before="5mm">END OF ATTACHMENT</fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>
