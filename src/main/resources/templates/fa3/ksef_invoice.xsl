<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:crd="http://crd.gov.pl/wzor/2025/06/25/13775/">
    <!-- Autor: Karol Bryzgiel (karol.bryzgiel@soft-project.pl) -->

    <!-- Załadowanie schematu XSD jako dokument XML -->
    <xsl:variable name="kodyKrajowXSD" select="document('http://crd.gov.pl/xml/schematy/dziedzinowe/mf/2022/01/05/eD/DefinicjeTypy/KodyKrajow_v10-0E.xsd')"/>

    <!-- Szablon do mapowania kodu kraju na nazwę -->
    <xsl:template name="mapKodKrajuToNazwa">
        <xsl:param name="kodKraju"/>

        <!-- Szukanie elementu enumeration z odpowiednim value -->
        <xsl:variable name="enumeration" select="$kodyKrajowXSD//xsd:enumeration[@value=$kodKraju]"/>

        <!-- Pobieranie tekstu z elementu documentation -->
        <xsl:variable name="nazwaKraju" select="$enumeration/xsd:annotation/xsd:documentation/text()"/>

        <xsl:choose>
            <xsl:when test="$nazwaKraju != ''">
                <xsl:value-of select="$nazwaKraju"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$kodKraju"/> <!-- Zwraca kod, jeśli nie znaleziono mapowania -->
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- Template for rendering a single QR code -->
    <xsl:template name="renderQrCode">
        <xsl:param name="qrCodeImage"/>
        <xsl:param name="qrCodeLabel"/>
        <xsl:param name="qrCodeVerificationLink"/>
        <xsl:param name="qrCodeVerificationLinkTitle"/>

        <xsl:if test="$qrCodeImage and $qrCodeVerificationLink">
            <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
            
            <fo:block keep-together.within-page="always">
                <fo:table width="100%">
                    <fo:table-column column-width="35%"/>
                    <fo:table-column column-width="65%"/>
                    <fo:table-body>
                        <fo:table-row>
                            <!-- Komórka z obrazkiem QR -->
                            <fo:table-cell display-align="center" height="auto" font-size="7pt">
                                <fo:block text-align="center" font-weight="600">
                                    <fo:external-graphic
                                            content-width="170pt"
                                            content-height="170pt"
                                            src="url('data:image/png;base64,{$qrCodeImage}')"/>
                                </fo:block>
                                <xsl:if test="$qrCodeLabel">
                                    <fo:block text-align="center" font-weight="600">
                                        <xsl:value-of select="$qrCodeLabel"/>
                                    </fo:block>
                                </xsl:if>
                            </fo:table-cell>
                            <!-- Komórka z tekstem -->
                            <fo:table-cell display-align="center" height="auto">
                                <fo:block font-size="7pt" display-align="center">
                                    <fo:block font-weight="600" space-after="2mm">
                                        <xsl:value-of select="$qrCodeVerificationLinkTitle"/>

                                    </fo:block>
                                    <fo:block display-align="center" space-after="2mm">
                                        <fo:basic-link
                                                external-destination="{$qrCodeVerificationLink}" color="blue">
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
    
    <xsl:include href="invoice-rows.xsl"/>
    <xsl:include href="order-invoice-rows.xsl"/>

    <!--  Additional parameters that are not included in the xml invoice -->
    <xsl:param name="nrKsef"/>
    <xsl:param name="qrCode"/>
    <xsl:param name="verificationLink"/>
    <xsl:param name="logo"/>
    <xsl:param name="showFooter"/>
    <xsl:param name="duplicateDate"/>
    <xsl:param name="currencyDate"/>
    <xsl:param name="issuerUser"/>
    <xsl:param name="showCorrectionDifferences"/>

    <xsl:param name="labels"/>
    <xsl:key name="kLabels" match="entry" use="@key"/>

    <!-- New parameters for multiple QR codes -->
    <xsl:param name="qrCodesCount" select="0"/>

    <!-- QR Code 0 parameters -->
    <xsl:param name="qrCode0"/>
    <xsl:param name="qrCodeLabel0"/>
    <xsl:param name="verificationLink0"/>
    <xsl:param name="verificationLinkTitle0"/>

    <!-- QR Code 1 parameters -->
    <xsl:param name="qrCode1"/>
    <xsl:param name="qrCodeLabel1"/>
    <xsl:param name="verificationLink1"/>
    <xsl:param name="verificationLinkTitle1"/>

    <!-- Attribute used for table border -->
    <xsl:attribute-set name="tableBorder">
        <xsl:attribute name="border">solid 0.2mm black</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="tableBorderTop">
        <xsl:attribute name="border-top">solid 0.2mm black</xsl:attribute>
        <xsl:attribute name="border-left">solid 0.2mm black</xsl:attribute>
        <xsl:attribute name="border-right">solid 0.2mm black</xsl:attribute>
    </xsl:attribute-set>

    <!-- Attribute used for table fonts -->
    <xsl:attribute-set name="tableHeaderFont">
        <xsl:attribute name="font-size">7</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="tableFont">
        <xsl:attribute name="font-size">7</xsl:attribute>
    </xsl:attribute-set>

    <!-- Attribute used for table padding -->
    <xsl:attribute-set name="table.cell.padding">
        <xsl:attribute name="padding-left">4pt</xsl:attribute>
        <xsl:attribute name="padding-right">4pt</xsl:attribute>
        <xsl:attribute name="padding-top">4pt</xsl:attribute>
        <xsl:attribute name="padding-bottom">4pt</xsl:attribute>
    </xsl:attribute-set>

    <!-- Faktura -->
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

                <!-- Stopka -->
                <xsl:if test="$showFooter">
                    <fo:static-content flow-name="xsl-region-after">
                        <!-- Linia oddzielająca -->
                        <fo:block border-bottom="solid 1px black" padding-bottom="2mm" space-after="2mm"/>
                        <!-- System, z którego wytworzono fakture -->
                        <fo:block font-size="8pt" text-align="left">
                            <xsl:value-of select="key('kLabels', 'generatedIn', $labels)"/>:
                            <xsl:value-of select="crd:Naglowek/crd:SystemInfo"/>
                        </fo:block>
                    </fo:static-content>
                </xsl:if>
                <!-- Faktura -->
                <fo:flow flow-name="xsl-region-body" color="#343a40">
                    <!-- Tytuł strony -->
                    <fo:block font-size="20pt" font-weight="bold" text-align="left">
                        <xsl:if test="$logo != ''">
                            <fo:external-graphic
                                    content-width="80pt"
                                    content-height="80pt"
                                    src="url('data:image/png;base64,{$logo}')"/>
                        </xsl:if>
                    </fo:block>
                    <!-- Numer faktury -->
                    <fo:block font-size="9pt" text-align="right" padding-top="-5mm">
                        <xsl:value-of select="key('kLabels', 'invoice.number', $labels)"/>
                    </fo:block>
                    <fo:block font-size="16pt" text-align="right" space-after="2mm">
                        <fo:inline font-weight="bold">
                            <xsl:value-of select="crd:Fa/crd:P_2"/>
                        </fo:inline>
                    </fo:block>
                    <xsl:if test="$duplicateDate">
                        <fo:block font-size="10pt" color="grey" text-align="right" space-after="2mm">
                            <xsl:value-of select="key('kLabels', 'duplicate.fromDate', $labels)"/>
                            <xsl:text> </xsl:text>
                            <xsl:value-of select="$duplicateDate"/>
                        </fo:block>
                    </xsl:if>

                    <!-- Typ faktury -->
                    <fo:block font-size="9pt" text-align="right" space-after="2mm">
                        <xsl:choose>
                            <xsl:when test="crd:Fa/crd:RodzajFaktury = 'VAT'">
                                <xsl:value-of select="key('kLabels', 'basic.invoice', $labels)"/>
                            </xsl:when>
                            <xsl:when test="crd:Fa/crd:RodzajFaktury = 'ZAL'">
                                <xsl:value-of select="key('kLabels', 'invoice.advance', $labels)"/>
                            </xsl:when>
                            <xsl:when test="crd:Fa/crd:OkresFaKorygowanej">
                                <xsl:value-of select="key('kLabels', 'invoice.correctionBulk', $labels)"/>
                            </xsl:when>
                            <xsl:when test="crd:Fa/crd:RodzajFaktury = 'KOR'">
                                <xsl:value-of select="key('kLabels', 'invoice.correction', $labels)"/>
                            </xsl:when>
                            <xsl:when test="crd:Fa/crd:RodzajFaktury = 'ROZ'">
                                <xsl:value-of select="key('kLabels', 'invoice.settlement', $labels)"/>
                            </xsl:when>
                            <xsl:when test="crd:Fa/crd:RodzajFaktury = 'UPR'">
                                <xsl:value-of select="key('kLabels', 'invoice.simplified', $labels)"/>
                            </xsl:when>
                            <xsl:when test="crd:Fa/crd:RodzajFaktury = 'KOR_ZAL'">
                                <xsl:value-of select="key('kLabels', 'invoice.correctionAdvance', $labels)"/>
                            </xsl:when>
                            <xsl:when test="crd:Fa/crd:RodzajFaktury = 'KOR_ROZ'">
                                <xsl:value-of select="key('kLabels', 'invoice.correctionSettlement', $labels)"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="key('kLabels', 'invoice.unknownType', $labels)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>
                    <!-- Numer KSeF-->
                    <xsl:if test="$nrKsef">
                        <fo:block font-size="9pt" text-align="right" space-after="5mm">
                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'ksef.number', $labels)"/>:</fo:inline>
                            <fo:inline>
                                <xsl:value-of select="$nrKsef"/>
                            </fo:inline>
                        </fo:block>
                    </xsl:if>

                    <!-- Dane faktury korygowanej -->
                    <xsl:if test="crd:Fa/crd:DaneFaKorygowanej">
                        <!-- Linia oddzielająca -->
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                        <fo:block font-size="12pt" text-align="left" space-after="5mm">
                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'correctedInvoice.data', $labels)"/></fo:inline>
                        </fo:block>
                        <fo:table table-layout="fixed" width="100%" font-size="7pt" space-after="5mm">
                            <fo:table-column column-width="50%"/>
                            <fo:table-column column-width="50%"/>
                            <fo:table-body>
                                <!-- Iterujemy przez wszystkie elementy DaneFaKorygowanej, zaczynając od pierwszego elementu -->
                                <xsl:for-each select="crd:Fa/crd:DaneFaKorygowanej[position() mod 2 = 1]">
                                    <fo:table-row>
                                        <!-- Pierwsza komórka w wierszu -->
                                        <fo:table-cell padding-right="5px" padding-bottom="8px" vertical-align="top">
                                            <fo:block>
                                                <xsl:call-template name="DaneFaKorygowanejTemplate">
                                                    <xsl:with-param name="faktura" select="."/>
                                                    <xsl:with-param name="numer" select="position() * 2 - 1"/>
                                                    <xsl:with-param name="pokazNagłówek" select="count(../crd:DaneFaKorygowanej) > 1"/>
                                                </xsl:call-template>
                                            </fo:block>
                                        </fo:table-cell>

                                        <!-- Druga komórka, jeśli istnieje element na następnej pozycji -->
                                        <xsl:choose>
                                            <xsl:when test="following-sibling::crd:DaneFaKorygowanej[1]">
                                                <fo:table-cell padding-left="5px" vertical-align="top">
                                                    <fo:block>
                                                        <xsl:call-template name="DaneFaKorygowanejTemplate">
                                                            <xsl:with-param name="faktura" select="following-sibling::crd:DaneFaKorygowanej[1]"/>
                                                            <xsl:with-param name="numer" select="position() * 2"/>
                                                            <xsl:with-param name="pokazNagłówek" select="count(../crd:DaneFaKorygowanej) > 1"/>
                                                        </xsl:call-template>
                                                    </fo:block>
                                                </fo:table-cell>
                                            </xsl:when>
                                            <!-- Jeśli nie ma następnego elementu, dodajemy pustą komórkę -->
                                            <xsl:otherwise>
                                                <fo:table-cell>
                                                    <fo:block/>
                                                </fo:table-cell>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </fo:table-row>
                                </xsl:for-each>
                            </fo:table-body>
                        </fo:table>
                    </xsl:if>


                    <!-- Korekta danych sprzedawcy -->
                    <xsl:choose >
                        <xsl:when test="crd:Fa/crd:Podmiot1K" >
                            <!-- Linia oddzielająca -->
                            <fo:block border-bottom="solid 1px grey" space-after="5mm"/>

                            <fo:block font-size="12pt" text-align="left">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'seller', $labels)"/></fo:inline>
                            </fo:block>

                            <fo:block text-align="left" >
                                <xsl:if test="crd:Podmiot1/crd:NrEORI" >
                                    <fo:block font-size="7pt">
                                        <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
                                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'identification.data', $labels)"/>
                                            </fo:inline>
                                        </fo:block>
                                        <fo:inline font-weight="600" font-size="7pt"><xsl:value-of select="key('kLabels', 'eori.number', $labels)"/>: </fo:inline>
                                        <xsl:value-of select="crd:Podmiot1/crd:NrEORI"/>
                                    </fo:block>
                                </xsl:if>
                            </fo:block>
                            <xsl:if test="crd:Podmiot1/crd:DaneKontaktowe/crd:Email|crd:DaneKontaktowe/crd:Telefon">
                                <fo:block text-align="left" padding-bottom="3px" font-size="7pt" padding-top="8px">
                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'contact.data', $labels)"/>
                                    </fo:inline>
                                </fo:block>
                                <xsl:if test="crd:Podmiot1/crd:DaneKontaktowe/crd:Email">
                                    <fo:block text-align="left" font-size="7pt" padding-bottom="2px">
                                        <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'email', $labels)"/>: </fo:inline>
                                        <xsl:value-of
                                                select="crd:Podmiot1/crd:DaneKontaktowe/crd:Email"/>
                                    </fo:block>
                                </xsl:if>
                                <xsl:if test="crd:Podmiot1/crd:DaneKontaktowe/crd:Telefon">
                                    <fo:block text-align="left" font-size="7pt" padding-bottom="8px">
                                        <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'phone', $labels)"/>: </fo:inline>
                                        <xsl:value-of
                                                select="crd:Podmiot1/crd:DaneKontaktowe/crd:Telefon"/>
                                    </fo:block>
                                </xsl:if>
                            </xsl:if>

                            <fo:table font-size="7pt" table-layout="fixed" width="100%" padding-top="8px">
                                <fo:table-column column-width="33%"/>
                                <fo:table-column column-width="33%"/>
                                <fo:table-column column-width="33%"/>
                                <fo:table-body>
                                    <fo:table-row space-after="5mm">
                                        <fo:table-cell padding-bottom="8px">
                                            <fo:block font-size="9pt" text-align="left">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'contentCorrected', $labels)"/></fo:inline>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell padding-bottom="8px">
                                            <fo:block>

                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell padding-bottom="8px">
                                            <fo:block font-size="9pt" text-align="left">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'contentCorrecting', $labels)"/></fo:inline>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>

                                    <fo:table-row>
                                        <!-- Treść korygowana -->
                                        <fo:table-cell>
                                            <xsl:if test="crd:Fa/crd:Podmiot1K/crd:PrefiksPodatnika">
                                                <fo:block text-align="left" padding-bottom="3px">
                                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'vatUe.prefix', $labels)"/>: </fo:inline>
                                                    <xsl:call-template name="mapKodKrajuToNazwa">
                                                        <xsl:with-param name="kodKraju" select="crd:Fa/crd:Podmiot1K/crd:PrefiksPodatnika"/>
                                                    </xsl:call-template>
                                                </fo:block>
                                            </xsl:if>
                                            <fo:block text-align="left" padding-bottom="3px" font-size="7pt">
                                                <xsl:if test="crd:Fa/crd:Podmiot1K/crd:DaneIdentyfikacyjne/crd:NrVatUE">
                                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'vatUe.number', $labels)"/>: </fo:inline>
                                                    <xsl:value-of select="crd:Fa/crd:Podmiot1K/crd:DaneIdentyfikacyjne/crd:KodUE"/>
                                                    <xsl:value-of select="crd:Fa/crd:Podmiot1K/crd:DaneIdentyfikacyjne/crd:NrVatUE"/>
                                                </xsl:if>
                                                <xsl:if test="crd:Fa/crd:Podmiot1K/crd:DaneIdentyfikacyjne/crd:NIP">
                                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'nip', $labels)"/>: </fo:inline>
                                                    <xsl:value-of
                                                            select="crd:Fa/crd:Podmiot1K/crd:DaneIdentyfikacyjne/crd:NIP"/>
                                                </xsl:if>
                                            </fo:block>
                                            <fo:block text-align="left" padding-bottom="3px">
                                                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'name', $labels)"/>: </fo:inline>
                                                <xsl:value-of
                                                        select="crd:Fa/crd:Podmiot1K/crd:DaneIdentyfikacyjne/crd:Nazwa"/>
                                            </fo:block>
                                            <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'address', $labels)"/></fo:inline>
                                            </fo:block>
                                            <fo:block text-align="left">
                                                <xsl:value-of select="crd:Fa/crd:Podmiot1K/crd:Adres/crd:AdresL1"/>
                                                <xsl:if test="crd:Fa/crd:Podmiot1K/crd:Adres/crd:AdresL2">
                                                    <fo:inline>, </fo:inline>
                                                    <xsl:value-of select="crd:Fa/crd:Podmiot1K/crd:Adres/crd:AdresL2"/>
                                                </xsl:if>
                                                <xsl:if test="crd:Fa/crd:Podmiot1K/crd:Adres/crd:KodKraju">
                                                    <fo:block>
                                                        <xsl:call-template name="mapKodKrajuToNazwa">
                                                            <xsl:with-param name="kodKraju" select="crd:Fa/crd:Podmiot1K/crd:Adres/crd:KodKraju"/>
                                                        </xsl:call-template>
                                                    </fo:block>
                                                </xsl:if>
                                            </fo:block>
                                        </fo:table-cell>

                                        <fo:table-cell>
                                            <fo:block>

                                            </fo:block>
                                        </fo:table-cell>
                                        <!-- Treść korygująca -->
                                        <fo:table-cell>
                                            <xsl:if test="crd:Podmiot1/crd:PrefiksPodatnika">
                                                <fo:block text-align="left" padding-bottom="3px">
                                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'vatUe.prefix', $labels)"/>: </fo:inline>
                                                    <xsl:call-template name="mapKodKrajuToNazwa">
                                                        <xsl:with-param name="kodKraju" select="crd:Podmiot1/crd:PrefiksPodatnika"/>
                                                    </xsl:call-template>
                                                </fo:block>
                                            </xsl:if>
                                            <fo:block text-align="left" padding-bottom="3px" font-size="7pt">
                                                <xsl:if test="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:NrVatUE">
                                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'vatUe.number', $labels)"/>: </fo:inline>
                                                    <xsl:value-of select="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:KodUE"/>
                                                    <xsl:value-of select="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:NrVatUE"/>
                                                </xsl:if>
                                                <xsl:if test="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:NIP">
                                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'nip', $labels)"/>: </fo:inline>
                                                    <xsl:value-of
                                                            select="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:NIP"/>
                                                </xsl:if>
                                            </fo:block>
                                            <fo:block text-align="left" padding-bottom="3px">
                                                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'name', $labels)"/>: </fo:inline>
                                                <xsl:value-of
                                                        select="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:Nazwa"/>
                                            </fo:block>
                                            <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'address', $labels)"/></fo:inline>
                                            </fo:block>
                                            <fo:block text-align="left">
                                                <xsl:value-of select="crd:Podmiot1/crd:Adres/crd:AdresL1"/>
                                                <xsl:if test="crd:Podmiot1/crd:Adres/crd:AdresL2">
                                                    <fo:inline>, </fo:inline>
                                                    <xsl:value-of select="crd:Podmiot1/crd:Adres/crd:AdresL2"/>
                                                </xsl:if>
                                                <xsl:if test="crd:Podmiot1/crd:Adres/crd:KodKraju">
                                                    <fo:block>
                                                        <xsl:call-template name="mapKodKrajuToNazwa">
                                                            <xsl:with-param name="kodKraju" select="crd:Podmiot1/crd:Adres/crd:KodKraju"/>
                                                        </xsl:call-template>
                                                    </fo:block>
                                                </xsl:if>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </fo:table-body>
                            </fo:table>
                        </xsl:when>
                    </xsl:choose>

