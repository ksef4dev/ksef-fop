<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:crd="http://crd.gov.pl/wzor/2026/03/06/14189/"
                xmlns:local="urn:local"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <xsl:import href="../fa3/common-functions.xsl"/>

    <xsl:variable name="kodyKrajowXSD" select="document('http://crd.gov.pl/xml/schematy/dziedzinowe/mf/2022/01/05/eD/DefinicjeTypy/KodyKrajow_v10-0E.xsd')"/>

    <xsl:param name="nrKsef"/>
    <xsl:param name="logo"/>
    <xsl:param name="logoUri"/>
    <xsl:param name="showFooter"/>
    <xsl:param name="duplicateDate"/>
    <xsl:param name="currencyDate"/>
    <xsl:param name="issuerUser"/>
    <xsl:param name="labels"/>
    <xsl:key name="kLabels" match="entry" use="@key"/>

    <xsl:param name="qrCodesCount" select="0"/>
    <xsl:param name="qrCode0"/>
    <xsl:param name="qrCodeLabel0"/>
    <xsl:param name="verificationLink0"/>
    <xsl:param name="verificationLinkTitle0"/>
    <xsl:param name="qrCode1"/>
    <xsl:param name="qrCodeLabel1"/>
    <xsl:param name="verificationLink1"/>
    <xsl:param name="verificationLinkTitle1"/>

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

    <xsl:template match="/crd:Faktura">
        <fo:root font-family="sans">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="A4"
                                       page-height="29.7cm" page-width="21.0cm"
                                       margin-top="1cm" margin-left="1cm" margin-right="1cm" margin-bottom="1cm">
                    <fo:region-body/>
                    <fo:region-after region-name="xsl-region-after"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="A4">
                <xsl:if test="$showFooter">
                    <fo:static-content flow-name="xsl-region-after">
                        <fo:block border-bottom="solid 1px black" padding-bottom="2mm" space-after="2mm"/>
                        <fo:block font-size="8pt" text-align="left">
                            <xsl:value-of select="key('kLabels', 'generatedIn', $labels)"/>:
                            <xsl:value-of select="crd:Naglowek/crd:SystemInfo"/>
                        </fo:block>
                    </fo:static-content>
                </xsl:if>

                <fo:flow flow-name="xsl-region-body" color="#343a40">
                    <fo:block font-size="20pt" font-weight="bold" text-align="left">
                        <xsl:if test="$logo != ''">
                            <fo:external-graphic content-width="80pt" content-height="80pt" src="url('data:image/svg;base64,{$logo}')"/>
                        </xsl:if>
                        <xsl:if test="$logoUri != ''">
                            <fo:external-graphic content-width="80pt" content-height="80pt" src="url('{$logoUri}')"/>
                        </xsl:if>
                    </fo:block>

                    <fo:block font-size="9pt" text-align="right" padding-top="-5mm">
                        <xsl:value-of select="key('kLabels', 'invoice.number', $labels)"/>
                    </fo:block>
                    <fo:block font-size="16pt" text-align="right" space-after="2mm">
                        <fo:inline font-weight="bold">
                            <xsl:value-of select="crd:FakturaRR/crd:P_4C"/>
                        </fo:inline>
                    </fo:block>
                    <xsl:if test="$duplicateDate">
                        <fo:block font-size="10pt" color="grey" text-align="right" space-after="2mm">
                            <xsl:value-of select="key('kLabels', 'duplicate.fromDate', $labels)"/>
                            <xsl:text> </xsl:text>
                            <xsl:value-of select="$duplicateDate"/>
                        </fo:block>
                    </xsl:if>
                    <fo:block font-size="9pt" text-align="right" space-after="2mm">
                        <xsl:choose>
                            <xsl:when test="crd:FakturaRR/crd:RodzajFaktury = 'VAT_RR'">
                                <xsl:value-of select="key('kLabels', 'invoice.vatRr', $labels)"/>
                            </xsl:when>
                            <xsl:when test="crd:FakturaRR/crd:RodzajFaktury = 'KOR_VAT_RR'">
                                <xsl:value-of select="key('kLabels', 'invoice.correctionVatRr', $labels)"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="key('kLabels', 'invoice.unknownType', $labels)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>
                    <xsl:if test="$nrKsef">
                        <fo:block font-size="9pt" text-align="right" space-after="5mm">
                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'ksef.number', $labels)"/>: </fo:inline>
                            <xsl:value-of select="$nrKsef"/>
                        </fo:block>
                    </xsl:if>

                    <xsl:call-template name="renderCorrectedInvoiceData"/>
                    <xsl:call-template name="renderCorrectedParty">
                        <xsl:with-param name="title" select="key('kLabels', 'supplier', $labels)"/>
                        <xsl:with-param name="oldParty" select="crd:FakturaRR/crd:Podmiot1K"/>
                        <xsl:with-param name="newParty" select="crd:Podmiot1"/>
                    </xsl:call-template>
                    <xsl:call-template name="renderCorrectedParty">
                        <xsl:with-param name="title" select="key('kLabels', 'buyer', $labels)"/>
                        <xsl:with-param name="oldParty" select="crd:FakturaRR/crd:Podmiot2K"/>
                        <xsl:with-param name="newParty" select="crd:Podmiot2"/>
                    </xsl:call-template>

                    <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
                    <fo:table font-size="7pt" table-layout="fixed" width="100%">
                        <fo:table-column column-width="33%"/>
                        <fo:table-column column-width="33%"/>
                        <fo:table-column column-width="33%"/>
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell padding-bottom="8px">
                                    <fo:block font-size="12pt" font-weight="bold"><xsl:value-of select="key('kLabels', 'supplier', $labels)"/></fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding-bottom="8px">
                                    <fo:block font-size="12pt" font-weight="bold">
                                        <xsl:if test="crd:Podmiot3[crd:Rola = 5]">
                                            <xsl:value-of select="key('kLabels', 'issuer', $labels)"/>
                                        </xsl:if>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding-bottom="8px">
                                    <fo:block font-size="12pt" font-weight="bold"><xsl:value-of select="key('kLabels', 'buyer', $labels)"/></fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell padding-right="4pt">
                                    <xsl:apply-templates select="crd:Podmiot1"/>
                                </fo:table-cell>
                                <fo:table-cell padding-left="4pt" padding-right="4pt">
                                    <fo:block>
                                        <xsl:for-each select="crd:Podmiot3[crd:Rola = 5]">
                                            <xsl:apply-templates select="."/>
                                        </xsl:for-each>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding-left="4pt">
                                    <xsl:apply-templates select="crd:Podmiot2"/>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>

                    <xsl:variable name="otherParties" select="crd:Podmiot3[crd:RolaInna or crd:Rola != 5]"/>
                    <xsl:if test="$otherParties">
                        <fo:block border-bottom="solid 1px grey" space-before="5mm"/>
                        <fo:table table-layout="fixed" width="100%">
                            <fo:table-column column-width="50%"/>
                            <fo:table-column column-width="50%"/>
                            <fo:table-body>
                                <xsl:for-each-group select="$otherParties" group-adjacent="(position() - 1) idiv 2">
                                    <fo:table-row>
                                        <fo:table-cell padding-right="4pt">
                                            <fo:block font-size="7pt">
                                                <xsl:apply-templates select="current-group()[1]"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell padding-left="4pt">
                                            <fo:block font-size="7pt">
                                                <xsl:apply-templates select="current-group()[2]"/>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:for-each-group>
                            </fo:table-body>
                        </fo:table>
                    </xsl:if>

                    <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
                    <fo:block font-size="12pt" font-weight="bold" text-align="left" space-after="3mm">
                        <xsl:value-of select="key('kLabels', 'details', $labels)"/>
                    </fo:block>
                    <fo:table space-after="5mm" table-layout="fixed" width="100%">
                        <fo:table-column column-width="50%"/>
                        <fo:table-column column-width="50%"/>
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell padding-right="6pt">
                                    <xsl:call-template name="detailLine">
                                        <xsl:with-param name="label" select="key('kLabels', 'issueDate', $labels)"/>
                                        <xsl:with-param name="value" select="crd:FakturaRR/crd:P_4B"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="detailLine">
                                        <xsl:with-param name="label" select="key('kLabels', 'acquisitionDate', $labels)"/>
                                        <xsl:with-param name="value" select="crd:FakturaRR/crd:P_4A"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="detailLine">
                                        <xsl:with-param name="label" select="key('kLabels', 'correctionReason', $labels)"/>
                                        <xsl:with-param name="value" select="crd:FakturaRR/crd:PrzyczynaKorekty"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="detailLine">
                                        <xsl:with-param name="label" select="key('kLabels', 'correctedInvoiceNumber', $labels)"/>
                                        <xsl:with-param name="value" select="crd:FakturaRR/crd:NrFaKorygowany"/>
                                    </xsl:call-template>
                                </fo:table-cell>
                                <fo:table-cell padding-left="6pt">
                                    <xsl:call-template name="detailLine">
                                        <xsl:with-param name="label" select="key('kLabels', 'issuePlace', $labels)"/>
                                        <xsl:with-param name="value" select="crd:FakturaRR/crd:P_1M"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="detailLine">
                                        <xsl:with-param name="label" select="key('kLabels', 'invoiceInCurrency', $labels)"/>
                                        <xsl:with-param name="value" select="crd:FakturaRR/crd:KodWaluty"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="detailLine">
                                        <xsl:with-param name="label" select="key('kLabels', 'exchangeRate', $labels)"/>
                                        <xsl:with-param name="value" select="crd:FakturaRR/crd:FakturaRRWiersz[crd:KursWaluty][1]/crd:KursWaluty"/>
                                    </xsl:call-template>
                                    <xsl:call-template name="correctionTypeLine">
                                        <xsl:with-param name="type" select="crd:FakturaRR/crd:TypKorekty"/>
                                    </xsl:call-template>
                                    <xsl:if test="$currencyDate">
                                        <xsl:call-template name="detailLine">
                                            <xsl:with-param name="label" select="key('kLabels', 'exchangeRateDate', $labels)"/>
                                            <xsl:with-param name="value" select="$currencyDate"/>
                                        </xsl:call-template>
                                    </xsl:if>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>
                    <xsl:if test="crd:FakturaRR/crd:P_4B">
                        <fo:block font-size="7pt" text-align="left" space-before="2mm">
                            <xsl:value-of select="key('kLabels', 'issueDate.footnote', $labels)"/>
                        </fo:block>
                    </xsl:if>

                    <xsl:if test="crd:FakturaRR/crd:FakturaRRWiersz">
                        <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
                        <fo:block font-size="12pt" font-weight="bold" text-align="left" space-after="2mm">
                            <xsl:value-of select="key('kLabels', 'positions', $labels)"/>
                        </fo:block>
                        <xsl:call-template name="rrPositionsTable">
                            <xsl:with-param name="rows" select="crd:FakturaRR/crd:FakturaRRWiersz"/>
                        </xsl:call-template>
                    </xsl:if>

                    <xsl:call-template name="rrSummary"/>
                    <xsl:call-template name="paymentDocuments"/>
                    <xsl:call-template name="additionalDescription"/>
                    <xsl:apply-templates select="crd:FakturaRR/crd:Rozliczenie"/>
                    <xsl:call-template name="rrPayment"/>
                    <xsl:call-template name="registries"/>
                    <xsl:call-template name="footerInfo"/>

                    <xsl:if test="$issuerUser">
                        <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
                        <fo:block font-size="10pt" text-align="left">
                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'issuingPerson', $labels)"/>: </fo:inline>
                            <xsl:value-of select="$issuerUser"/>
                        </fo:block>
                    </xsl:if>

                    <xsl:if test="$qrCodesCount > 0">
                        <fo:block keep-together.within-page="always">
                            <fo:block font-size="12pt" text-align="left" space-before="4mm" keep-with-next.within-page="always">
                                <fo:inline font-weight="bold">
                                    <xsl:value-of select="key('kLabels', 'checkInvoiceInKsef', $labels)"/>
                                </fo:inline>
                            </fo:block>
                            <xsl:if test="$qrCode0 and $verificationLink0">
                                <xsl:call-template name="renderQrCode">
                                    <xsl:with-param name="qrCodeImage" select="$qrCode0"/>
                                    <xsl:with-param name="qrCodeLabel" select="$qrCodeLabel0"/>
                                    <xsl:with-param name="qrCodeVerificationLinkTitle" select="$verificationLinkTitle0"/>
                                    <xsl:with-param name="qrCodeVerificationLink" select="$verificationLink0"/>
                                </xsl:call-template>
                            </xsl:if>
                            <xsl:if test="$qrCode1 and $verificationLink1">
                                <xsl:call-template name="renderQrCode">
                                    <xsl:with-param name="qrCodeImage" select="$qrCode1"/>
                                    <xsl:with-param name="qrCodeLabel" select="$qrCodeLabel1"/>
                                    <xsl:with-param name="qrCodeVerificationLinkTitle" select="$verificationLinkTitle1"/>
                                    <xsl:with-param name="qrCodeVerificationLink" select="$verificationLink1"/>
                                </xsl:call-template>
                            </xsl:if>
                        </fo:block>
                    </xsl:if>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template name="rrPositionsTable">
        <xsl:param name="rows"/>
        <xsl:variable name="showLineAcquisitionDate" select="exists($rows/crd:P_4AA[normalize-space()])"/>
        <xsl:choose>
            <xsl:when test="$showLineAcquisitionDate">
                <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-after="5mm">
                    <fo:table-column column-width="6%"/>
                    <fo:table-column column-width="23%"/>
                    <fo:table-column column-width="8%"/>
                    <fo:table-column column-width="9%"/>
                    <fo:table-column column-width="8%"/>
                    <fo:table-column column-width="5%"/>
                    <fo:table-column column-width="9%"/>
                    <fo:table-column column-width="9%"/>
                    <fo:table-column column-width="6%"/>
                    <fo:table-column column-width="8%"/>
                    <fo:table-column column-width="9%"/>
                    <fo:table-header>
                        <fo:table-row background-color="#f5f5f5" font-weight="bold">
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'row.lp', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'row.productName', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'acquisitionDateShort', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rrQualityClass', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'row.quantity', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'row.unit', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rrUnitPrice', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rrPurchaseValue', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rrRefundRate', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rrRefundAmount', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rrTotalValue', $labels)"/></xsl:call-template>
                        </fo:table-row>
                    </fo:table-header>
                    <fo:table-body>
                        <xsl:apply-templates select="$rows">
                            <xsl:with-param name="showLineAcquisitionDate" select="true()" tunnel="yes"/>
                        </xsl:apply-templates>
                    </fo:table-body>
                </fo:table>
            </xsl:when>
            <xsl:otherwise>
                <fo:table table-layout="fixed" width="100%" border-collapse="separate" space-after="5mm">
                    <fo:table-column column-width="6%"/>
                    <fo:table-column column-width="31%"/>
                    <fo:table-column column-width="9%"/>
                    <fo:table-column column-width="8%"/>
                    <fo:table-column column-width="5%"/>
                    <fo:table-column column-width="9%"/>
                    <fo:table-column column-width="9%"/>
                    <fo:table-column column-width="6%"/>
                    <fo:table-column column-width="8%"/>
                    <fo:table-column column-width="9%"/>
                    <fo:table-header>
                        <fo:table-row background-color="#f5f5f5" font-weight="bold">
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'row.lp', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'row.productName', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rrQualityClass', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'row.quantity', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'row.unit', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rrUnitPrice', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rrPurchaseValue', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rrRefundRate', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rrRefundAmount', $labels)"/></xsl:call-template>
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rrTotalValue', $labels)"/></xsl:call-template>
                        </fo:table-row>
                    </fo:table-header>
                    <fo:table-body>
                        <xsl:apply-templates select="$rows">
                            <xsl:with-param name="showLineAcquisitionDate" select="false()" tunnel="yes"/>
                        </xsl:apply-templates>
                    </fo:table-body>
                </fo:table>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="crd:FakturaRRWiersz">
        <xsl:param name="showLineAcquisitionDate" tunnel="yes" select="true()"/>
        <fo:table-row>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                <fo:block><xsl:value-of select="crd:NrWierszaFa"/></fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                <fo:block><xsl:value-of select="crd:P_5"/></fo:block>
                <xsl:call-template name="inlineMeta"><xsl:with-param name="label" select="key('kLabels', 'row.uniqueRowId', $labels)"/><xsl:with-param name="value" select="crd:UU_ID"/></xsl:call-template>
                <xsl:call-template name="inlineMeta"><xsl:with-param name="label" select="key('kLabels', 'row.gtin', $labels)"/><xsl:with-param name="value" select="crd:GTIN"/></xsl:call-template>
                <xsl:call-template name="inlineMeta"><xsl:with-param name="label" select="key('kLabels', 'row.pkwiu', $labels)"/><xsl:with-param name="value" select="crd:PKWiU"/></xsl:call-template>
                <xsl:call-template name="inlineMeta"><xsl:with-param name="label" select="key('kLabels', 'row.cn', $labels)"/><xsl:with-param name="value" select="crd:CN"/></xsl:call-template>
                <xsl:if test="crd:StanPrzed = 1">
                    <fo:block font-size="6pt" color="#6c757d"><xsl:value-of select="key('kLabels', 'row.stateBefore', $labels)"/>: <xsl:value-of select="key('kLabels', 'common.yes', $labels)"/></fo:block>
                </xsl:if>
            </fo:table-cell>
            <xsl:if test="$showLineAcquisitionDate">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:P_4AA"/></fo:block></fo:table-cell>
            </xsl:if>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:P_6C"/></fo:block></fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right"><fo:block><xsl:value-of select="local:format-quantity(crd:P_6B)"/></fo:block></fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:P_6A"/></fo:block></fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right"><fo:block><xsl:value-of select="local:format-unit-price(crd:P_7)"/></fo:block></fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right"><fo:block><xsl:value-of select="local:format-amount(crd:P_8)"/></fo:block></fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right"><fo:block><xsl:value-of select="translate(string(crd:P_9), '.', ',')"/>%</fo:block></fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right"><fo:block><xsl:value-of select="local:format-amount(crd:P_10)"/></fo:block></fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right"><fo:block><xsl:value-of select="local:format-amount(crd:P_11)"/></fo:block></fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="rrSummary">
        <fo:block text-align="right" font-size="8pt" space-before="3mm" space-after="5mm">
            <xsl:call-template name="summaryAmountLine">
                <xsl:with-param name="label" select="key('kLabels', 'rrTotalPurchaseValue', $labels)"/>
                <xsl:with-param name="amount" select="crd:FakturaRR/crd:P_11_1"/>
                <xsl:with-param name="currency" select="crd:FakturaRR/crd:KodWaluty"/>
            </xsl:call-template>
            <xsl:call-template name="summaryAmountLine">
                <xsl:with-param name="label" select="key('kLabels', 'rrTotalRefundAmount', $labels)"/>
                <xsl:with-param name="amount" select="crd:FakturaRR/crd:P_11_2"/>
                <xsl:with-param name="currency" select="crd:FakturaRR/crd:KodWaluty"/>
            </xsl:call-template>
            <xsl:call-template name="summaryAmountLine">
                <xsl:with-param name="label" select="key('kLabels', 'totalAmount', $labels)"/>
                <xsl:with-param name="amount" select="crd:FakturaRR/crd:P_12_1"/>
                <xsl:with-param name="currency" select="crd:FakturaRR/crd:KodWaluty"/>
            </xsl:call-template>
            <xsl:if test="crd:FakturaRR/crd:P_12_2">
                <fo:block space-after="1mm">
                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'amountInWords', $labels)"/>: </fo:inline>
                    <xsl:value-of select="crd:FakturaRR/crd:P_12_2"/>
                </fo:block>
            </xsl:if>
        </fo:block>
    </xsl:template>

    <xsl:template name="rrPayment">
        <xsl:if test="crd:FakturaRR/crd:Platnosc">
            <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
            <fo:block font-size="12pt" font-weight="bold" text-align="left" space-after="2mm">
                <xsl:value-of select="key('kLabels', 'payment', $labels)"/>
            </fo:block>
            <xsl:choose>
                <xsl:when test="crd:FakturaRR/crd:Platnosc/crd:FormaPlatnosci">
                    <xsl:call-template name="detailLine">
                        <xsl:with-param name="label" select="key('kLabels', 'paymentMethod', $labels)"/>
                        <xsl:with-param name="value" select="key('kLabels', 'paymentMethod.transfer', $labels)"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="crd:FakturaRR/crd:Platnosc/crd:PlatnoscInna = 1">
                    <xsl:call-template name="detailLine">
                        <xsl:with-param name="label" select="key('kLabels', 'paymentMethod', $labels)"/>
                        <xsl:with-param name="value" select="crd:FakturaRR/crd:Platnosc/crd:OpisPlatnosci"/>
                    </xsl:call-template>
                </xsl:when>
            </xsl:choose>
            <xsl:call-template name="detailLine">
                <xsl:with-param name="label" select="key('kLabels', 'paymentIdentifier', $labels)"/>
                <xsl:with-param name="value" select="crd:FakturaRR/crd:Platnosc/crd:IPKSeF"/>
            </xsl:call-template>
            <xsl:call-template name="detailLine">
                <xsl:with-param name="label" select="key('kLabels', 'paymentLink', $labels)"/>
                <xsl:with-param name="value" select="crd:FakturaRR/crd:Platnosc/crd:LinkDoPlatnosci"/>
            </xsl:call-template>
            <xsl:call-template name="renderPaymentAccounts">
                <xsl:with-param name="farmerAccounts" select="crd:FakturaRR/crd:Platnosc/crd:RachunekBankowy1"/>
                <xsl:with-param name="buyerAccounts" select="crd:FakturaRR/crd:Platnosc/crd:RachunekBankowy2"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="renderPaymentAccounts">
        <xsl:param name="farmerAccounts"/>
        <xsl:param name="buyerAccounts"/>
        <xsl:if test="$farmerAccounts or $buyerAccounts">
            <fo:block space-before="3mm" font-size="7pt">
                <fo:inline-container inline-progression-dimension="48%" padding-right="4pt" alignment-baseline="before-edge">
                    <fo:block font-size="9pt" font-weight="bold" space-after="1mm">
                        <xsl:value-of select="key('kLabels', 'supplierBankAccounts', $labels)"/>
                    </fo:block>
                    <xsl:for-each select="$farmerAccounts">
                        <fo:block space-after="2mm">
                            <xsl:call-template name="renderBankAccountTable">
                                <xsl:with-param name="bankAccountNode" select="."/>
                            </xsl:call-template>
                        </fo:block>
                    </xsl:for-each>
                </fo:inline-container>
                <fo:inline-container inline-progression-dimension="48%" padding-left="4pt" alignment-baseline="before-edge">
                    <fo:block font-size="9pt" font-weight="bold" space-after="1mm">
                        <xsl:value-of select="key('kLabels', 'buyerBankAccounts', $labels)"/>
                    </fo:block>
                    <xsl:for-each select="$buyerAccounts">
                        <fo:block space-after="2mm">
                            <xsl:call-template name="renderBankAccountTable">
                                <xsl:with-param name="bankAccountNode" select="."/>
                            </xsl:call-template>
                        </fo:block>
                    </xsl:for-each>
                </fo:inline-container>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template name="paymentDocuments">
        <xsl:if test="crd:FakturaRR/crd:DokumentZaplaty">
            <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
            <fo:block font-size="12pt" font-weight="bold" text-align="left" space-after="2mm">
                <xsl:value-of select="key('kLabels', 'paymentDocuments', $labels)"/>
            </fo:block>
            <fo:table table-layout="fixed" width="70%" border-collapse="separate">
                <fo:table-column column-width="60%"/>
                <fo:table-column column-width="40%"/>
                <fo:table-header>
                    <fo:table-row background-color="#f5f5f5" font-weight="bold">
                        <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'documentNumber', $labels)"/></xsl:call-template>
                        <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'documentDate', $labels)"/></xsl:call-template>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                    <xsl:for-each select="crd:FakturaRR/crd:DokumentZaplaty">
                        <fo:table-row>
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:NrDokumentu"/></fo:block></fo:table-cell>
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:DataDokumentu"/></fo:block></fo:table-cell>
                        </fo:table-row>
                    </xsl:for-each>
                </fo:table-body>
            </fo:table>
        </xsl:if>
    </xsl:template>

    <xsl:template name="additionalDescription">
        <xsl:if test="crd:FakturaRR/crd:DodatkowyOpis">
            <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
            <fo:block font-weight="bold" space-after="2mm"><xsl:value-of select="key('kLabels', 'additionalDescription', $labels)"/></fo:block>
            <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                <xsl:variable name="hasNrWiersza" select="boolean(crd:FakturaRR/crd:DodatkowyOpis/crd:NrWiersza[normalize-space()])"/>
                <xsl:choose>
                    <xsl:when test="$hasNrWiersza">
                        <fo:table-column column-width="10%"/>
                        <fo:table-column column-width="45%"/>
                        <fo:table-column column-width="45%"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <fo:table-column column-width="50%"/>
                        <fo:table-column column-width="50%"/>
                    </xsl:otherwise>
                </xsl:choose>
                <fo:table-header>
                    <fo:table-row background-color="#f5f5f5" font-weight="bold">
                        <xsl:if test="$hasNrWiersza">
                            <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'rowNumber', $labels)"/></xsl:call-template>
                        </xsl:if>
                        <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'infoType', $labels)"/></xsl:call-template>
                        <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'infoContent', $labels)"/></xsl:call-template>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                    <xsl:for-each select="crd:FakturaRR/crd:DodatkowyOpis">
                        <fo:table-row>
                            <xsl:if test="$hasNrWiersza">
                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:NrWiersza"/></fo:block></fo:table-cell>
                            </xsl:if>
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:Klucz"/></fo:block></fo:table-cell>
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:Wartosc"/></fo:block></fo:table-cell>
                        </fo:table-row>
                    </xsl:for-each>
                </fo:table-body>
            </fo:table>
        </xsl:if>
    </xsl:template>

    <xsl:template name="renderCorrectedInvoiceData">
        <xsl:if test="crd:FakturaRR/crd:DaneFaKorygowanej">
            <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
            <fo:block font-size="12pt" font-weight="bold" text-align="left" space-after="3mm">
                <xsl:value-of select="key('kLabels', 'correctedInvoice.data', $labels)"/>
            </fo:block>
            <xsl:for-each select="crd:FakturaRR/crd:DaneFaKorygowanej">
                <xsl:call-template name="detailLine">
                    <xsl:with-param name="label" select="key('kLabels', 'correctedInvoice.issueDate', $labels)"/>
                    <xsl:with-param name="value" select="crd:DataWystFaKorygowanej"/>
                </xsl:call-template>
                <xsl:call-template name="detailLine">
                    <xsl:with-param name="label" select="key('kLabels', 'correctedInvoiceNumber', $labels)"/>
                    <xsl:with-param name="value" select="crd:NrFaKorygowanej"/>
                </xsl:call-template>
                <xsl:call-template name="detailLine">
                    <xsl:with-param name="label" select="key('kLabels', 'correctedInvoice.ksefNumber', $labels)"/>
                    <xsl:with-param name="value" select="crd:NrKSeFFaKorygowanej"/>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:template name="renderCorrectedParty">
        <xsl:param name="title"/>
        <xsl:param name="oldParty"/>
        <xsl:param name="newParty"/>
        <xsl:if test="$oldParty">
            <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
            <fo:block font-size="12pt" font-weight="bold" text-align="left" space-after="2mm">
                <xsl:value-of select="$title"/>
            </fo:block>
            <fo:table font-size="7pt" table-layout="fixed" width="100%">
                <fo:table-column column-width="50%"/>
                <fo:table-column column-width="50%"/>
                <fo:table-body>
                    <fo:table-row>
                        <fo:table-cell padding-right="4pt">
                            <fo:block font-size="9pt" font-weight="bold" space-after="2mm"><xsl:value-of select="key('kLabels', 'contentCorrected', $labels)"/></fo:block>
                            <xsl:apply-templates select="$oldParty" mode="partyBody"/>
                        </fo:table-cell>
                        <fo:table-cell padding-left="4pt">
                            <fo:block font-size="9pt" font-weight="bold" space-after="2mm"><xsl:value-of select="key('kLabels', 'contentCorrecting', $labels)"/></fo:block>
                            <xsl:apply-templates select="$newParty" mode="partyBody"/>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </xsl:if>
    </xsl:template>

    <xsl:template match="crd:Podmiot1 | crd:Podmiot2">
        <fo:table font-size="7pt" table-layout="fixed" width="100%">
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell>
                        <xsl:apply-templates select="." mode="partyBody"/>
                    </fo:table-cell>
                </fo:table-row>
                <xsl:if test="self::crd:Podmiot1/crd:NrKontrahenta or self::crd:Podmiot2/crd:StatusInfoPodatnika">
                    <fo:table-row>
                        <fo:table-cell padding-top="8px">
                            <xsl:call-template name="detailLine">
                                <xsl:with-param name="label" select="key('kLabels', 'customer.number', $labels)"/>
                                <xsl:with-param name="value" select="crd:NrKontrahenta"/>
                            </xsl:call-template>
                            <xsl:call-template name="statusInfoLine">
                                <xsl:with-param name="status" select="crd:StatusInfoPodatnika"/>
                            </xsl:call-template>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="crd:Podmiot1 | crd:Podmiot2 | crd:Podmiot1K | crd:Podmiot2K" mode="partyBody">
        <xsl:variable name="id" select="crd:DaneIdentyfikacyjne"/>
        <fo:block text-align="left" padding-bottom="3px">
            <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'nip', $labels)"/>: </fo:inline>
            <xsl:value-of select="$id/crd:NIP"/>
        </fo:block>
        <fo:block text-align="left" padding-bottom="3px">
            <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'name', $labels)"/>: </fo:inline>
            <xsl:value-of select="$id/crd:Nazwa"/>
        </fo:block>
        <xsl:apply-templates select="crd:Adres" mode="blocks"/>
        <xsl:apply-templates select="crd:AdresKoresp" mode="blocks"/>
        <xsl:call-template name="contactData"/>
    </xsl:template>

    <xsl:template match="crd:Podmiot3">
        <xsl:if test="crd:Rola != '5'">
            <fo:block font-weight="bold" font-size="12pt" text-align="left" padding-bottom="4px" padding-top="5mm">
                <xsl:choose>
                    <xsl:when test="crd:Rola = '1'"><xsl:value-of select="key('kLabels', 'role.factor', $labels)"/></xsl:when>
                    <xsl:when test="crd:Rola = '2'"><xsl:value-of select="key('kLabels', 'role.recipient', $labels)"/></xsl:when>
                    <xsl:when test="crd:Rola = '3'"><xsl:value-of select="key('kLabels', 'role.originalEntity', $labels)"/></xsl:when>
                    <xsl:when test="crd:Rola = '6'"><xsl:value-of select="key('kLabels', 'role.payer', $labels)"/></xsl:when>
                    <xsl:when test="crd:Rola = '7'"><xsl:value-of select="key('kLabels', 'role.localGovIssuer', $labels)"/></xsl:when>
                    <xsl:when test="crd:Rola = '8'"><xsl:value-of select="key('kLabels', 'role.localGovRecipient', $labels)"/></xsl:when>
                    <xsl:otherwise><xsl:value-of select="crd:OpisRoli"/></xsl:otherwise>
                </xsl:choose>
            </fo:block>
        </xsl:if>
        <xsl:if test="crd:RolaInna">
            <fo:block font-weight="bold" font-size="12pt" text-align="left" padding-bottom="8px" padding-top="5mm">
                <xsl:value-of select="crd:OpisRoli"/>
            </fo:block>
        </xsl:if>
        <xsl:variable name="id" select="crd:DaneIdentyfikacyjne"/>
        <xsl:if test="$id/crd:NIP">
            <fo:block text-align="left" padding-bottom="3px"><fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'nip', $labels)"/>: </fo:inline><xsl:value-of select="$id/crd:NIP"/></fo:block>
        </xsl:if>
        <xsl:if test="$id/crd:IDWew">
            <fo:block text-align="left" padding-bottom="3px"><fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'idWew', $labels)"/>: </fo:inline><xsl:value-of select="$id/crd:IDWew"/></fo:block>
        </xsl:if>
        <xsl:if test="$id/crd:BrakID = 1">
            <fo:block text-align="left" padding-bottom="3px"><xsl:value-of select="key('kLabels', 'podmiot3.brakIdentyfikatora', $labels)"/></fo:block>
        </xsl:if>
        <fo:block text-align="left" padding-bottom="3px"><fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'name', $labels)"/>: </fo:inline><xsl:value-of select="$id/crd:Nazwa"/></fo:block>
        <xsl:apply-templates select="crd:Adres" mode="blocks"/>
        <xsl:apply-templates select="crd:AdresKoresp" mode="blocks"/>
        <xsl:call-template name="contactData"/>
    </xsl:template>

    <xsl:template name="contactData">
        <xsl:if test="crd:DaneKontaktowe/crd:Email or crd:DaneKontaktowe/crd:Telefon">
            <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'contact.data', $labels)"/></fo:inline>
            </fo:block>
            <xsl:for-each select="crd:DaneKontaktowe">
                <xsl:call-template name="detailLine">
                    <xsl:with-param name="label" select="key('kLabels', 'email', $labels)"/>
                    <xsl:with-param name="value" select="crd:Email"/>
                </xsl:call-template>
                <xsl:call-template name="detailLine">
                    <xsl:with-param name="label" select="key('kLabels', 'phone', $labels)"/>
                    <xsl:with-param name="value" select="crd:Telefon"/>
                </xsl:call-template>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:template match="crd:Rozliczenie">
        <xsl:if test="*">
            <fo:block id="Rozliczenie" font-size="7pt">
                <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
                <fo:block font-size="12pt" font-weight="bold" text-align="left" space-after="2mm">
                    <xsl:value-of select="key('kLabels', 'settlement', $labels)"/>
                </fo:block>
                <xsl:if test="crd:Obciazenia">
                    <fo:block font-weight="bold" space-after="1mm"><xsl:value-of select="key('kLabels', 'settlement.charges', $labels)"/>:</fo:block>
                    <xsl:call-template name="settlementTable">
                        <xsl:with-param name="rows" select="crd:Obciazenia"/>
                        <xsl:with-param name="reasonLabel" select="key('kLabels', 'settlement.chargeReason', $labels)"/>
                        <xsl:with-param name="amountLabel" select="key('kLabels', 'settlement.amount', $labels)"/>
                    </xsl:call-template>
                    <xsl:apply-templates select="crd:SumaObciazen"/>
                </xsl:if>
                <xsl:if test="crd:Odliczenia">
                    <fo:block font-weight="bold" space-after="1mm"><xsl:value-of select="key('kLabels', 'settlement.deductions', $labels)"/>:</fo:block>
                    <xsl:call-template name="settlementTable">
                        <xsl:with-param name="rows" select="crd:Odliczenia"/>
                        <xsl:with-param name="reasonLabel" select="key('kLabels', 'settlement.deductionReason', $labels)"/>
                        <xsl:with-param name="amountLabel" select="key('kLabels', 'settlement.deductionAmount', $labels)"/>
                    </xsl:call-template>
                    <xsl:apply-templates select="crd:SumaOdliczen"/>
                </xsl:if>
                <xsl:apply-templates select="crd:DoZaplaty"/>
                <xsl:apply-templates select="crd:DoRozliczenia"/>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template name="settlementTable">
        <xsl:param name="rows"/>
        <xsl:param name="reasonLabel"/>
        <xsl:param name="amountLabel"/>
        <fo:table table-layout="fixed" width="100%" space-after="2mm">
            <fo:table-column column-width="70%"/>
            <fo:table-column column-width="30%"/>
            <fo:table-header>
                <fo:table-row background-color="#f0f0f0">
                    <xsl:call-template name="headerCell"><xsl:with-param name="text" select="$reasonLabel"/></xsl:call-template>
                    <xsl:call-template name="headerCell"><xsl:with-param name="text" select="$amountLabel"/></xsl:call-template>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
                <xsl:apply-templates select="$rows"/>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="crd:Obciazenia | crd:Odliczenia">
        <fo:table-row>
            <fo:table-cell xsl:use-attribute-sets="tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:Powod"/></fo:block></fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableBorder table.cell.padding" text-align="right"><fo:block><xsl:value-of select="local:format-amount(crd:Kwota)"/></fo:block></fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="crd:SumaObciazen">
        <fo:block text-align="right" space-after="2mm">
            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'settlement.sumCharges', $labels)"/>: </fo:inline>
            <xsl:value-of select="local:format-amount(.)"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="crd:SumaOdliczen">
        <fo:block text-align="right" space-after="2mm">
            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'settlement.sumDeductions', $labels)"/>: </fo:inline>
            <xsl:value-of select="local:format-amount(.)"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="crd:DoZaplaty">
        <fo:block font-size="8pt" text-align="right" space-after="1mm" font-weight="bold">
            <xsl:value-of select="key('kLabels', 'settlement.amountToPay', $labels)"/>:
            <xsl:text> </xsl:text>
            <xsl:value-of select="local:format-amount(.)"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="//crd:KodWaluty"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="crd:DoRozliczenia">
        <fo:block font-size="8pt" text-align="right" space-after="1mm" font-weight="bold">
            <xsl:value-of select="key('kLabels', 'settlement.amountToSettle', $labels)"/>:
            <xsl:text> </xsl:text>
            <xsl:value-of select="local:format-amount(.)"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="//crd:KodWaluty"/>
        </fo:block>
    </xsl:template>

    <xsl:template name="registries">
        <xsl:if test="crd:Stopka/crd:Rejestry">
            <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
            <fo:block font-weight="bold" space-after="3mm"><xsl:value-of select="key('kLabels', 'registries', $labels)"/></fo:block>
            <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                <fo:table-column column-width="55%"/>
                <fo:table-column column-width="15%"/>
                <fo:table-column column-width="15%"/>
                <fo:table-column column-width="15%"/>
                <fo:table-header>
                    <fo:table-row background-color="#f5f5f5" font-weight="bold">
                        <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'fullName', $labels)"/></xsl:call-template>
                        <xsl:call-template name="headerCell"><xsl:with-param name="text" select="'KRS'"/></xsl:call-template>
                        <xsl:call-template name="headerCell"><xsl:with-param name="text" select="'REGON'"/></xsl:call-template>
                        <xsl:call-template name="headerCell"><xsl:with-param name="text" select="'BDO'"/></xsl:call-template>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                    <xsl:for-each select="crd:Stopka/crd:Rejestry">
                        <fo:table-row>
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:PelnaNazwa"/></fo:block></fo:table-cell>
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:KRS"/></fo:block></fo:table-cell>
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:REGON"/></fo:block></fo:table-cell>
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:BDO"/></fo:block></fo:table-cell>
                        </fo:table-row>
                    </xsl:for-each>
                </fo:table-body>
            </fo:table>
        </xsl:if>
    </xsl:template>

    <xsl:template name="footerInfo">
        <xsl:if test="crd:Stopka/crd:Informacje">
            <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
            <fo:block font-weight="bold" space-after="3mm"><xsl:value-of select="key('kLabels', 'otherInfo', $labels)"/></fo:block>
            <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                <fo:table-column column-width="100%"/>
                <fo:table-header>
                    <fo:table-row background-color="#f5f5f5" font-weight="bold">
                        <xsl:call-template name="headerCell"><xsl:with-param name="text" select="key('kLabels', 'invoiceFooter', $labels)"/></xsl:call-template>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                    <xsl:for-each select="crd:Stopka/crd:Informacje">
                        <fo:table-row>
                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"><fo:block><xsl:value-of select="crd:StopkaFaktury"/></fo:block></fo:table-cell>
                        </fo:table-row>
                    </xsl:for-each>
                </fo:table-body>
            </fo:table>
        </xsl:if>
    </xsl:template>

    <xsl:template name="renderBankAccountTable">
        <xsl:param name="bankAccountNode"/>
        <fo:table table-layout="fixed" width="100%" border-collapse="separate" padding-top="2mm">
            <fo:table-column column-width="45mm"/>
            <fo:table-column column-width="45mm"/>
            <fo:table-body>
                <xsl:call-template name="textRow"><xsl:with-param name="label" select="key('kLabels', 'bankAccountNumber', $labels)"/><xsl:with-param name="value" select="$bankAccountNode/crd:NrRB"/></xsl:call-template>
                <xsl:call-template name="textRow"><xsl:with-param name="label" select="key('kLabels', 'swift', $labels)"/><xsl:with-param name="value" select="$bankAccountNode/crd:SWIFT"/></xsl:call-template>
                <xsl:call-template name="textRow"><xsl:with-param name="label" select="key('kLabels', 'bankName', $labels)"/><xsl:with-param name="value" select="$bankAccountNode/crd:NazwaBanku"/></xsl:call-template>
                <xsl:call-template name="textRow"><xsl:with-param name="label" select="key('kLabels', 'accountDescriptionLabel', $labels)"/><xsl:with-param name="value" select="$bankAccountNode/crd:OpisRachunku"/></xsl:call-template>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template name="headerCell">
        <xsl:param name="text"/>
        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
            <fo:block><xsl:value-of select="$text"/></fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template name="textRow">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <xsl:if test="normalize-space(string($value)) != ''">
            <fo:table-row>
                <fo:table-cell background-color="#f5f5f5" font-weight="bold" xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                    <fo:block><xsl:value-of select="$label"/></fo:block>
                </fo:table-cell>
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                    <fo:block><xsl:value-of select="$value"/></fo:block>
                </fo:table-cell>
            </fo:table-row>
        </xsl:if>
    </xsl:template>

    <xsl:template name="summaryAmountLine">
        <xsl:param name="label"/>
        <xsl:param name="amount"/>
        <xsl:param name="currency"/>
        <xsl:if test="normalize-space(string($amount)) != ''">
            <fo:block space-after="1mm">
                <fo:inline font-weight="bold"><xsl:value-of select="$label"/>: </fo:inline>
                <xsl:value-of select="local:format-amount($amount)"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="$currency"/>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template name="detailLine">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <xsl:if test="normalize-space(string($value)) != ''">
            <fo:block font-size="8pt" text-align="left" space-after="1mm">
                <fo:inline font-weight="bold"><xsl:value-of select="$label"/>: </fo:inline>
                <xsl:value-of select="$value"/>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template name="inlineMeta">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <xsl:if test="normalize-space(string($value)) != ''">
            <fo:block font-size="6pt" color="#6c757d">
                <fo:inline font-weight="600"><xsl:value-of select="$label"/>: </fo:inline>
                <xsl:value-of select="$value"/>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template name="correctionTypeLine">
        <xsl:param name="type"/>
        <xsl:if test="normalize-space(string($type)) != ''">
            <fo:block font-size="8pt" text-align="left" space-after="1mm">
                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'correctionType', $labels)"/>: </fo:inline>
                <xsl:choose>
                    <xsl:when test="$type = 1"><xsl:value-of select="key('kLabels', 'correctionType.original', $labels)"/></xsl:when>
                    <xsl:when test="$type = 2"><xsl:value-of select="key('kLabels', 'correctionType.correcting', $labels)"/></xsl:when>
                    <xsl:when test="$type = 3"><xsl:value-of select="key('kLabels', 'correctionType.other', $labels)"/></xsl:when>
                    <xsl:when test="$type = 4"><xsl:value-of select="key('kLabels', 'correctionType.return', $labels)"/></xsl:when>
                </xsl:choose>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template name="statusInfoLine">
        <xsl:param name="status"/>
        <xsl:if test="normalize-space(string($status)) != ''">
            <fo:block font-size="8pt" text-align="left" space-after="1mm">
                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'statusInfoPodatnika', $labels)"/>: </fo:inline>
                <xsl:choose>
                    <xsl:when test="$status = 1"><xsl:value-of select="key('kLabels', 'statusInfoPodatnika.liquidation', $labels)"/></xsl:when>
                    <xsl:when test="$status = 2"><xsl:value-of select="key('kLabels', 'statusInfoPodatnika.restructuring', $labels)"/></xsl:when>
                    <xsl:when test="$status = 3"><xsl:value-of select="key('kLabels', 'statusInfoPodatnika.bankruptcy', $labels)"/></xsl:when>
                    <xsl:when test="$status = 4"><xsl:value-of select="key('kLabels', 'statusInfoPodatnika.inheritance', $labels)"/></xsl:when>
                </xsl:choose>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template name="mapKodKrajuToNazwa">
        <xsl:param name="kodKraju"/>
        <xsl:variable name="enumeration" select="$kodyKrajowXSD//xsd:enumeration[@value=$kodKraju]"/>
        <xsl:variable name="nazwaKraju" select="$enumeration/xsd:annotation/xsd:documentation/text()"/>
        <xsl:choose>
            <xsl:when test="$nazwaKraju != ''"><xsl:value-of select="$nazwaKraju"/></xsl:when>
            <xsl:otherwise><xsl:value-of select="$kodKraju"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="renderAddressAsBlocks">
        <xsl:param name="label"/>
        <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
            <fo:inline font-weight="bold"><xsl:value-of select="$label"/></fo:inline>
        </fo:block>
        <fo:block text-align="left">
            <xsl:value-of select="crd:AdresL1"/>
            <xsl:apply-templates select="crd:AdresL2"/>
            <xsl:apply-templates select="crd:KodKraju"/>
            <xsl:apply-templates select="crd:GLN"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="crd:AdresL2">
        <fo:inline>, </fo:inline>
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="crd:KodKraju">
        <fo:block>
            <xsl:call-template name="mapKodKrajuToNazwa">
                <xsl:with-param name="kodKraju" select="."/>
            </xsl:call-template>
        </fo:block>
    </xsl:template>

    <xsl:template match="crd:GLN">
        <fo:block>
            <fo:inline font-weight="600">GLN: </fo:inline>
            <xsl:value-of select="."/>
        </fo:block>
    </xsl:template>

    <xsl:template match="crd:Adres" mode="blocks">
        <xsl:call-template name="renderAddressAsBlocks">
            <xsl:with-param name="label" select="key('kLabels', 'address', $labels)"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="crd:AdresKoresp" mode="blocks">
        <xsl:call-template name="renderAddressAsBlocks">
            <xsl:with-param name="label" select="key('kLabels', 'correspondenceAddress', $labels)"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="renderQrCode">
        <xsl:param name="qrCodeImage"/>
        <xsl:param name="qrCodeLabel"/>
        <xsl:param name="qrCodeVerificationLink"/>
        <xsl:param name="qrCodeVerificationLinkTitle"/>
        <xsl:if test="$qrCodeImage and $qrCodeVerificationLink">
            <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
            <fo:block keep-together.within-page="always">
                <fo:table table-layout="fixed" width="100%">
                    <fo:table-column column-width="35%"/>
                    <fo:table-column column-width="65%"/>
                    <fo:table-body>
                        <fo:table-row>
                            <fo:table-cell display-align="center" height="auto" font-size="7pt">
                                <fo:block text-align="center" font-weight="600">
                                    <fo:external-graphic content-width="170pt" content-height="170pt" src="url('data:image/png;base64,{$qrCodeImage}')"/>
                                </fo:block>
                                <xsl:if test="$qrCodeLabel">
                                    <fo:block text-align="center" font-weight="600">
                                        <xsl:value-of select="$qrCodeLabel"/>
                                    </fo:block>
                                </xsl:if>
                            </fo:table-cell>
                            <fo:table-cell display-align="center" height="auto">
                                <fo:block font-size="7pt" display-align="center">
                                    <fo:block font-weight="600" space-after="2mm">
                                        <xsl:value-of select="$qrCodeVerificationLinkTitle"/>
                                    </fo:block>
                                    <fo:block display-align="center" space-after="2mm">
                                        <fo:basic-link external-destination="{$qrCodeVerificationLink}" color="blue">
                                            <xsl:value-of select="$qrCodeVerificationLink"/>
                                        </fo:basic-link>
                                    </fo:block>
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </fo:table-body>
                </fo:table>
            </fo:block>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
