<?xml version="1.0" encoding="UTF-8"?>
<!-- Minimal invoice template implementing ChunkTemplateContract, used by ChunkedInvoiceRenderingTest. -->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:param name="chunkMode" select="''"/>
    <xsl:param name="chunkFrom" select="1"/>
    <xsl:param name="chunkTo" select="0"/>
    <xsl:param name="chunkInitialPage" select="0"/>

    <xsl:variable name="head" select="string($chunkMode) = ('', 'head')"/>
    <xsl:variable name="tail" select="string($chunkMode) = ('', 'tail')"/>
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
                <fo:page-sequence-master master-name="invoice">
                    <fo:repeatable-page-master-alternatives>
                        <fo:conditional-page-master-reference page-position="first" master-reference="first"/>
                        <fo:conditional-page-master-reference page-position="rest" master-reference="rest"/>
                    </fo:repeatable-page-master-alternatives>
                </fo:page-sequence-master>
                <fo:page-sequence-master master-name="continuation">
                    <fo:repeatable-page-master-reference master-reference="rest"/>
                </fo:page-sequence-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="{if (string($chunkMode) = ('middle', 'tail')) then 'continuation' else 'invoice'}">
                <xsl:if test="number($chunkInitialPage) gt 0">
                    <xsl:attribute name="initial-page-number" select="$chunkInitialPage"/>
                </xsl:if>
                <fo:static-content flow-name="footer-first">
                    <fo:block>FIRST PAGE FOOTER, page <fo:page-number/></fo:block>
                </fo:static-content>
                <fo:static-content flow-name="footer-rest">
                    <fo:block>page <fo:page-number/></fo:block>
                </fo:static-content>
                <fo:flow flow-name="xsl-region-body">
                    <xsl:if test="$head">
                        <fo:block font-size="16pt" space-after="1cm">INVOICE HEADER</fo:block>
                    </xsl:if>
                    <fo:block>
                        <fo:table table-layout="fixed" width="100%" border-collapse="collapse">
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
                                <xsl:for-each select="$rows[position() ge number($chunkFrom)
                                                            and (number($chunkTo) le 0 or position() le number($chunkTo))]">
                                    <xsl:variable name="rowNo" select="position() + number($chunkFrom) - 1"/>
                                    <fo:table-row keep-together.within-page="always">
                                        <fo:table-cell border="solid 0.5pt black">
                                            <fo:block>
                                                <xsl:if test="string($chunkMode) != ''">
                                                    <xsl:attribute name="id" select="concat('ksefrow-', $rowNo)"/>
                                                </xsl:if>
                                                <xsl:value-of select="$rowNo"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell border="solid 0.5pt black">
                                            <fo:block><xsl:value-of select="*[local-name() = 'P_7']"/></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell border="solid 0.5pt black">
                                            <fo:block><xsl:value-of select="*[local-name() = 'P_11']"/></fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:for-each>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <xsl:if test="$tail">
                        <fo:block space-before="5mm">INVOICE SUMMARY: <xsl:value-of select="count($rows)"/> positions</fo:block>
                    </xsl:if>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>