<!--                    Linia oddzielająca-->
                   <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                    <!-- Sprzedawca / Wystawca / Nabywca -->
                    <fo:table font-size="7pt" table-layout="fixed" width="100%">
                        <fo:table-column column-width="33%"/>
                        <fo:table-column column-width="33%"/>
                        <fo:table-column column-width="33%"/>
                        <fo:table-body>
                            <fo:table-row space-after="5mm">
                                <fo:table-cell padding-bottom="8px">
                                    <xsl:choose>
                                        <!-- Gdy jest zarówno Podmiot1K jak i Podmiot2K - oba już pokazane wyżej -->
                                        <xsl:when test="crd:Fa/crd:Podmiot1K and crd:Fa/crd:Podmiot2K">
                                            <fo:block/>
                                        </xsl:when>
                                        <!-- Gdy jest tylko Podmiot1K - sprzedawca pokazany wyżej, tu nabywca -->
                                        <xsl:when test="crd:Fa/crd:Podmiot1K">
                                            <fo:block font-size="12pt" text-align="left">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'buyer', $labels)"/></fo:inline>
                                            </fo:block>
                                        </xsl:when>
                                        <!-- Gdy jest tylko Podmiot2K - nabywca pokazany wyżej, tu sprzedawca -->
                                        <xsl:when test="crd:Fa/crd:Podmiot2K">
                                            <fo:block font-size="12pt" text-align="left">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'seller', $labels)"/></fo:inline>
                                            </fo:block>
                                        </xsl:when>
                                        <!-- Standardowa faktura - sprzedawca w pierwszej kolumnie -->
                                        <xsl:otherwise>
                                            <fo:block font-size="12pt" text-align="left">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'seller', $labels)"/></fo:inline>
                                            </fo:block>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:table-cell>
                                <fo:table-cell padding-bottom="8px">
                                    <xsl:choose>
                                        <xsl:when test="crd:Podmiot3 and crd:Podmiot3/crd:Rola = 5">
                                            <fo:block font-size="12pt" text-align="left">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'issuer', $labels)"/></fo:inline>
                                            </fo:block>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <fo:block/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:table-cell>
                                <fo:table-cell padding-bottom="8px">
                                    <xsl:choose>
                                        <!-- Gdy jest Podmiot1K lub Podmiot2K - trzecia kolumna pusta -->
                                        <xsl:when test="crd:Fa/crd:Podmiot1K or crd:Fa/crd:Podmiot2K">
                                            <fo:block/>
                                        </xsl:when>
                                        <!-- Standardowa faktura - nabywca w trzeciej kolumnie -->
                                        <xsl:otherwise>
                                            <fo:block font-size="12pt" text-align="left">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'buyer', $labels)"/></fo:inline>
                                            </fo:block>
                                        </xsl:otherwise>
                                    </xsl:choose>

                                </fo:table-cell>
                            </fo:table-row>

                            <!-- Dane w kolumnach -->
                            <fo:table-row>
                                <fo:table-cell>
                                    <xsl:choose>
                                        <!-- Gdy jest zarówno Podmiot1K jak i Podmiot2K - pierwsza kolumna pusta -->
                                        <xsl:when test="crd:Fa/crd:Podmiot1K and crd:Fa/crd:Podmiot2K">
                                            <fo:block/>
                                        </xsl:when>
                                        <!-- Gdy jest tylko Podmiot1K - pokazujemy nabywcę -->
                                        <xsl:when test="crd:Fa/crd:Podmiot1K">
                                            <xsl:apply-templates select="crd:Podmiot2"/>
                                        </xsl:when>
                                        <!-- Gdy jest tylko Podmiot2K lub standardowa faktura - pokazujemy sprzedawcę -->
                                        <xsl:otherwise>
                                            <xsl:apply-templates select="crd:Podmiot1"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:table-cell>

                                <!-- Dane wystawcy -->
                                <fo:table-cell>
                                    <xsl:choose>
                                        <xsl:when test="crd:Podmiot3[crd:Rola = 5]">
                                            <xsl:for-each select="crd:Podmiot3[crd:Rola = 5]">
                                                <xsl:apply-templates select="."/>
                                            </xsl:for-each>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <fo:block/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:table-cell>

                                <!-- Dane nabywcy -->
                                <fo:table-cell>
                                    <xsl:choose>
                                        <!-- Gdy jest Podmiot1K lub Podmiot2K - trzecia kolumna pusta -->
                                        <xsl:when test="crd:Fa/crd:Podmiot1K or crd:Fa/crd:Podmiot2K">
                                            <fo:block/>
                                        </xsl:when>
                                        <!-- Standardowa faktura - pokazujemy nabywcę -->
                                        <xsl:otherwise>
                                            <xsl:apply-templates select="crd:Podmiot2"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>

                    <!-- Korekta danych nabywcy -->
                    <xsl:choose >
                        <xsl:when test="crd:Fa/crd:Podmiot2K" >
                            <!-- Linia oddzielająca - tylko gdy nie ma Podmiot1K (bo wtedy już jest linia przed główną tabelą) -->
                            <xsl:if test="not(crd:Fa/crd:Podmiot1K)">
                                <fo:block border-bottom="solid 1px grey" space-after="5mm" space-before="4mm"/>
                            </xsl:if>

                            <fo:block font-size="12pt" text-align="left">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'buyer', $labels)"/></fo:inline>
                            </fo:block>

                            <fo:block text-align="left" >
                                <xsl:if test="crd:Podmiot2/crd:NrEORI" >
                                    <fo:block font-size="7pt">
                                        <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
                                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'identification.data', $labels)"/>
                                            </fo:inline>
                                        </fo:block>
                                        <fo:inline font-weight="600" font-size="7pt"><xsl:value-of select="key('kLabels', 'eori.number', $labels)"/>: </fo:inline>
                                        <xsl:value-of select="crd:Podmiot2/crd:NrEORI"/>
                                    </fo:block>
                                </xsl:if>
                            </fo:block>
                            <xsl:if test="crd:Podmiot2/crd:DaneKontaktowe/crd:Email|crd:Podmiot2/crd:DaneKontaktowe/crd:Telefon|crd:Podmiot2/crd:NrKlienta|crd:Podmiot2/crd:IDNabywcy">
                                <fo:block text-align="left" padding-bottom="3px" font-size="7pt" padding-top="8px">
                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'contact.data', $labels)"/>
                                    </fo:inline>
                                </fo:block>
                                <xsl:if test="crd:Podmiot2/crd:DaneKontaktowe/crd:Email">
                                    <fo:block text-align="left" font-size="7pt" padding-bottom="2px">
                                        <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'email', $labels)"/>: </fo:inline>
                                        <xsl:value-of
                                                select="crd:Podmiot2/crd:DaneKontaktowe/crd:Email"/>
                                    </fo:block>
                                </xsl:if>
                                <xsl:if test="crd:Podmiot2/crd:DaneKontaktowe/crd:Telefon">
                                    <fo:block text-align="left" font-size="7pt" padding-bottom="2px">
                                        <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'phone', $labels)"/>: </fo:inline>
                                        <xsl:value-of
                                                select="crd:Podmiot2/crd:DaneKontaktowe/crd:Telefon"/>
                                    </fo:block>
                                </xsl:if>
                                <xsl:if test="crd:Podmiot2/crd:NrKlienta">
                                    <fo:block text-align="left" font-size="7pt" padding-bottom="2px">
                                        <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'customer.number', $labels)"/>: </fo:inline>
                                        <xsl:value-of select="crd:Podmiot2/crd:NrKlienta"/>
                                    </fo:block>
                                </xsl:if>
                                <xsl:if test="crd:Podmiot2/crd:IDNabywcy">
                                    <fo:block text-align="left" font-size="7pt" padding-bottom="2px">
                                        <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'buyerId', $labels)"/>: </fo:inline>
                                        <xsl:value-of select="crd:Podmiot2/crd:IDNabywcy"/>
                                    </fo:block>
                                </xsl:if>
                                <!-- Dodatkowy odstęp po danych kontaktowych -->
                                <fo:block padding-bottom="8px"/>
                            </xsl:if>

                            <fo:table font-size="7pt" table-layout="fixed" width="100%" padding-top="8px">
                                <fo:table-column column-width="33%"/>
                                <fo:table-column column-width="33%"/>
                                <fo:table-column column-width="33%"/>
                                <fo:table-body>
                                    <fo:table-row space-after="5mm">
                                        <fo:table-cell padding-bottom="8px">
                                            <fo:block font-size="9pt" text-align="left">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'contentCorrected', $labels)"/></fo:inline>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell padding-bottom="8px">
                                            <fo:block>

                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell padding-bottom="8px">
                                            <fo:block font-size="9pt" text-align="left">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'contentCorrecting', $labels)"/></fo:inline>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>

                                    <fo:table-row>
                                        <!-- Treść korygowana -->
                                        <fo:table-cell>
                                            <xsl:if test="crd:Fa/crd:Podmiot2K/crd:NrEORI">
                                                <fo:block text-align="left" padding-bottom="3px">
                                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'eori.number', $labels)"/>: </fo:inline>
                                                    <xsl:value-of select="crd:Fa/crd:Podmiot2K/crd:NrEORI"/>
                                                </fo:block>
                                            </xsl:if>
                                            <fo:block text-align="left" padding-bottom="3px" font-size="7pt">
                                                <xsl:if test="crd:Fa/crd:Podmiot2K/crd:DaneIdentyfikacyjne/crd:NrVatUE">
                                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'vatUe.number', $labels)"/>: </fo:inline>
                                                    <xsl:value-of select="crd:Fa/crd:Podmiot2K/crd:DaneIdentyfikacyjne/crd:KodUE"/>
                                                    <xsl:value-of select="crd:Fa/crd:Podmiot2K/crd:DaneIdentyfikacyjne/crd:NrVatUE"/>
                                                </xsl:if>
                                                <xsl:if test="crd:Fa/crd:Podmiot2K/crd:DaneIdentyfikacyjne/crd:NIP">
                                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'nip', $labels)"/>: </fo:inline>
                                                    <xsl:value-of
                                                            select="crd:Fa/crd:Podmiot2K/crd:DaneIdentyfikacyjne/crd:NIP"/>
                                                </xsl:if>
                                            </fo:block>
                                            <fo:block text-align="left" padding-bottom="3px">
                                                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'name', $labels)"/>: </fo:inline>
                                                <xsl:value-of
                                                        select="crd:Fa/crd:Podmiot2K/crd:DaneIdentyfikacyjne/crd:Nazwa"/>
                                            </fo:block>
                                            <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'address', $labels)"/></fo:inline>
                                            </fo:block>
                                            <fo:block text-align="left">
                                                <xsl:value-of select="crd:Fa/crd:Podmiot2K/crd:Adres/crd:AdresL1"/>
                                                <xsl:if test="crd:Fa/crd:Podmiot2K/crd:Adres/crd:AdresL2">
                                                    <fo:inline>, </fo:inline>
                                                    <xsl:value-of select="crd:Fa/crd:Podmiot2K/crd:Adres/crd:AdresL2"/>
                                                </xsl:if>
                                                <xsl:if test="crd:Fa/crd:Podmiot2K/crd:Adres/crd:KodKraju">
                                                    <fo:block>
                                                        <xsl:call-template name="mapKodKrajuToNazwa">
                                                            <xsl:with-param name="kodKraju" select="crd:Fa/crd:Podmiot2K/crd:Adres/crd:KodKraju"/>
                                                        </xsl:call-template>
                                                    </fo:block>
                                                </xsl:if>
                                            </fo:block>
                                        </fo:table-cell>

                                        <fo:table-cell>
                                            <fo:block>

                                            </fo:block>
                                        </fo:table-cell>
                                        <!-- Treść korygująca -->
                                        <fo:table-cell>
                                            <xsl:if test="crd:Podmiot2/crd:NrEORI">
                                                <fo:block text-align="left" padding-bottom="3px">
                                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'eori.number', $labels)"/>: </fo:inline>
                                                    <xsl:value-of select="crd:Podmiot2/crd:NrEORI"/>
                                                </fo:block>
                                            </xsl:if>
                                            <fo:block text-align="left" padding-bottom="3px" font-size="7pt">
                                                <xsl:if test="crd:Podmiot2/crd:DaneIdentyfikacyjne/crd:NrVatUE">
                                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'vatUe.number', $labels)"/>: </fo:inline>
                                                    <xsl:value-of select="crd:Podmiot2/crd:DaneIdentyfikacyjne/crd:KodUE"/>
                                                    <xsl:value-of select="crd:Podmiot2/crd:DaneIdentyfikacyjne/crd:NrVatUE"/>
                                                </xsl:if>
                                                <xsl:if test="crd:Podmiot2/crd:DaneIdentyfikacyjne/crd:NIP">
                                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'nip', $labels)"/>: </fo:inline>
                                                    <xsl:value-of
                                                            select="crd:Podmiot2/crd:DaneIdentyfikacyjne/crd:NIP"/>
                                                </xsl:if>
                                            </fo:block>
                                            <fo:block text-align="left" padding-bottom="3px">
                                                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'name', $labels)"/>: </fo:inline>
                                                <xsl:value-of
                                                        select="crd:Podmiot2/crd:DaneIdentyfikacyjne/crd:Nazwa"/>
                                            </fo:block>
                                            <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'address', $labels)"/></fo:inline>
                                            </fo:block>
                                            <fo:block text-align="left">
                                                <xsl:value-of select="crd:Podmiot2/crd:Adres/crd:AdresL1"/>
                                                <xsl:if test="crd:Podmiot2/crd:Adres/crd:AdresL2">
                                                    <fo:inline>, </fo:inline>
                                                    <xsl:value-of select="crd:Podmiot2/crd:Adres/crd:AdresL2"/>
                                                </xsl:if>
                                                <xsl:if test="crd:Podmiot2/crd:Adres/crd:KodKraju">
                                                    <fo:block>
                                                        <xsl:call-template name="mapKodKrajuToNazwa">
                                                            <xsl:with-param name="kodKraju" select="crd:Podmiot2/crd:Adres/crd:KodKraju"/>
                                                        </xsl:call-template>
                                                    </fo:block>
                                                </xsl:if>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </fo:table-body>
                            </fo:table>
                        </xsl:when>
                    </xsl:choose>

                    <!-- Podmioty inne -->
                    <xsl:if test="crd:Podmiot3[crd:Rola != 5]">
                        <!-- Linia oddzielająca -->
                        <fo:block border-bottom="solid 1px grey" space-before="5mm"/>
                        <!-- Table z podmiotami innymi-->
                        <fo:table table-layout="fixed" width="100%">
                            <fo:table-column column-width="50%"/>
                            <fo:table-column column-width="50%"/>

                            <fo:table-body>
                                <!-- Iterujemy przez wszystkie elementy Podmiot3, zaczynając od pierwszego elementu -->
                                <xsl:for-each select="crd:Podmiot3[crd:Rola != 5][position() mod 2 = 1]">
                                    <fo:table-row>
                                        <!-- Pierwsza komórka w wierszu -->
                                        <fo:table-cell>
                                            <fo:block font-size="7pt">
                                                <xsl:apply-templates select="."/>
                                            </fo:block>
                                        </fo:table-cell>

                                        <!-- Druga komórka, jeśli istnieje element na następnej pozycji -->
                                        <xsl:choose>
                                            <xsl:when test="following-sibling::crd:Podmiot3[crd:Rola != 5]">
                                                <fo:table-cell>
                                                    <fo:block font-size="7pt">
                                                        <xsl:apply-templates select="following-sibling::crd:Podmiot3[1]"/>
                                                    </fo:block>
                                                </fo:table-cell>
                                            </xsl:when>
                                            <!-- Jeśli nie ma następnego elementu, dodajemy pustą komórkę -->
                                            <xsl:otherwise>
                                                <fo:table-cell>
                                                    <fo:block/>
                                                </fo:table-cell>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </fo:table-row>
                                </xsl:for-each>
                            </fo:table-body>
                        </fo:table>
                    </xsl:if>

                    <!-- Linia oddzielająca -->
                   <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                    <!-- Szczegóły -->
                    <fo:block font-size="12pt" text-align="left" space-after="3mm">
                        <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'details', $labels)"/></fo:inline>
                    </fo:block>
                    <fo:table space-after="5mm" table-layout="fixed" width="100%">
                        <fo:table-column column-width="50%" />
                        <fo:table-column column-width="50%" />
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell padding-right="6pt">
                                    <xsl:if test="crd:Fa/crd:P_1">
                                        <fo:block font-size="8pt" text-align="left">
                                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'issueDate', $labels)"/>: </fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:P_1"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:P_6">
                                        <fo:block font-size="8pt" text-align="left">
                                            <xsl:choose>
                                                <xsl:when test="crd:Fa/crd:RodzajFaktury = 'ZAL'">
                                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'paymentReceivedDate', $labels)"/>:
                                                    </fo:inline>
                                                </xsl:when>
                                                <xsl:otherwise>
<fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'deliveryDate', $labels)"/>:
                                                    </fo:inline>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <xsl:value-of select="crd:Fa/crd:P_6"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:PrzyczynaKorekty">
                                        <fo:block font-size="8pt" text-align="left">
                                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'correctionReason', $labels)"/>:
                                            </fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:PrzyczynaKorekty"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:TypKorekty">
                                        <fo:block font-size="8pt" text-align="left">
                                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'correctionType', $labels)"/>: </fo:inline>
                                            <xsl:choose>
                                                <xsl:when test="crd:Fa/crd:TypKorekty = 1">
                                                    <xsl:value-of select="key('kLabels', 'correctionType.original', $labels)"/>
                                                </xsl:when>
                                                <xsl:when test="crd:Fa/crd:TypKorekty = 2">
                                                    <xsl:value-of select="key('kLabels', 'correctionType.correcting', $labels)"/>
                                                </xsl:when>
                                                <xsl:when test="crd:Fa/crd:TypKorekty = 3">
                                                    <xsl:value-of select="key('kLabels', 'correctionType.other', $labels)"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:text/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </fo:block>
                                    </xsl:if>
                                </fo:table-cell>
                                <fo:table-cell padding-left="6pt">
                                    <xsl:if test="crd:Fa/crd:P_1M">
                                        <fo:block font-size="8pt" text-align="left">
                                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'issuePlace', $labels)"/>: </fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:P_1M"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:KursWalutyZ">
                                        <fo:block text-align="left" font-size="8pt">
                                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'exchangeRate', $labels)"/>: </fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:KursWalutyZ"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:OkresFaKorygowanej">
                                        <fo:block text-align="left" font-size="8pt">
                                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'discountPeriod', $labels)"/>: </fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:OkresFaKorygowanej"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:FaWiersz[1]/crd:KursWaluty">
                                        <fo:block text-align="left" font-size="8pt">
                                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'exchangeRate', $labels)"/>: </fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:FaWiersz[1]/crd:KursWaluty"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:choose>
                                        <xsl:when test="$currencyDate">
                                            <fo:block text-align="left" font-size="8pt">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'exchangeRateDate', $labels)"/>: </fo:inline>
                                                <xsl:value-of select="$currencyDate"/>
                                            </fo:block>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <fo:block/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>

                    <!-- Numery wcześniejszych faktur zaliczkowych -->
                    <xsl:if test="count(crd:Fa/crd:FakturaZaliczkowa/crd:NrKSeFFaZaliczkowej) > 0 or count(crd:Fa/crd:FakturaZaliczkowa/crd:NrFaZaliczkowej) > 0">
                        <fo:block padding-bottom="16px">
                            <!-- Numery faktur-->
                            <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                                <fo:table-column column-width="50%"/> <!-- Numery wcześniejszych faktur zaliczkowych  -->

                                <fo:table-header>
                                    <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block><xsl:value-of select="key('kLabels', 'previousAdvanceInvoices', $labels)"/></fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </fo:table-header>
                                <fo:table-body>
                                        <xsl:for-each select="crd:Fa/crd:FakturaZaliczkowa/crd:NrKSeFFaZaliczkowej">
                                            <fo:table-row>
                                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                                    <fo:block text-align="left">
                                                        <xsl:value-of select="."/>
                                                    </fo:block>
                                                </fo:table-cell>
                                            </fo:table-row>
                                        </xsl:for-each>
                                        <xsl:for-each select="crd:Fa/crd:FakturaZaliczkowa/crd:NrFaZaliczkowej">
                                            <fo:table-row>
                                                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                                    <fo:block text-align="left">
                                                        <xsl:value-of select="."/>
                                                    </fo:block>
                                                </fo:table-cell>
                                            </fo:table-row>
                                        </xsl:for-each>
                                </fo:table-body>
                            </fo:table>
                        </fo:block>
                    </xsl:if>

                    <!-- Dodatkowy opis-->
                    <xsl:if test="count(crd:Fa/crd:DodatkowyOpis) > 0">
                        <fo:block>
                            <fo:block text-align="left" space-after="2mm">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'additionalDescription', $labels)"/></fo:inline>
                            </fo:block>
                            <!-- Dodatkowe opisy-->
                            <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                                <xsl:variable name="dodatkowyOpisElements" select="crd:Fa/crd:DodatkowyOpis"/>
                                <xsl:variable name="hasNrWiersza" select="boolean($dodatkowyOpisElements/crd:NrWiersza[normalize-space()])"/>

                                <!-- Dynamiczne szerokości kolumn w zależności od obecności NrWiersza -->
                                <xsl:choose>
                                    <xsl:when test="$hasNrWiersza">
                                        <fo:table-column column-width="10%"/> <!-- Nr wiersza -->
                                        <fo:table-column column-width="45%"/> <!-- Rodzaj informacji -->
                                        <fo:table-column column-width="45%"/> <!-- Treść informacji -->
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <fo:table-column column-width="50%"/> <!-- Rodzaj informacji  -->
                                        <fo:table-column column-width="50%"/> <!-- Treść informacji -->
                                    </xsl:otherwise>
                                </xsl:choose>

                                <fo:table-header>
                                    <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                        <xsl:if test="$hasNrWiersza">
                                            <fo:table-cell
                                                    xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                <fo:block><xsl:value-of select="key('kLabels', 'rowNumber', $labels)"/></fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block><xsl:value-of select="key('kLabels', 'infoType', $labels)"/></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block><xsl:value-of select="key('kLabels', 'infoContent', $labels)"/></fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </fo:table-header>
                                <fo:table-body>
                                    <xsl:apply-templates select="crd:Fa/crd:DodatkowyOpis">
                                        <xsl:with-param name="hasNrWiersza" select="$hasNrWiersza"/>
                                    </xsl:apply-templates>
                                </fo:table-body>
                            </fo:table>
                        </fo:block>
                    </xsl:if>


                    <!-- Show positions section only if there are invoice lines or orders -->

                    <xsl:if test="crd:Fa/crd:FaWiersz or crd:Fa/crd:Zamowienie/crd:ZamowienieWiersz or crd:Fa/crd:OkresFaKorygowanej">
                        <!-- Linia oddzielająca -->
                        <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
                        
                        <!-- Label informujący o cenach netto/brutto -->
                        <xsl:if test="crd:Fa/crd:KodWaluty and (crd:Fa/crd:FaWiersz or crd:Fa/crd:Zamowienie/crd:ZamowienieWiersz)">
                            <xsl:variable name="hasNetPrices" select="boolean(crd:Fa/crd:FaWiersz[crd:P_9A and crd:P_11] or crd:Fa/crd:Zamowienie/crd:ZamowienieWiersz[crd:P_9AZ and crd:P_11NettoZ])"/>
                            <xsl:variable name="hasGrossPrices" select="boolean(crd:Fa/crd:FaWiersz[crd:P_9B and crd:P_11A])"/>
                            <xsl:if test="$hasNetPrices or $hasGrossPrices">
                                <fo:block font-size="8pt" font-weight="bold" text-align="left" space-after="3mm">
                                    <xsl:choose>
                                        <xsl:when test="$hasNetPrices">
                                            <fo:inline><xsl:value-of select="key('kLabels', 'invoiceInNetPrices', $labels)"/><xsl:text> </xsl:text></fo:inline>
                                        </xsl:when>
                                        <xsl:when test="$hasGrossPrices">
                                            <fo:inline><xsl:value-of select="key('kLabels', 'invoiceInGrossPrices', $labels)"/><xsl:text> </xsl:text></fo:inline>
                                        </xsl:when>
                                    </xsl:choose>
                                    <xsl:value-of select="crd:Fa/crd:KodWaluty"/>
                                </fo:block>
                            </xsl:if>
                        </xsl:if>
                        
                        <fo:block text-align="left" space-after="2mm">
                            <fo:inline font-weight="bold" font-size="12pt">
                                <xsl:choose>
                                    <xsl:when test="crd:Fa/crd:RodzajFaktury = 'ZAL'"><xsl:value-of select="key('kLabels', 'order', $labels)"/></xsl:when>
                                    <xsl:when test="crd:Fa/crd:OkresFaKorygowanej"><xsl:value-of select="key('kLabels', 'discount', $labels)"/></xsl:when>
                                    <xsl:otherwise><xsl:value-of select="key('kLabels', 'positions', $labels)"/></xsl:otherwise>
                                </xsl:choose>
                            </fo:inline>
                        </fo:block>
                        <xsl:if test="crd:Fa/crd:OkresFaKorygowanej">
<!--                            <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>-->
<!--                            <fo:block text-align="left" space-after="2mm">-->
<!--                                <fo:inline font-weight="bold" font-size="12pt">-->
<!--                                    Rabat-->
<!--                                </fo:inline>-->
<!--                            </fo:block>-->
                            <fo:block color="#343a40" font-size="9pt" text-align="left" space-before="3mm" space-after="3mm">
                                <xsl:choose>
                                    <xsl:when test="crd:Fa/crd:FaWiersz">
                                        <fo:inline><xsl:value-of select="key('kLabels', 'discountNotAll', $labels)"/></fo:inline>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <fo:inline><xsl:value-of select="key('kLabels', 'discountAll', $labels)"/></fo:inline>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </fo:block>
                        </xsl:if>

                        <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:Fa/crd:FaWiersz[crd:StanPrzed = 1] and crd:Fa/crd:FaWiersz[not(crd:StanPrzed)]">
                                <!-- Show "before correction" positions only if they exist -->
                                <xsl:if test="crd:Fa/crd:FaWiersz[crd:StanPrzed = 1]">
                                    <fo:block text-align="left" space-after="1mm">
                                        <fo:inline font-weight="bold" font-size="10pt"><xsl:value-of select="key('kLabels', 'positionsBeforeCorrection', $labels)"/></fo:inline>
                                    </fo:block>
                                    <!-- Pozycje na FV-->
                                    <xsl:call-template name="positionsTable">
                                        <xsl:with-param name="faWiersz" select="crd:Fa/crd:FaWiersz[crd:StanPrzed = 1]"/>
                                    </xsl:call-template>
                                </xsl:if>

                                <!-- Add differences table when showCorrectionDifferences is true -->
                                <xsl:if test="$showCorrectionDifferences">
                                    <fo:block text-align="left" space-after="1mm">
                                        <fo:inline font-weight="bold" font-size="10pt"><xsl:value-of select="key('kLabels', 'difference', $labels)"/></fo:inline>
                                    </fo:block>
                                    <xsl:call-template name="differencesTable">
                                        <xsl:with-param name="faWierszBefore" select="crd:Fa/crd:FaWiersz[crd:StanPrzed = 1]"/>
                                        <xsl:with-param name="faWierszAfter" select="crd:Fa/crd:FaWiersz[not(crd:StanPrzed)]"/>
                                    </xsl:call-template>
                                </xsl:if>

                                <!-- Show "after correction" positions only if they exist -->
                                <xsl:if test="crd:Fa/crd:FaWiersz[not(crd:StanPrzed)]">
                                    <fo:block text-align="left" space-after="1mm">
                                        <fo:inline font-weight="bold" font-size="10pt"><xsl:value-of select="key('kLabels', 'positionsAfterCorrection', $labels)"/></fo:inline>
                                    </fo:block>
                                    <xsl:call-template name="positionsTable">
                                        <xsl:with-param name="faWiersz" select="crd:Fa/crd:FaWiersz[not(crd:StanPrzed)]"/>
                                    </xsl:call-template>
                                </xsl:if>
                            </xsl:when>
                            <xsl:when test="crd:Fa/crd:FaWiersz[crd:StanPrzed = 1] and not(crd:Fa/crd:FaWiersz[not(crd:StanPrzed)])">
                                <!-- Show "before correction" positions only if they exist -->
                                <xsl:if test="crd:Fa/crd:FaWiersz[crd:StanPrzed = 1]">
                                    <fo:block text-align="left" space-after="1mm">
                                        <fo:inline font-weight="bold" font-size="10pt"><xsl:value-of select="key('kLabels', 'positionsBeforeCorrection', $labels)"/></fo:inline>
                                    </fo:block>
                                    <!-- Tylko pozycje przed korektą -->
                                    <xsl:call-template name="positionsTable">
                                        <xsl:with-param name="faWiersz" select="crd:Fa/crd:FaWiersz[crd:StanPrzed = 1]"/>
                                    </xsl:call-template>
                                </xsl:if>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:choose>
                                    <xsl:when test="crd:Fa/crd:RodzajFaktury = 'ZAL' or crd:Fa/crd:RodzajFaktury = 'KOR_ZAL'">
                                        <xsl:call-template name="zamowienieTable">
                                            <xsl:with-param name="zamowienieWiersz" select="crd:Fa/crd:Zamowienie/crd:ZamowienieWiersz"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <!-- Show positions table only if there are invoice lines -->
                                        <xsl:if test="crd:Fa/crd:FaWiersz">
                                            <xsl:call-template name="positionsTable">
                                                <xsl:with-param name="faWiersz" select="crd:Fa/crd:FaWiersz"/>
                                            </xsl:call-template>
                                        </xsl:if>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                        </fo:block>
                    </xsl:if>

                    <!-- Show amounts section only if there are invoice lines or orders -->
                    <xsl:if test="crd:Fa/crd:FaWiersz or crd:Fa/crd:Zamowienie/crd:ZamowienieWiersz or crd:Fa/crd:OkresFaKorygowanej">
                        <!-- Kwota należności ogółem -->

                        <!-- Conditional block for displaying correction amounts only when RodzajFaktury = 'KOR' -->
                        <xsl:if test="crd:Fa/crd:RodzajFaktury = 'KOR'">
                            <!-- Optional block for Kwota brutto przed korektą -->
                            <xsl:if test="boolean(sum(crd:Fa/crd:FaWiersz[crd:StanPrzed = 1]/crd:P_11A))">
                                <fo:block color="#6c757d" font-size="8pt" text-align="right" space-before="2mm">
                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'grossAmountBeforeCorrection', $labels)"/>: </fo:inline>
                                    <fo:inline>
                                        <xsl:value-of select="translate(format-number(sum(crd:Fa/crd:FaWiersz[crd:StanPrzed = 1]/crd:P_11A), '#,##0.00'), ',.', ' ,')"/>
                                        <xsl:text> </xsl:text>
                                        <fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:KodWaluty"/>
                                        </fo:inline>
                                    </fo:inline>
                                </fo:block>
                            </xsl:if>

                            <!-- Optional block for Kwota brutto po korekcie -->
                            <xsl:if test="boolean(sum(crd:Fa/crd:FaWiersz[not(crd:StanPrzed)]/crd:P_11A))">
                                <fo:block color="#6c757d" font-size="8pt" text-align="right">
                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'grossAmountAfterCorrection', $labels)"/>: </fo:inline>
                                    <fo:inline>
                                        <xsl:value-of select="translate(format-number(sum(crd:Fa/crd:FaWiersz[not(crd:StanPrzed)]/crd:P_11A), '#,##0.00'), ',.', ' ,')"/>
                                        <xsl:text> </xsl:text>
                                        <fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:KodWaluty"/>
                                        </fo:inline>
                                    </fo:inline>
                                </fo:block>
                            </xsl:if>
                        </xsl:if>
                        <xsl:choose>
                            <xsl:when test="crd:Fa/crd:RodzajFaktury = 'ZAL'">
                                <fo:block color="#343a40" font-size="10pt" text-align="right" space-before="3mm">
                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'advancePaymentReceived', $labels)"/>: </fo:inline>
                                    <fo:inline>
                                        <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_15), '#,##0.00'), ',.', ' ,')"/>
                                        <xsl:text> </xsl:text>
                                        <fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:KodWaluty"/>
                                        </fo:inline>
                                    </fo:inline>
                                </fo:block>
                            </xsl:when>
                            <xsl:when test="crd:Fa/crd:RodzajFaktury = 'ROZ'">
                                <fo:block color="#343a40" font-size="10pt" text-align="right" space-before="3mm">
                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'amountRemaining', $labels)"/>: </fo:inline>
                                    <fo:inline>
                                        <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_15), '#,##0.00'), ',.', ' ,')"/>
                                        <xsl:text> </xsl:text>
                                        <fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:KodWaluty"/>
                                        </fo:inline>
                                    </fo:inline>
                                </fo:block>
                            </xsl:when>
                            <xsl:when test="crd:Fa/crd:OkresFaKorygowanej">
                                <fo:block color="#343a40" font-size="10pt" text-align="right" space-before="3mm">
                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'totalDiscount', $labels)"/>: </fo:inline>
                                    <fo:inline>
                                        <xsl:value-of select="translate(format-number(abs(number(crd:Fa/crd:P_15)), '#,##0.00'), ',.', ' ,')"/>
                                        <xsl:text> </xsl:text>
                                        <fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:KodWaluty"/>
                                        </fo:inline>
                                    </fo:inline>
                                </fo:block>
                            </xsl:when>
                            <xsl:otherwise>
                                <fo:block color="#343a40" font-size="10pt" text-align="right" space-before="3mm">
                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'totalAmount', $labels)"/>: </fo:inline>
                                    <fo:inline>
                                        <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_15), '#,##0.00'), ',.', ' ,')"/>
                                        <xsl:text> </xsl:text>
                                        <fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:KodWaluty"/>
                                        </fo:inline>
                                    </fo:inline>
                                </fo:block>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>


                    <!-- Podsumowanie stawek podatku-->
                    <xsl:variable name="hasAnyTaxRates" select="crd:Fa/crd:P_13_1 != 0 or crd:Fa/crd:P_14_1 != 0 or
                                                               crd:Fa/crd:P_13_2 != 0 or crd:Fa/crd:P_14_2 != 0 or
                                                               crd:Fa/crd:P_13_3 != 0 or crd:Fa/crd:P_14_3 != 0 or
                                                               crd:Fa/crd:P_13_4 != 0 or crd:Fa/crd:P_14_4 != 0 or
                                                               crd:Fa/crd:P_13_5 != 0 or crd:Fa/crd:P_14_5 != 0 or
                                                               crd:Fa/crd:P_13_6_1 != 0 or crd:Fa/crd:P_13_6_2 != 0 or
                                                               crd:Fa/crd:P_13_7 != 0 or crd:Fa/crd:P_13_8 != 0 or
                                                               crd:Fa/crd:P_13_9 != 0 or crd:Fa/crd:P_13_10 != 0 or
                                                               crd:Fa/crd:P_13_11 != 0"/>

                    <xsl:if test="$hasAnyTaxRates">
                        <!-- Linia oddzielająca -->
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                        <fo:block font-size="12pt" text-align="left" space-after="2mm">
                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'taxRateSummary', $labels)"/></fo:inline>
                        </fo:block>
                        <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                            <fo:table-column column-width="20%"/> <!-- Stawka podatku -->
                            <fo:table-column column-width="20%"/> <!-- Kwota netto-->
                            <fo:table-column column-width="20%"/> <!-- Kwota podatku -->
                            <fo:table-column column-width="20%"/> <!-- Kwota brutto -->
                            <xsl:if test="crd:Fa/crd:P_14_1W|crd:Fa/crd:P_14_2W|crd:Fa/crd:P_14_3W">
                                <fo:table-column column-width="20%"/> <!-- Kwota podatku PLN -->
                            </xsl:if>
                            <fo:table-header>
                                <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                    <fo:table-cell
                                            xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block><xsl:value-of select="key('kLabels', 'row.taxRate', $labels)"/></fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell
                                            xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block><xsl:value-of select="key('kLabels', 'netAmount', $labels)"/></fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell
                                            xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block><xsl:value-of select="key('kLabels', 'taxAmount', $labels)"/></fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell
                                            xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block><xsl:value-of select="key('kLabels', 'grossAmount', $labels)"/></fo:block>
                                    </fo:table-cell>
                                    <xsl:if test="crd:Fa/crd:P_14_1W | crd:Fa/crd:P_14_2W | crd:Fa/crd:P_14_3W | crd:Fa/crd:P_14_4W">
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block><xsl:value-of select="key('kLabels', 'taxAmountPln', $labels)"/></fo:block>
                                        </fo:table-cell>
                                    </xsl:if>
                                </fo:table-row>
                            </fo:table-header>
                            <fo:table-body>
                                <!-- Sprawdzenie, czy istnieją jakiekolwiek dane do wyświetlenia -->
                                <xsl:variable name="hasAnyTaxRates" select="crd:Fa/crd:P_13_1 != 0 or crd:Fa/crd:P_14_1 != 0 or
                                                                           crd:Fa/crd:P_13_2 != 0 or crd:Fa/crd:P_14_2 != 0 or
                                                                           crd:Fa/crd:P_13_3 != 0 or crd:Fa/crd:P_14_3 != 0 or
                                                                           crd:Fa/crd:P_13_4 != 0 or crd:Fa/crd:P_14_4 != 0 or
                                                                           crd:Fa/crd:P_13_5 != 0 or crd:Fa/crd:P_14_5 != 0 or
                                                                           crd:Fa/crd:P_13_6_1 != 0 or crd:Fa/crd:P_13_6_2 != 0 or
                                                                           crd:Fa/crd:P_13_7 != 0 or crd:Fa/crd:P_13_8 != 0 or
                                                                           crd:Fa/crd:P_13_9 != 0 or crd:Fa/crd:P_13_10 != 0 or
                                                                           crd:Fa/crd:P_13_11 != 0"/>

                                <!-- Jeśli nie ma żadnych stawek podatku, wyświetl informację -->
                                <xsl:if test="not($hasAnyTaxRates)">
                                    <fo:table-row>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" number-columns-spanned="100">
                                            <fo:block text-align="center"><xsl:value-of select="key('kLabels', 'noDataToDisplay', $labels)"/></fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:if>

                                <xsl:if test="crd:Fa/crd:P_13_1 | crd:Fa/crd:P_14_1 and crd:Fa/crd:P_13_1 != 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:text>23%</xsl:text>  <!-- Stawka podatku -->
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_1), '#,##0.00'), ',.', ' ,')"/>  <!-- Kwota netto -->
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_14_1), '#,##0.00'), ',.', ' ,')"/>  <!-- Kwota podatku -->
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_1) + number(crd:Fa/crd:P_14_1), '#,##0.00'), ',.', ' ,')"/>  <!-- Kwota brutto -->
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_1W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="translate(format-number(number(crd:Fa/crd:P_14_1W), '#,##0.00'), ',.', ' ,')"/>  <!-- Kwota podatku PLN -->
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:P_13_2 | crd:Fa/crd:P_14_2  and crd:Fa/crd:P_13_2 != 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:text>8%</xsl:text>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_2), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_14_2), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_2) + number(crd:Fa/crd:P_14_2), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_2W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="translate(format-number(number(crd:Fa/crd:P_14_2W), '#,##0.00'), ',.', ' ,')"/>  <!-- Kwota podatku PLN -->
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:P_13_3 | crd:Fa/crd:P_14_3  and crd:Fa/crd:P_13_3 != 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:text>5%</xsl:text>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_3), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_14_3), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_3) + number(crd:Fa/crd:P_14_3), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_3W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="translate(format-number(number(crd:Fa/crd:P_14_3W), '#,##0.00'), ',.', ' ,')"/>  <!-- Kwota podatku PLN -->
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:P_13_4 | crd:Fa/crd:P_14_4  and crd:Fa/crd:P_13_4 != 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:text>4%</xsl:text>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_4), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_14_4), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_4) + number(crd:Fa/crd:P_14_4), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_4W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="translate(format-number(number(crd:Fa/crd:P_14_4W), '#,##0.00'), ',.', ' ,')"/>  <!-- Kwota podatku PLN -->
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:P_13_5 | crd:Fa/crd:P_14_5  and crd:Fa/crd:P_13_5 != 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:value-of select="key('kLabels', 'taxRate.npExclArt100Alt', $labels)"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_5), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_14_5), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_5) + number(crd:Fa/crd:P_14_5), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_1W | crd:Fa/crd:P_14_2W | crd:Fa/crd:P_14_3W | crd:Fa/crd:P_14_4W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                    <xsl:value-of></xsl:value-of>
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:P_13_6_1 and crd:Fa/crd:P_13_6_1 != 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:value-of select="key('kLabels', 'taxRate.zeroDomestic', $labels)"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_6_1), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_6_1) + 0, '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_1W | crd:Fa/crd:P_14_2W | crd:Fa/crd:P_14_3W | crd:Fa/crd:P_14_4W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:P_13_6_2 and crd:Fa/crd:P_13_6_2 != 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:value-of select="key('kLabels', 'taxRate.zeroWdt', $labels)"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_6_2), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_6_2) + 0, '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_1W | crd:Fa/crd:P_14_2W | crd:Fa/crd:P_14_3W | crd:Fa/crd:P_14_4W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:P_13_6_3 and crd:Fa/crd:P_13_6_3 != 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:value-of select="key('kLabels', 'taxRate.zeroExport', $labels)"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_6_3), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_6_3) + 0, '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_1W | crd:Fa/crd:P_14_2W | crd:Fa/crd:P_14_3W | crd:Fa/crd:P_14_4W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:P_13_7 and crd:Fa/crd:P_13_7 != 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:value-of select="key('kLabels', 'taxRate.exempt', $labels)"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_7), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_7) + 0, '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_1W | crd:Fa/crd:P_14_2W | crd:Fa/crd:P_14_3W | crd:Fa/crd:P_14_4W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:P_13_8 and crd:Fa/crd:P_13_8 != 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:value-of select="key('kLabels', 'taxRate.npExclArt100', $labels)"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_8), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_8) + 0, '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_1W | crd:Fa/crd:P_14_2W | crd:Fa/crd:P_14_3W | crd:Fa/crd:P_14_4W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:P_13_9 and crd:Fa/crd:P_13_9 != 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:value-of select="key('kLabels', 'taxRate.npArt100', $labels)"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_9), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_9) + 0, '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_1W | crd:Fa/crd:P_14_2W | crd:Fa/crd:P_14_3W | crd:Fa/crd:P_14_4W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:P_13_10 and crd:Fa/crd:P_13_10 != 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:value-of select="key('kLabels', 'taxRate.reverseCharge', $labels)"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_10), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_10) + 0, '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_1W | crd:Fa/crd:P_14_2W | crd:Fa/crd:P_14_3W | crd:Fa/crd:P_14_4W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                    <xsl:value-of
                                                            select="translate(format-number(number(0), '#,##0.00'), ',.', ' ,')"/>
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:P_13_11 and crd:Fa/crd:P_13_11 > 0">
                                    <fo:table-row>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                            <fo:block>
                                                <xsl:value-of select="key('kLabels', 'taxRate.margin', $labels)"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_11), '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>

                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                       text-align="right">
                                            <fo:block>
                                                <xsl:value-of
                                                        select="translate(format-number(number(crd:Fa/crd:P_13_11) + 0, '#,##0.00'), ',.', ' ,')"/>
                                            </fo:block>
                                        </fo:table-cell>
                                        <xsl:if test="crd:Fa/crd:P_14_1W | crd:Fa/crd:P_14_2W | crd:Fa/crd:P_14_3W | crd:Fa/crd:P_14_4W">
                                            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding"
                                                           text-align="right">
                                                <fo:block>
                                                </fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                    </fo:table-row>
                                </xsl:if>
                            </fo:table-body>
                        </fo:table>
                    </xsl:if>

                    <xsl:if test="crd:Fa/crd:Adnotacje/crd:P_16 = 1 or crd:Fa/crd:Adnotacje/crd:P_17 = 1 or crd:Fa/crd:Adnotacje/crd:P_18 = 1 or crd:Fa/crd:Adnotacje/crd:P_18A = 1">

                        <!-- Adnotacje -->
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                        <fo:block font-size="12pt" text-align="left" space-after="5mm">
                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'annotations', $labels)"/></fo:inline>
                        </fo:block>

                        <fo:table table-layout="fixed" width="100%" space-after="2mm">
                            <fo:table-column column-width="50%"/>
                            <fo:table-column column-width="50%"/>
                            <fo:table-body>
                                <fo:table-row>
                                    <fo:table-cell padding-right="5mm">
                                        <fo:block font-size="7pt" text-align="left">
                                            <xsl:if test="crd:Fa/crd:Adnotacje/crd:P_16 = 1">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'cashMethod', $labels)"/></fo:inline>
                                            </xsl:if>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding-left="5mm">
                                        <fo:block font-size="7pt" text-align="left">
                                            <xsl:if test="crd:Fa/crd:Adnotacje/crd:P_17 = 1">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'selfBilling', $labels)"/></fo:inline>
                                            </xsl:if>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                                <fo:table-row>
                                    <fo:table-cell padding-right="5mm">
                                        <fo:block font-size="7pt" text-align="left">
                                            <xsl:if test="crd:Fa/crd:Adnotacje/crd:P_18 = 1">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'reverseCharge', $labels)"/></fo:inline>
                                            </xsl:if>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding-left="5mm">
                                        <fo:block font-size="7pt" text-align="left">
                                            <xsl:if test="crd:Fa/crd:Adnotacje/crd:P_18A = 1">
                                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'splitPayment', $labels)"/></fo:inline>
                                            </xsl:if>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-body>
                        </fo:table>

                    </xsl:if>
                    <!-- Płatność -->
                    <xsl:if test="crd:Fa/crd:Platnosc and (
                        crd:Fa/crd:Platnosc/crd:Zaplacono or
                        crd:Fa/crd:Platnosc/crd:ZnacznikZaplatyCzesciowej or
                        crd:Fa/crd:Platnosc/crd:DataZaplaty or
                        crd:Fa/crd:Platnosc/crd:FormaPlatnosci or
                        crd:Fa/crd:Platnosc/crd:PlatnoscInna or
                        crd:Fa/crd:Platnosc/crd:TerminPlatnosci/crd:Termin or
                        crd:Fa/crd:Platnosc/crd:TerminPlatnosci/crd:TerminOpis or
                        crd:Fa/crd:Platnosc/crd:ZaplataCzesciowa)">
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                        <fo:block font-size="12pt" text-align="left" space-after="2mm">
                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'payment', $labels)"/></fo:inline>
                        </fo:block>

                        <!-- Informacja o płatności -->
                        <xsl:if test="crd:Fa/crd:Platnosc/crd:Zaplacono = 1">
                            <fo:block font-size="7pt" text-align="left" space-after="1mm">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'paymentInfo', $labels)"/>:</fo:inline>
                                <xsl:text> </xsl:text><xsl:value-of select="key('kLabels', 'payment.paid', $labels)"/>
                            </fo:block>
                        </xsl:if>

                        <xsl:if test="crd:Fa/crd:Platnosc/crd:ZnacznikZaplatyCzesciowej = 1 or (crd:Fa/crd:Platnosc/crd:Zaplacono != 1 and crd:Fa/crd:Platnosc/crd:ZaplataCzesciowa[1]/crd:KwotaZaplatyCzesciowej > 0)">
                            <fo:block font-size="7pt" text-align="left" space-after="1mm">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'paymentInfo', $labels)"/>:</fo:inline>
                                <xsl:text> </xsl:text><xsl:value-of select="key('kLabels', 'payment.partial', $labels)"/>
                            </fo:block>
                        </xsl:if>

                        <!-- DataZaplaty -->
                        <xsl:if test="crd:Fa/crd:Platnosc/crd:DataZaplaty">
                            <fo:block font-size="7pt" text-align="left" space-after="1mm">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'paymentDate', $labels)"/>: </fo:inline>
                                <xsl:value-of select="crd:Fa/crd:Platnosc/crd:DataZaplaty"/>
                            </fo:block>
                        </xsl:if>

                        <!-- Forma płatności -->
                        <xsl:choose>
                            <xsl:when test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci">
                                <fo:block font-size="7pt" text-align="left" space-after="1mm">
                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'paymentMethod', $labels)"/>: </fo:inline>
                                    <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '1'">
                                        <xsl:value-of select="key('kLabels', 'paymentMethod.cash', $labels)"/>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '2'">
                                        <xsl:value-of select="key('kLabels', 'paymentMethod.card', $labels)"/>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '3'">
                                        <xsl:value-of select="key('kLabels', 'paymentMethod.voucher', $labels)"/>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '4'">
                                        <xsl:value-of select="key('kLabels', 'paymentMethod.check', $labels)"/>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '5'">
                                        <xsl:value-of select="key('kLabels', 'paymentMethod.credit', $labels)"/>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '6'">
                                        <xsl:value-of select="key('kLabels', 'paymentMethod.transfer', $labels)"/>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '7'">
                                        <xsl:value-of select="key('kLabels', 'paymentMethod.mobile', $labels)"/>
                                    </xsl:if>
                                </fo:block>
                            </xsl:when>
                            <xsl:when test="crd:Fa/crd:Platnosc/crd:PlatnoscInna = 1">
                                <fo:block font-size="7pt" text-align="left" space-after="1mm">
                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'paymentMethod', $labels)"/>: </fo:inline>
                                    <xsl:if test="crd:Fa/crd:Platnosc/crd:OpisPlatnosci">
                                        <xsl:value-of select="crd:Fa/crd:Platnosc/crd:OpisPlatnosci"/>
                                    </xsl:if>
                                </fo:block>
                            </xsl:when>
                        </xsl:choose>

                        <!-- Termin płatności -->
                        <xsl:if test="crd:Fa/crd:Platnosc/crd:TerminPlatnosci/crd:Termin">
                            <fo:block font-size="7pt" text-align="left" space-after="1mm">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'paymentDueDate', $labels)"/>: </fo:inline>
                                <xsl:value-of select="crd:Fa/crd:Platnosc/crd:TerminPlatnosci/crd:Termin"/>
                            </fo:block>
                        </xsl:if>

                        <xsl:if test="crd:Fa/crd:Platnosc/crd:TerminPlatnosci/crd:TerminOpis">
                            <fo:block font-size="7pt" text-align="left" space-after="1mm">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'paymentDescription', $labels)"/>: </fo:inline>
                                <xsl:value-of select="crd:Fa/crd:Platnosc/crd:TerminPlatnosci/crd:TerminOpis"/>
                            </fo:block>
                        </xsl:if>

                        <!-- Tabela płatności częściowych -->
                        <xsl:if test="crd:Fa/crd:Platnosc/crd:ZnacznikZaplatyCzesciowej = 1 or (crd:Fa/crd:Platnosc/crd:Zaplacono != 1 and crd:Fa/crd:Platnosc/crd:ZaplataCzesciowa)">
                            <fo:block space-before="2mm" space-after="2mm">
                                <fo:table table-layout="fixed" width="100%">
                                    <fo:table-column column-width="25%"/>
                                    <fo:table-column column-width="25%"/>
                                    <fo:table-header>
                                        <fo:table-row background-color="#f0f0f0">
                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding" >
                                                <fo:block font-size="7pt" font-weight="bold" text-align="left">
                                                    <xsl:value-of select="key('kLabels', 'partialPaymentDate', $labels)"/>
                                                </fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                <fo:block font-size="7pt" font-weight="bold" text-align="left">
                                                    <xsl:value-of select="key('kLabels', 'partialPaymentAmount', $labels)"/>
                                                </fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </fo:table-header>
                                    <fo:table-body>
                                        <xsl:for-each select="crd:Fa/crd:Platnosc/crd:ZaplataCzesciowa">
                                            <fo:table-row>
                                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                    <fo:block font-size="7pt" text-align="left">
                                                        <xsl:value-of select="crd:DataZaplatyCzesciowej"/>
                                                    </fo:block>
                                                </fo:table-cell>
                                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                    <fo:block font-size="7pt" text-align="right">
                                                        <xsl:value-of select="translate(format-number(number(crd:KwotaZaplatyCzesciowej), '#,##0.00'), ',.', ' ,')"/>
                                                    </fo:block>
                                                </fo:table-cell>
                                            </fo:table-row>
                                        </xsl:for-each>
                                    </fo:table-body>
                                </fo:table>
                            </fo:block>
                        </xsl:if>
                    </xsl:if>
                    <xsl:if test="crd:Fa/crd:WarunkiTransakcji">
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                        <fo:block font-size="12pt" text-align="left" space-after="4mm">
                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'transactionConditions', $labels)"/></fo:inline>
                        </fo:block>

                        <!-- Sprawdzamy, czy istnieją Umowy i Zamówienia -->
                        <xsl:variable name="hasUmowy" select="count(crd:Fa/crd:WarunkiTransakcji/crd:Umowy) > 0"/>
                        <xsl:variable name="hasZamowienia" select="count(crd:Fa/crd:WarunkiTransakcji/crd:Zamowienia) > 0"/>

                        <xsl:choose>
                            <!--                         Zamowienia i umowy-->
                            <xsl:when test="$hasUmowy and $hasZamowienia">
                                <fo:table table-layout="fixed" width="100%">
                                    <fo:table-column column-width="50%"/> <!-- Kolumna dla Umowy -->
                                    <fo:table-column column-width="50%"/> <!-- Kolumna dla Zamówienia -->
                                    <fo:table-body>
                                        <fo:table-row>
                                            <!-- Umowy -->
                                            <fo:table-cell xsl:use-attribute-sets="table.cell.padding">
                                                <xsl:if test="count(crd:Fa/crd:WarunkiTransakcji/crd:Umowy) > 0">
                                                    <fo:block font-size="7pt" text-align="left" space-after="2mm">
                                                        <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'contract', $labels)"/></fo:inline>
                                                    </fo:block>

                                                    <fo:table table-layout="fixed" width="100%">
                                                        <fo:table-column column-width="50%"/> <!-- Data umowy -->
                                                        <fo:table-column column-width="50%"/> <!-- Numer umowy -->
                                                        <fo:table-header>
                                                            <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                                    <fo:block><xsl:value-of select="key('kLabels', 'contractDate', $labels)"/></fo:block>
                                                                </fo:table-cell>
                                                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                                    <fo:block><xsl:value-of select="key('kLabels', 'contractNumber', $labels)"/></fo:block>
                                                                </fo:table-cell>
                                                            </fo:table-row>
                                                        </fo:table-header>
                                                        <fo:table-body>
                                                            <xsl:apply-templates select="crd:Fa/crd:WarunkiTransakcji/crd:Umowy"/>
                                                        </fo:table-body>
                                                    </fo:table>
                                                </xsl:if>
                                            </fo:table-cell>

                                            <!-- Zamówienia -->
                                            <fo:table-cell xsl:use-attribute-sets="table.cell.padding">
                                                <xsl:if test="count(crd:Fa/crd:WarunkiTransakcji/crd:Zamowienia) > 0">
                                                    <fo:block font-size="7pt" text-align="left" space-after="2mm">
                                                        <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'order', $labels)"/></fo:inline>
                                                    </fo:block>

                                                    <fo:table table-layout="fixed" width="100%">
                                                        <fo:table-column column-width="50%"/> <!-- Data zamówienia -->
                                                        <fo:table-column column-width="50%"/> <!-- Numer zamówienia -->
                                                        <fo:table-header>
                                                            <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                                    <fo:block><xsl:value-of select="key('kLabels', 'orderDate', $labels)"/></fo:block>
                                                                </fo:table-cell>
                                                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                                    <fo:block><xsl:value-of select="key('kLabels', 'orderNumber', $labels)"/></fo:block>
                                                                </fo:table-cell>
                                                            </fo:table-row>
                                                        </fo:table-header>
                                                        <fo:table-body>
                                                            <xsl:apply-templates select="crd:Fa/crd:WarunkiTransakcji/crd:Zamowienia"/>
                                                        </fo:table-body>
                                                    </fo:table>
                                                </xsl:if>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </fo:table-body>
                                </fo:table>
                            </xsl:when>
                            <!--                            Tylko umowy-->
                            <xsl:when test="$hasUmowy and not($hasZamowienia)">
                                <fo:block font-size="7pt" text-align="left" space-after="2mm">
                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'contract', $labels)"/></fo:inline>
                                </fo:block>

                                <fo:table table-layout="fixed" width="100%">
                                    <fo:table-column column-width="25%"/> <!-- Data umowy -->
                                    <fo:table-column column-width="25%"/> <!-- Numer umowy -->
                                    <fo:table-header>
                                        <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                <fo:block><xsl:value-of select="key('kLabels', 'contractDate', $labels)"/></fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                <fo:block><xsl:value-of select="key('kLabels', 'contractNumber', $labels)"/></fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </fo:table-header>
                                    <fo:table-body>
                                        <xsl:apply-templates select="crd:Fa/crd:WarunkiTransakcji/crd:Umowy"/>
                                    </fo:table-body>
                                </fo:table>
                            </xsl:when>
                            <!--                            Tylko zamowienia-->
                            <xsl:when test="$hasZamowienia and not($hasUmowy)">
                                <fo:block font-size="7pt" text-align="left" space-after="2mm">
                                    <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'orders', $labels)"/></fo:inline>
                                </fo:block>

                                <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                                    <fo:table-column column-width="25%"/> <!-- Data zamowienia -->
                                    <fo:table-column column-width="25%"/> <!-- Numer zamowienia -->
                                    <fo:table-header>
                                        <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                            <fo:table-cell
                                                    xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                <fo:block><xsl:value-of select="key('kLabels', 'orderDate', $labels)"/></fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell
                                                    xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                <fo:block><xsl:value-of select="key('kLabels', 'orderNumber', $labels)"/></fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>
                                    </fo:table-header>
                                    <fo:table-body>
                                        <xsl:apply-templates
                                                select="crd:Fa/crd:WarunkiTransakcji/crd:Zamowienia"/>
                                    </fo:table-body>
                                </fo:table>
                            </xsl:when>
                        </xsl:choose>

                        <!-- Warunki dostawy -->
                        <xsl:if test="crd:Fa/crd:WarunkiTransakcji/crd:WarunkiDostawy">
                            <fo:block font-size="7pt" text-align="left" space-after="1mm" space-before="3mm">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'deliveryConditions', $labels)"/>: </fo:inline>
                                <xsl:value-of select="crd:Fa/crd:WarunkiTransakcji/crd:WarunkiDostawy"/>
                            </fo:block>
                        </xsl:if>
                    </xsl:if>

                    <!-- Rachunki bankowe -->
                    <xsl:if test="count(crd:Fa/crd:Platnosc/crd:RachunekBankowy) > 0">
                        <!-- Blok tytułu -->
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
                        <fo:block font-size="12pt" text-align="left">
                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'bankAccountNumber', $labels)"/></fo:inline>
                        </fo:block>

                        <!-- Tabela z rachunkami bankowymi -->
                        <fo:table table-layout="fixed" width="100%">
                            <fo:table-column column-width="50%"/>
                            <fo:table-column column-width="50%"/>

                            <fo:table-body>
                                <!-- Iterujemy przez wszystkie elementy RachunekBankowy, zaczynając od pierwszego -->
                                <xsl:for-each select="crd:Fa/crd:Platnosc/crd:RachunekBankowy[position() mod 2 = 1]">
                                    <fo:table-row>
                                        <!-- Pierwsza komórka w wierszu -->
                                        <fo:table-cell>
                                            <fo:block font-size="7pt" space-after="5mm">
                                                <xsl:call-template name="renderBankAccountTable">
                                                    <xsl:with-param name="bankAccountNode" select="."/>
                                                </xsl:call-template>
                                            </fo:block>
                                        </fo:table-cell>

                                        <!-- Druga komórka, jeśli istnieje następny element -->
                                        <xsl:choose>
                                            <xsl:when test="following-sibling::crd:RachunekBankowy[1]">
                                                <fo:table-cell padding-left="6pt">
                                                    <fo:block font-size="7pt" space-after="5mm">
                                                        <xsl:call-template name="renderBankAccountTable">
                                                            <xsl:with-param name="bankAccountNode" select="following-sibling::crd:RachunekBankowy[1]"/>
                                                        </xsl:call-template>
                                                    </fo:block>
                                                </fo:table-cell>
                                            </xsl:when>
                                            <!-- Jeśli nie ma następnego elementu, wstawiamy pustą komórkę -->
                                            <xsl:otherwise>
                                                <fo:table-cell>
                                                    <fo:block/>
                                                </fo:table-cell>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </fo:table-row>
                                </xsl:for-each>
                            </fo:table-body>
                        </fo:table>
                    </xsl:if>

                    <!-- Rejestry  -->
                    <xsl:if test="count(crd:Stopka/crd:Rejestry) > 0">
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                        <fo:block>
                            <fo:block text-align="left" space-after="3mm">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'registries', $labels)"/></fo:inline>
                            </fo:block>
                            <!-- Rekord rejestru-->
                            <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                                <fo:table-column column-width="55%"/> <!-- Pełna nazwa-->
                                <fo:table-column column-width="15%"/> <!-- KRS -->
                                <fo:table-column column-width="15%"/> <!-- REGON -->
                                <fo:table-column column-width="15%"/> <!-- BDO -->
                                <fo:table-header>
                                    <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block><xsl:value-of select="key('kLabels', 'fullName', $labels)"/></fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block>KRS</fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block>REGON</fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block>BDO</fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </fo:table-header>
                                <fo:table-body>
                                    <xsl:apply-templates select="crd:Stopka/crd:Rejestry"/>
                                </fo:table-body>
                            </fo:table>
                        </fo:block>
                    </xsl:if>

                    <!-- Pozostałe informacje  -->
                    <xsl:if test="count(crd:Stopka/crd:Informacje) > 0">
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                        <fo:block>
                            <fo:block text-align="left" space-after="3mm">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'otherInfo', $labels)"/></fo:inline>
                            </fo:block>
                            <!-- Rekord pozostałych informacji -->
                            <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                                <fo:table-column column-width="100%"/> <!-- Stopka faktury-->
                                <fo:table-header>
                                    <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block><xsl:value-of select="key('kLabels', 'invoiceFooter', $labels)"/></fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </fo:table-header>
                                <fo:table-body>
                                    <xsl:apply-templates select="crd:Stopka/crd:Informacje"/>
                                </fo:table-body>
                            </fo:table>
                        </fo:block>
                    </xsl:if>

                    <xsl:if test="$issuerUser">
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                        <fo:block font-size="10pt" text-align="left">
                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'issuingPerson', $labels)"/>: </fo:inline>
                            <xsl:value-of select="$issuerUser"/>
                        </fo:block>
                    </xsl:if>

                    <!-- New multiple QR codes approach -->
                    <xsl:if test="$qrCodesCount > 0">
                        <!-- Keep text and QR codes together on the same page -->
                        <fo:block keep-together.within-page="always">
                            <fo:block font-size="12pt" text-align="left" space-before="4mm" keep-with-next.within-page="always">
                                <fo:inline font-weight="bold">
                                    <xsl:value-of select="key('kLabels', 'checkInvoiceInKsef', $labels)"/>
                                </fo:inline>
                            </fo:block>

                            <!-- Render QR Code 0 if exists -->
                            <xsl:if test="$qrCode0 and $verificationLink0">
                                <xsl:call-template name="renderQrCode">
                                    <xsl:with-param name="qrCodeImage" select="$qrCode0"/>
                                    <xsl:with-param name="qrCodeLabel" select="$qrCodeLabel0"/>
                                    <xsl:with-param name="qrCodeVerificationLinkTitle" select="$verificationLinkTitle0"/>
                                    <xsl:with-param name="qrCodeVerificationLink" select="$verificationLink0"/>
                                </xsl:call-template>
                            </xsl:if>

                            <!-- Render QR Code 1 if exists -->
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

    <xsl:template match="crd:Fa/crd:DodatkowyOpis">
        <xsl:param name="hasNrWiersza"/>
        <fo:table-row>
            <xsl:if test="$hasNrWiersza">
                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                    <fo:block>
                        <xsl:value-of select="crd:NrWiersza"/> <!-- Nr wiersza -->
                    </fo:block>
                </fo:table-cell>
            </xsl:if>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:value-of select="crd:Klucz"/> <!-- Klucz dodatkowego opisu -->
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:for-each select="tokenize(crd:Wartosc, '\\n')">
                        <fo:block>
                            <xsl:value-of select="."/>
                        </fo:block>
                    </xsl:for-each>
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>
    <xsl:template match="crd:Stopka/crd:Informacje">
        <fo:table-row>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:value-of select="crd:StopkaFaktury"/> <!-- Stopka faktury -->
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="crd:Stopka/crd:Rejestry">
        <fo:table-row>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:value-of select="crd:PelnaNazwa"/> <!-- Pelna nazwa -->
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:value-of select="crd:KRS"/> <!-- KRS  -->
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:value-of select="crd:REGON"/> <!-- REGON -->
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:value-of select="crd:BDO"/> <!--BDO -->
                </fo:block>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="crd:Fa/crd:WarunkiTransakcji/crd:Umowy">
        <fo:table-row>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:value-of select="crd:DataUmowy"/> <!-- DataUmowy -->
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" padding-left="3pt">
                <fo:block>
                    <xsl:value-of select="crd:NrUmowy"/> <!-- NrUmowy -->
                </fo:block>
            </fo:table-cell>
        </fo:table-row>

    </xsl:template>

    <xsl:template match="crd:Fa/crd:WarunkiTransakcji/crd:Zamowienia">
        <fo:table-row>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:value-of select="crd:DataZamowienia"/> <!-- DataZamowienia -->
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" padding-left="3pt">
                <fo:block>
                    <xsl:value-of select="crd:NrZamowienia"/> <!-- NrZamowienia -->
                </fo:block>
            </fo:table-cell>
        </fo:table-row>

    </xsl:template>

    <xsl:template match="crd:Podmiot1">
        <fo:table font-size="7pt" table-layout="fixed" width="100%">
            <fo:table-body>
                <xsl:if test="crd:NrEORI">
                    <fo:table-row>
                        <fo:table-cell>
                            <fo:block text-align="left" padding-bottom="3px">
                                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'eori.number', $labels)"/>: </fo:inline>
                                <xsl:value-of
                                        select="crd:NrEORI"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
                <xsl:if test="crd:PrefiksPodatnika">
                    <fo:table-row>
                        <fo:table-cell>
                            <fo:block text-align="left" padding-bottom="3px">
                                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'vatUe.prefix', $labels)"/>: </fo:inline>
                                <xsl:call-template name="mapKodKrajuToNazwa">
                                    <xsl:with-param name="kodKraju" select="crd:PrefiksPodatnika"/>
                                </xsl:call-template>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
                <fo:table-row>
                    <fo:table-cell>
                        <fo:block text-align="left" padding-bottom="3px">
                            <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'nip', $labels)"/>: </fo:inline>
                            <xsl:value-of
                                    select="crd:DaneIdentyfikacyjne/crd:NIP"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell>
                        <fo:block text-align="left">
                            <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'name', $labels)"/>: </fo:inline>
                            <xsl:value-of
                                    select="crd:DaneIdentyfikacyjne/crd:Nazwa"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell padding-top="16px">
                        <fo:block text-align="left" padding-bottom="3px">
                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'address', $labels)"/></fo:inline>
                        </fo:block>
                        <fo:block text-align="left">
                            <xsl:value-of select="crd:Adres/crd:AdresL1"/>
                            <xsl:if test="crd:Adres/crd:AdresL2">
                                <fo:inline>, </fo:inline>
                                <xsl:value-of select="crd:Adres/crd:AdresL2"/>
                            </xsl:if>
                            <xsl:if test="crd:Adres/crd:KodKraju">
                                <fo:block>
                                    <xsl:call-template name="mapKodKrajuToNazwa">
                                        <xsl:with-param name="kodKraju" select="crd:Adres/crd:KodKraju"/>
                                    </xsl:call-template>
                                </fo:block>
                            </xsl:if>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <xsl:if test="crd:DaneKontaktowe/crd:Email|crd:DaneKontaktowe/crd:Telefon">
                    <fo:table-row>
                        <fo:table-cell padding-top="16px">
                            <fo:block text-align="left" padding-bottom="3px">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'contact.data', $labels)"/></fo:inline>
                            </fo:block>
                            <xsl:if test="crd:DaneKontaktowe/crd:Email">
                                <fo:block text-align="left" padding-bottom="2px">
                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'email', $labels)"/>: </fo:inline>
                                    <xsl:value-of
                                            select="crd:DaneKontaktowe/crd:Email"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="crd:DaneKontaktowe/crd:Telefon">
                                <fo:block text-align="left" padding-bottom="2px">
                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'phone', $labels)"/>: </fo:inline>
                                    <xsl:value-of
                                            select="crd:DaneKontaktowe/crd:Telefon"/>
                                </fo:block>
                            </xsl:if>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="crd:Podmiot2">
        <fo:table font-size="7pt" table-layout="fixed" width="100%">
            <fo:table-body>
                <xsl:if test="crd:NrEORI">
                    <fo:table-row>
                        <fo:table-cell>
                            <fo:block text-align="left" padding-bottom="3px">
                                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'eori.number', $labels)"/>: </fo:inline>
                                <xsl:value-of
                                        select="crd:NrEORI"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
                <xsl:if test="crd:DaneIdentyfikacyjne/crd:NrVatUE">
                    <fo:table-row>
                        <fo:table-cell>
                            <fo:block text-align="left" padding-bottom="3px">
                                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'vatUe.number', $labels)"/>: </fo:inline>
                                <xsl:value-of select="crd:DaneIdentyfikacyjne/crd:KodUE"/>
                                <xsl:value-of select="crd:DaneIdentyfikacyjne/crd:NrVatUE"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
                <xsl:if test="crd:DaneIdentyfikacyjne/crd:NIP">
                    <fo:table-row>
                        <fo:table-cell>
                            <fo:block text-align="left" padding-bottom="3px">
                                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'nip', $labels)"/>: </fo:inline>
                                <xsl:value-of
                                        select="crd:DaneIdentyfikacyjne/crd:NIP"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
                <fo:table-row>
                    <fo:table-cell>
                        <fo:block text-align="left">
                            <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'name', $labels)"/>: </fo:inline>
                            <xsl:value-of
                                    select="crd:DaneIdentyfikacyjne/crd:Nazwa"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell padding-top="16px">
                        <fo:block text-align="left" padding-bottom="3px">
                            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'address', $labels)"/></fo:inline>
                        </fo:block>
                        <fo:block text-align="left">
                            <xsl:value-of select="crd:Adres/crd:AdresL1"/>
                            <xsl:if test="crd:Adres/crd:AdresL2">
                                <fo:inline>, </fo:inline>
                                <xsl:value-of select="crd:Adres/crd:AdresL2"/>
                            </xsl:if>
                            <xsl:if test="crd:Adres/crd:KodKraju">
                                <fo:block>
                                    <xsl:call-template name="mapKodKrajuToNazwa">
                                        <xsl:with-param name="kodKraju" select="crd:Adres/crd:KodKraju"/>
                                    </xsl:call-template>
                                </fo:block>
                            </xsl:if>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <xsl:if test="crd:DaneKontaktowe/crd:Email|crd:DaneKontaktowe/crd:Telefon|crd:NrKlienta|crd:IDNabywcy">
                    <fo:table-row>
                        <fo:table-cell padding-top="16px">
                            <fo:block text-align="left" padding-bottom="3px">
                                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'contact.data', $labels)"/></fo:inline>
                            </fo:block>
                            <xsl:if test="crd:DaneKontaktowe/crd:Email">
                                <fo:block text-align="left" padding-bottom="2px">
                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'email', $labels)"/>: </fo:inline>
                                    <xsl:value-of
                                            select="crd:DaneKontaktowe/crd:Email"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="crd:DaneKontaktowe/crd:Telefon">
                                <fo:block text-align="left" padding-bottom="2px">
                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'phone', $labels)"/>: </fo:inline>
                                    <xsl:value-of
                                            select="crd:DaneKontaktowe/crd:Telefon"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="crd:NrKlienta">
                                <fo:block text-align="left" padding-bottom="2px">
                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'customer.number', $labels)"/>: </fo:inline>
                                    <xsl:value-of select="crd:NrKlienta"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="crd:IDNabywcy">
                                <fo:block text-align="left">
                                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'buyerId', $labels)"/>: </fo:inline>
                                    <xsl:value-of select="crd:IDNabywcy"/>
                                </fo:block>
                            </xsl:if>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="crd:Podmiot3">
        <xsl:choose>
            <xsl:when test="crd:Rola = 5">
                <fo:block/>
            </xsl:when>
            <xsl:otherwise>
                <fo:block font-weight="bold" font-size="12pt" text-align="left" padding-bottom="8px" padding-top="5mm">
                    <xsl:if test="crd:Rola = '1'">
                        <xsl:value-of select="key('kLabels', 'role.factor', $labels)"/>
                    </xsl:if>
                    <xsl:if test="crd:Rola = '2'">
                        <xsl:value-of select="key('kLabels', 'role.recipient', $labels)"/>
                    </xsl:if>
                    <xsl:if test="crd:Rola = '3'">
                        <xsl:value-of select="key('kLabels', 'role.originalEntity', $labels)"/>
                    </xsl:if>
                    <xsl:if test="crd:Rola = '4'">
                        <xsl:value-of select="key('kLabels', 'role.additionalBuyer', $labels)"/>
                    </xsl:if>
                    <xsl:if test="crd:Rola = '6'">
                        <xsl:value-of select="key('kLabels', 'role.payer', $labels)"/>
                    </xsl:if>
                    <xsl:if test="crd:Rola = '7'">
                        <xsl:value-of select="key('kLabels', 'role.localGovIssuer', $labels)"/>
                    </xsl:if>
                    <xsl:if test="crd:Rola = '8'">
                        <xsl:value-of select="key('kLabels', 'role.localGovRecipient', $labels)"/>
                    </xsl:if>
                    <xsl:if test="crd:Rola = '9'">
                        <xsl:value-of select="key('kLabels', 'role.vatGroupIssuer', $labels)"/>
                    </xsl:if>
                    <xsl:if test="crd:Rola = '10'">
                        <xsl:value-of select="key('kLabels', 'role.vatGroupRecipient', $labels)"/>
                    </xsl:if>
                </fo:block>
            </xsl:otherwise>
        </xsl:choose>
        <fo:block text-align="left" padding-bottom="3px">
            <xsl:if test="crd:NrEORI">
                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'eori.number', $labels)"/>: </fo:inline>
                <xsl:value-of
                        select="crd:NrEORI"/>
            </xsl:if>
        </fo:block>
        <fo:block text-align="left" padding-bottom="3px">
            <xsl:if test="crd:DaneIdentyfikacyjne/crd:NrVatUE">
                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'vatUe.number', $labels)"/>: </fo:inline>
                <xsl:value-of select="crd:DaneIdentyfikacyjne/crd:KodUE"/>
                <xsl:value-of select="crd:DaneIdentyfikacyjne/crd:NrVatUE"/>
            </xsl:if>
            <xsl:if test="crd:DaneIdentyfikacyjne/crd:NIP">
                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'nip', $labels)"/>: </fo:inline>
                <xsl:value-of
                        select="crd:DaneIdentyfikacyjne/crd:NIP"/>
            </xsl:if>
        </fo:block>
        <fo:block text-align="left" padding-bottom="3px">
            <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'name', $labels)"/>: </fo:inline>
            <xsl:value-of
                    select="crd:DaneIdentyfikacyjne/crd:Nazwa"/>
        </fo:block>
        <xsl:if test="crd:Udzial">
            <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'share', $labels)"/>: </fo:inline>
            <xsl:value-of select="crd:Udzial"/>%
        </xsl:if>
        <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
            <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'address', $labels)"/></fo:inline>
        </fo:block>
        <fo:block text-align="left">
            <xsl:value-of select="crd:Adres/crd:AdresL1"/>
            <xsl:if test="crd:Adres/crd:AdresL2">
                <fo:inline>, </fo:inline>
                <xsl:value-of select="crd:Adres/crd:AdresL2"/>
            </xsl:if>
            <xsl:if test="crd:Adres/crd:KodKraju">
                <fo:block>
                    <xsl:call-template name="mapKodKrajuToNazwa">
                        <xsl:with-param name="kodKraju" select="crd:Adres/crd:KodKraju"/>
                    </xsl:call-template>
                </fo:block>
            </xsl:if>
        </fo:block>
        <xsl:if test="crd:DaneKontaktowe/crd:Email|crd:DaneKontaktowe/crd:Telefon">
            <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
                <fo:inline font-weight="bold"><xsl:value-of select="key('kLabels', 'contact.data', $labels)"/>
                </fo:inline>
            </fo:block>
            <xsl:if test="crd:DaneKontaktowe/crd:Email">
                <fo:block text-align="left" padding-bottom="2px">
                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'email', $labels)"/>: </fo:inline>
                    <xsl:value-of
                            select="crd:DaneKontaktowe/crd:Email"/>
                </fo:block>
            </xsl:if>
            <xsl:if test="crd:DaneKontaktowe/crd:Telefon">
                <fo:block text-align="left" padding-bottom="2px">
                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'phone', $labels)"/>: </fo:inline>
                    <xsl:value-of
                            select="crd:DaneKontaktowe/crd:Telefon"/>
                </fo:block>
            </xsl:if>
            <xsl:if test="crd:NrKlienta">
                <fo:block text-align="left" padding-bottom="2px">
                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'customer.number', $labels)"/>: </fo:inline>
                    <xsl:value-of select="crd:NrKlienta"/>
                </fo:block>
            </xsl:if>
            <xsl:if test="crd:IDNabywcy">
                <fo:block text-align="left">
                    <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'buyerId', $labels)"/>: </fo:inline>
                    <xsl:value-of select="crd:IDNabywcy"/>
                </fo:block>
            </xsl:if>
        </xsl:if>
    </xsl:template>


    <xsl:template name="renderBankAccountTable">
        <xsl:param name="bankAccountNode"/>
        <fo:table table-layout="fixed" width="100%" border-collapse="separate" padding-top="2mm">
            <fo:table-column column-width="45mm"/>
            <fo:table-column column-width="45mm"/>
            <fo:table-body>
                <xsl:if test="$bankAccountNode/crd:NrRB">
                    <fo:table-row>
                        <fo:table-cell background-color="#f5f5f5"
                                       font-weight="bold"
                                       xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'bankAccountNumber', $labels)"/></fo:block>
                        </fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>
                                <xsl:value-of select="$bankAccountNode/crd:NrRB"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
                <xsl:if test="$bankAccountNode/crd:SWIFT">
                    <fo:table-row>
                        <fo:table-cell background-color="#f5f5f5"
                                       font-weight="bold"
                                       xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'swift', $labels)"/></fo:block>
                        </fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>
                                <xsl:value-of select="$bankAccountNode/crd:SWIFT"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
                <xsl:if test="$bankAccountNode/crd:NazwaBanku">
                    <fo:table-row>
                        <fo:table-cell background-color="#f5f5f5"
                                       font-weight="bold"
                                       xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'bankName', $labels)"/></fo:block>
                        </fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>
                                <xsl:value-of select="$bankAccountNode/crd:NazwaBanku"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
                <xsl:if test="$bankAccountNode/crd:OpisRachunku">
                    <fo:table-row>
                        <fo:table-cell background-color="#f5f5f5"
                                       font-weight="bold"
                                       xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block><xsl:value-of select="key('kLabels', 'accountDescriptionLabel', $labels)"/></fo:block>
                        </fo:table-cell>
                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                            <fo:block>
                                <xsl:value-of select="$bankAccountNode/crd:OpisRachunku"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template name="DaneFaKorygowanejTemplate">
        <xsl:param name="faktura"/>
        <xsl:param name="numer"/>
        <xsl:param name="pokazNagłówek"/>
        
        <xsl:if test="$pokazNagłówek">
            <fo:block text-align="left" font-weight="bold" font-size="10pt" space-after="3mm">
                <xsl:value-of select="key('kLabels', 'correctedInvoice.identificationData', $labels)"/><xsl:text> </xsl:text><xsl:value-of select="$numer"/>
            </fo:block>
        </xsl:if>

        <xsl:if test="$faktura/crd:DataWystFaKorygowanej">
            <fo:block text-align="left" space-after="1mm">
                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'correctedInvoice.issueDate', $labels)"/>: </fo:inline>
                <xsl:value-of select="$faktura/crd:DataWystFaKorygowanej"/>
            </fo:block>
        </xsl:if>

        <xsl:if test="$faktura/crd:NrFaKorygowanej">
            <fo:block text-align="left" space-after="1mm">
                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'correctedInvoiceNumber', $labels)"/>: </fo:inline>
                <xsl:value-of select="$faktura/crd:NrFaKorygowanej"/>
            </fo:block>
        </xsl:if>

        <xsl:if test="$faktura/crd:NrKSeFFaKorygowanej">
            <fo:block text-align="left">
                <fo:inline font-weight="600"><xsl:value-of select="key('kLabels', 'correctedInvoice.ksefNumber', $labels)"/>: </fo:inline>
                <xsl:value-of select="$faktura/crd:NrKSeFFaKorygowanej"/>
            </fo:block>
        </xsl:if>
    </xsl:template>


</xsl:stylesheet>
