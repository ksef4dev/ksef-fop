<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:crd="http://crd.gov.pl/wzor/2023/06/29/12648/">
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
                            Wytworzona w:
                            <xsl:value-of select="crd:Naglowek/crd:SystemInfo"/>
                        </fo:block>
                    </fo:static-content>
                </xsl:if>
                <!-- Faktura -->
                <fo:flow flow-name="xsl-region-body" color="#343a40">
                    <!-- Tytuł strony -->
                    <fo:block font-size="20pt" font-weight="bold" text-align="left">
                        <fo:external-graphic
                                content-width="80pt"
                                content-height="80pt"
                                src="url('data:image/png;base64,{$logo}')"/>
                    </fo:block>
                    <!-- Numer faktury -->
                    <fo:block font-size="9pt" text-align="right" padding-top="-5mm">
                        Numer faktury
                    </fo:block>
                    <fo:block font-size="16pt" text-align="right" space-after="2mm">
                        <fo:inline font-weight="bold">
                            <xsl:value-of select="crd:Fa/crd:P_2"/>
                        </fo:inline>
                    </fo:block>
                    <xsl:if test="$duplicateDate">
                        <fo:block font-size="10pt" color="grey" text-align="right" space-after="2mm">
                            <xsl:text>Duplikat z dnia </xsl:text>
                            <xsl:value-of select="$duplicateDate"/>
                        </fo:block>
                    </xsl:if>

                    <!-- Typ faktury -->
                    <fo:block font-size="9pt" text-align="right" space-after="2mm">
                        <xsl:if test="crd:Fa/crd:RodzajFaktury = 'VAT'">
                            Faktura podstawowa
                        </xsl:if>
                        <xsl:if test="crd:Fa/crd:RodzajFaktury = 'KOR'">
                            Faktura korygująca
                        </xsl:if>
                        <xsl:if test="crd:Fa/crd:RodzajFaktury = 'ZAL'">
                            Faktura zaliczkowa
                        </xsl:if>
                        <xsl:if test="crd:Fa/crd:RodzajFaktury = 'ROZ'">
                            Faktura rozliczeniowa
                        </xsl:if>
                        <xsl:if test="crd:Fa/crd:RodzajFaktury = 'UPR'">
                            Faktura uproszczona
                        </xsl:if>
                        <xsl:if test="crd:Fa/crd:RodzajFaktury = 'KOR_ZAL'">
                            Faktura korygująca zaliczkową
                        </xsl:if>
                        <xsl:if test="crd:Fa/crd:RodzajFaktury = 'KOR_ROZ'">
                            Faktura rozliczeniowa korygująca
                        </xsl:if>
                    </fo:block>
                    <!-- Numer KSeF-->
                    <xsl:if test="$nrKsef">
                        <fo:block font-size="9pt" text-align="right" space-after="5mm">
                            <fo:inline font-weight="bold">Numer KSeF:</fo:inline>
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
                            <fo:inline font-weight="bold">Dane faktury korygowanej</fo:inline>
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
                                <fo:inline font-weight="bold">Sprzedawca</fo:inline>
                            </fo:block>

                            <fo:block text-align="left" >
                                <xsl:if test="crd:Podmiot1/crd:NrEORI" >
                                    <fo:block font-size="7pt">
                                        <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
                                            <fo:inline font-weight="bold">Dane identyfikacyjne
                                            </fo:inline>
                                        </fo:block>
                                        <fo:inline font-weight="600" font-size="7pt">Numer EORI: </fo:inline>
                                        <xsl:value-of select="crd:Podmiot1/crd:NrEORI"/>
                                    </fo:block>
                                </xsl:if>
                            </fo:block>
                            <xsl:if test="crd:Podmiot1/crd:DaneKontaktowe/crd:Email|crd:DaneKontaktowe/crd:Telefon">
                                <fo:block text-align="left" padding-bottom="3px" font-size="7pt" padding-top="8px">
                                    <fo:inline font-weight="bold">Dane kontaktowe
                                    </fo:inline>
                                </fo:block>
                                <xsl:if test="crd:Podmiot1/crd:DaneKontaktowe/crd:Email">
                                    <fo:block text-align="left" font-size="7pt" padding-bottom="2px">
                                        <fo:inline font-weight="600">E-mail: </fo:inline>
                                        <xsl:value-of
                                                select="crd:Podmiot1/crd:DaneKontaktowe/crd:Email"/>
                                    </fo:block>
                                </xsl:if>
                                <xsl:if test="crd:Podmiot1/crd:DaneKontaktowe/crd:Telefon">
                                    <fo:block text-align="left" font-size="7pt" padding-bottom="8px">
                                        <fo:inline font-weight="600">Tel.: </fo:inline>
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
                                                <fo:inline font-weight="bold">Treść korygowana</fo:inline>
                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell padding-bottom="8px">
                                            <fo:block>

                                            </fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell padding-bottom="8px">
                                            <fo:block font-size="9pt" text-align="left">
                                                <fo:inline font-weight="bold">Treść korygująca</fo:inline>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>

                                    <fo:table-row>
                                        <!-- Treść korygowanay -->
                                        <fo:table-cell>
                                            <xsl:if test="crd:Fa/crd:Podmiot1K/crd:PrefiksPodatnika">
                                                <fo:block text-align="left" padding-bottom="3px">
                                                    <fo:inline font-weight="600">Prefiks VAT: </fo:inline>
                                                    <xsl:call-template name="mapKodKrajuToNazwa">
                                                        <xsl:with-param name="kodKraju" select="crd:Fa/crd:Podmiot1K/crd:PrefiksPodatnika"/>
                                                    </xsl:call-template>
                                                </fo:block>
                                            </xsl:if>
                                            <fo:block text-align="left" padding-bottom="3px" font-size="7pt">
                                                <xsl:if test="crd:Fa/crd:Podmiot1K/crd:DaneIdentyfikacyjne/crd:NrVatUE">
                                                    <fo:inline font-weight="600">Numer Vat-UE: </fo:inline>
                                                    <xsl:value-of select="crd:Fa/crd:Podmiot1K/crd:DaneIdentyfikacyjne/crd:KodUE"/>
                                                    <xsl:value-of select="crd:Fa/crd:Podmiot1K/crd:DaneIdentyfikacyjne/crd:NrVatUE"/>
                                                </xsl:if>
                                                <xsl:if test="crd:Fa/crd:Podmiot1K/crd:DaneIdentyfikacyjne/crd:NIP">
                                                    <fo:inline font-weight="600">NIP: </fo:inline>
                                                    <xsl:value-of
                                                            select="crd:Fa/crd:Podmiot1K/crd:DaneIdentyfikacyjne/crd:NIP"/>
                                                </xsl:if>
                                            </fo:block>
                                            <fo:block text-align="left" padding-bottom="3px">
                                                <fo:inline font-weight="600">Nazwa: </fo:inline>
                                                <xsl:value-of
                                                        select="crd:Fa/crd:Podmiot1K/crd:DaneIdentyfikacyjne/crd:Nazwa"/>
                                            </fo:block>
                                            <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
                                                <fo:inline font-weight="bold">Adres</fo:inline>
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
                                                    <fo:inline font-weight="600">Prefiks VAT: </fo:inline>
                                                    <xsl:call-template name="mapKodKrajuToNazwa">
                                                        <xsl:with-param name="kodKraju" select="crd:Podmiot1/crd:PrefiksPodatnika"/>
                                                    </xsl:call-template>
                                                </fo:block>
                                            </xsl:if>
                                            <fo:block text-align="left" padding-bottom="3px" font-size="7pt">
                                                <xsl:if test="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:NrVatUE">
                                                    <fo:inline font-weight="600">Numer Vat-UE: </fo:inline>
                                                    <xsl:value-of select="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:KodUE"/>
                                                    <xsl:value-of select="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:NrVatUE"/>
                                                </xsl:if>
                                                <xsl:if test="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:NIP">
                                                    <fo:inline font-weight="600">NIP: </fo:inline>
                                                    <xsl:value-of
                                                            select="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:NIP"/>
                                                </xsl:if>
                                            </fo:block>
                                            <fo:block text-align="left" padding-bottom="3px">
                                                <fo:inline font-weight="600">Nazwa: </fo:inline>
                                                <xsl:value-of
                                                        select="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:Nazwa"/>
                                            </fo:block>
                                            <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
                                                <fo:inline font-weight="bold">Adres</fo:inline>
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
                                        <xsl:when test="crd:Fa/crd:Podmiot1K">
                                            <fo:block font-size="12pt" text-align="left">
                                                <fo:inline font-weight="bold">Nabywca</fo:inline>
                                            </fo:block>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <fo:block font-size="12pt" text-align="left">
                                                <fo:inline font-weight="bold">Sprzedawca</fo:inline>
                                            </fo:block>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:table-cell>
                                <fo:table-cell padding-bottom="8px">
                                    <xsl:choose>
                                        <xsl:when test="crd:Podmiot3 and crd:Podmiot3/crd:Rola = 5">
                                            <fo:block font-size="12pt" text-align="left">
                                                <fo:inline font-weight="bold">Wystawca</fo:inline>
                                            </fo:block>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <fo:block/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:table-cell>
                                <fo:table-cell padding-bottom="8px">
                                    <xsl:choose>
                                        <xsl:when test="crd:Fa/crd:Podmiot1K">
                                            <fo:block>
                                            </fo:block>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <fo:block font-size="12pt" text-align="left">
                                                <fo:inline font-weight="bold">Nabywca</fo:inline>
                                            </fo:block>
                                        </xsl:otherwise>
                                    </xsl:choose>

                                </fo:table-cell>
                            </fo:table-row>

                            <!-- Dane sprzedawcy -->
                            <fo:table-row>
                                <fo:table-cell>
                                    <xsl:choose>
                                        <xsl:when test="crd:Fa/crd:Podmiot1K">
                                            <xsl:apply-templates select="crd:Podmiot2"/>
                                        </xsl:when>
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
                                        <xsl:when test="crd:Fa/crd:Podmiot1K">
                                            <fo:block></fo:block>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:apply-templates select="crd:Podmiot2"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>


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
                        <fo:inline font-weight="bold">Szczegóły</fo:inline>
                    </fo:block>
                    <fo:table space-after="5mm" table-layout="fixed" width="100%">
                        <fo:table-column column-width="50%" />
                        <fo:table-column column-width="50%" />
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell padding-right="6pt">
                                    <xsl:if test="crd:Fa/crd:P_1">
                                        <fo:block font-size="8pt" text-align="left">
                                            <fo:inline font-weight="bold">Data wystawienia: </fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:P_1"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:P_6">
                                        <fo:block font-size="8pt" text-align="left">
                                            <xsl:choose>
                                                <xsl:when test="crd:Fa/crd:RodzajFaktury = 'ZAL'">
                                                    <fo:inline font-weight="bold">Data otrzymania zapłaty:
                                                    </fo:inline>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <fo:inline font-weight="bold">Data dokonania lub zakończenia dostawy towarów
                                                        lub wykonania usługi:
                                                    </fo:inline>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <xsl:value-of select="crd:Fa/crd:P_6"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:PrzyczynaKorekty">
                                        <fo:block font-size="8pt" text-align="left">
                                            <fo:inline font-weight="bold">Przyczyna korekty:
                                            </fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:PrzyczynaKorekty"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:TypKorekty">
                                        <fo:block font-size="8pt" text-align="left">
                                            <fo:inline font-weight="bold">Typ korekty:</fo:inline>
                                            <xsl:choose>
                                                <xsl:when test="crd:Fa/crd:TypKorekty = 1">
                                                    <xsl:text>Korekta skutkująca w dacie ujęcia faktury pierwotnej</xsl:text>
                                                </xsl:when>
                                                <xsl:when test="crd:Fa/crd:TypKorekty = 2">
                                                    <xsl:text>Korekta skutkująca w dacie wystawienia faktury korygującej</xsl:text>
                                                </xsl:when>
                                                <xsl:when test="crd:Fa/crd:TypKorekty = 3">
                                                    <xsl:text>Korekta skutkująca w dacie innej, w tym gdy dla różnych pozycji faktury korygującej daty te są różne</xsl:text>
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
                                            <fo:inline font-weight="bold">Miejsce wystawienia: </fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:P_1M"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:KursWalutyZ">
                                        <fo:block text-align="left" font-size="8pt">
                                            <fo:inline font-weight="bold">Kurs waluty: </fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:KursWalutyZ"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:FaWiersz[1]/crd:KursWaluty">
                                        <fo:block text-align="left" font-size="8pt">
                                            <fo:inline font-weight="bold">Kurs waluty: </fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:FaWiersz[1]/crd:KursWaluty"/>
                                        </fo:block>
                                    </xsl:if>
                                    <xsl:choose>
                                        <xsl:when test="$currencyDate">
                                            <fo:block text-align="left" font-size="8pt">
                                                <fo:inline font-weight="bold">Data kursu waluty: </fo:inline>
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
                                            <fo:block>Numery wcześniejszych faktur zaliczkowych</fo:block>
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
                                <fo:inline font-weight="bold">Dodatkowy opis</fo:inline>
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
                                                <fo:block>Nr Wiersza</fo:block>
                                            </fo:table-cell>
                                        </xsl:if>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block>Rodzaj informacji</fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block>Treść informacji</fo:block>
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

                    <!-- Linia oddzielająca -->
                   <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                    <fo:block text-align="left" space-after="2mm">
                        <fo:inline font-weight="bold" font-size="12pt">
                            <xsl:choose>
                                <xsl:when test="crd:Fa/crd:RodzajFaktury = 'ZAL'">Zamówienie</xsl:when>
                                <xsl:otherwise>Pozycje</xsl:otherwise>
                            </xsl:choose>
                        </fo:inline>
                    </fo:block>
                    <xsl:if test="crd:Fa/crd:KodWaluty != 'PLN'">
                        <fo:block font-size="8pt" font-weight="bold" text-align="left" space-after="3mm">
                            <fo:inline>Faktura wystawiona w walucie </fo:inline>
                            <xsl:value-of select="crd:Fa/crd:KodWaluty"/>
                        </fo:block>
                    </xsl:if>
                    <fo:block>
                        <xsl:choose>
                            <xsl:when test="crd:Fa/crd:FaWiersz[crd:StanPrzed = 1] and crd:Fa/crd:FaWiersz[not(crd:StanPrzed)]">
                                <fo:block text-align="left" space-after="1mm">
                                    <fo:inline font-weight="bold" font-size="10pt">Pozycje na fakturze przed korektą</fo:inline>
                                </fo:block>
                                <!-- Pozycje na FV-->
                                <xsl:call-template name="positionsTable">
                                    <xsl:with-param name="faWiersz" select="crd:Fa/crd:FaWiersz[crd:StanPrzed = 1]"/>
                                </xsl:call-template>

                                <!-- Add differences table when showCorrectionDifferences is true -->
                                <xsl:if test="$showCorrectionDifferences">
                                    <fo:block text-align="left" space-after="1mm">
                                        <fo:inline font-weight="bold" font-size="10pt">Różnica</fo:inline>
                                    </fo:block>
                                    <xsl:call-template name="differencesTable">
                                        <xsl:with-param name="faWierszBefore" select="crd:Fa/crd:FaWiersz[crd:StanPrzed = 1]"/>
                                        <xsl:with-param name="faWierszAfter" select="crd:Fa/crd:FaWiersz[not(crd:StanPrzed)]"/>
                                    </xsl:call-template>
                                </xsl:if>

                                <!-- Subheader and Table for "Pozycje na fakturze po korekcie" -->
                                <fo:block text-align="left" space-after="1mm">
                                    <fo:inline font-weight="bold" font-size="10pt">Pozycje na fakturze po korekcie</fo:inline>
                                </fo:block>
                                <xsl:call-template name="positionsTable">
                                    <xsl:with-param name="faWiersz" select="crd:Fa/crd:FaWiersz[not(crd:StanPrzed)]"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:when test="crd:Fa/crd:FaWiersz[crd:StanPrzed = 1] and not(crd:Fa/crd:FaWiersz[not(crd:StanPrzed)])">
                                <fo:block text-align="left" space-after="1mm">
                                    <fo:inline font-weight="bold" font-size="10pt">Pozycje na fakturze przed korektą</fo:inline>
                                </fo:block>
                                <!-- Tylko pozycje przed korektą -->
                                <xsl:call-template name="positionsTable">
                                    <xsl:with-param name="faWiersz" select="crd:Fa/crd:FaWiersz[crd:StanPrzed = 1]"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:choose>
                                    <xsl:when test="crd:Fa/crd:RodzajFaktury = 'ZAL' or crd:Fa/crd:RodzajFaktury = 'KOR_ZAL'">
                                        <xsl:call-template name="zamowienieTable">
                                            <xsl:with-param name="zamowienieWiersz" select="crd:Fa/crd:Zamowienie/crd:ZamowienieWiersz"/>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:call-template name="positionsTable">
                                            <xsl:with-param name="faWiersz" select="crd:Fa/crd:FaWiersz"/>
                                        </xsl:call-template>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>

                    <!-- Kwota należności ogółem -->

                    <!-- Conditional block for displaying correction amounts only when RodzajFaktury = 'KOR' -->
                    <xsl:if test="crd:Fa/crd:RodzajFaktury = 'KOR'">
                        <!-- Optional block for Kwota brutto przed korektą -->
                        <xsl:if test="boolean(sum(crd:Fa/crd:FaWiersz[crd:StanPrzed = 1]/crd:P_11A))">
                            <fo:block color="#6c757d" font-size="8pt" text-align="right" space-before="2mm">
                                <fo:inline font-weight="bold">Kwota brutto przed korektą: </fo:inline>
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
                                <fo:inline font-weight="bold">Kwota brutto po korekcie: </fo:inline>
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
                                <fo:inline font-weight="bold">Otrzymana kwota zapłaty (zaliczki): </fo:inline>
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
                                <fo:inline font-weight="bold">Kwota pozostała do zaplaty: </fo:inline>
                                <fo:inline>
                                    <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_15), '#,##0.00'), ',.', ' ,')"/>
                                    <xsl:text> </xsl:text>
                                    <fo:inline>
                                        <xsl:value-of select="crd:Fa/crd:KodWaluty"/>
                                    </fo:inline>
                                </fo:inline>
                            </fo:block>
                        </xsl:when>
                        <xsl:otherwise>
                            <fo:block color="#343a40" font-size="10pt" text-align="right" space-before="3mm">
                                <fo:inline font-weight="bold">Kwota należności ogółem: </fo:inline>
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
                            <fo:inline font-weight="bold">Podsumowanie stawek podatku</fo:inline>
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
                                        <fo:block>Stawka podatku</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell
                                            xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block>Kwota netto</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell
                                            xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block>Kwota podatku</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell
                                            xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block>Kwota brutto</fo:block>
                                    </fo:table-cell>
                                    <xsl:if test="crd:Fa/crd:P_14_1W | crd:Fa/crd:P_14_2W | crd:Fa/crd:P_14_3W | crd:Fa/crd:P_14_4W">
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block>Kwota podatku PLN</fo:block>
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
                                            <fo:block text-align="center">Brak danych do wyświetlenia</fo:block>
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
                                                <xsl:text>np z wyłączeniem art. 100 ust 1 pkt ustawy</xsl:text>
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
                                                <xsl:text>0% - krajowe</xsl:text>
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
                                                <xsl:text>0% - WDT</xsl:text>
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
                                                <xsl:text>0% - eksport</xsl:text>
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
                                                <xsl:text>zwolnione z opodatkowania</xsl:text>
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
                                                <xsl:text>np z wyłączeniem art. 100 ust 1 pkt 4 ustawy</xsl:text>
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
                                                <xsl:text>np na podstawie art. 100 ust 1 pkt 4 ustawy</xsl:text>
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
                                                <xsl:text>odwrotne obciążenie</xsl:text>
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
                                                <xsl:text>marża</xsl:text>
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

                    <xsl:if test="crd:Fa/crd:Adnotacje/crd:P_18 = 1">

                        <!-- Adnotacje -->
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                        <fo:block font-size="12pt" text-align="left" space-after="5mm">
                            <fo:inline font-weight="bold">Adnotacje</fo:inline>
                        </fo:block>

                        <xsl:if test="crd:Fa/crd:Adnotacje/crd:P_18 = 1">
                            <fo:block font-size="7pt" text-align="left" space-after="1mm">
                                <fo:inline font-weight="bold">Odwrotne obciążenie</fo:inline>
                            </fo:block>
                        </xsl:if>

                    </xsl:if>
                    <!-- Płatność -->
                   <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                    <fo:block font-size="12pt" text-align="left" space-after="2mm">
                        <fo:inline font-weight="bold">Płatność</fo:inline>
                    </fo:block>

                    <!-- Informacja o płatności -->
                    <xsl:if test="crd:Fa/crd:Platnosc/crd:Zaplacono = 1">
                        <fo:block font-size="7pt" text-align="left" space-after="1mm">
                            <fo:inline font-weight="bold">Informacja o płatności:</fo:inline>
                            Zapłacono
                        </fo:block>
                    </xsl:if>

                    <xsl:if test="crd:Fa/crd:Platnosc/crd:Zaplacono != 1 and crd:Fa/crd:Platnosc/crd:ZaplataCzesciowa[1]/crd:KwotaZaplatyCzesciowej > 0">
                        <fo:block font-size="7pt" text-align="left" space-after="1mm">
                            <fo:inline font-weight="bold">Informacja o płatności:</fo:inline>
                            Zapłata częściowa
                        </fo:block>
                    </xsl:if>

                    <!-- DataZaplaty -->
                    <xsl:if test="crd:Fa/crd:Platnosc/crd:DataZaplaty">
                        <fo:block font-size="7pt" text-align="left" space-after="1mm">
                            <fo:inline font-weight="bold">Data zapłaty: </fo:inline>
                            <xsl:value-of select="crd:Fa/crd:Platnosc/crd:DataZaplaty"/>
                        </fo:block>
                    </xsl:if>

                    <!-- Forma płatności -->
                    <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci">
                        <fo:block font-size="7pt" text-align="left" space-after="1mm">
                            <fo:inline font-weight="bold">Forma płatności:</fo:inline>
                            <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '1'">
                                Gotówka
                            </xsl:if>
                            <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '2'">
                                Karta
                            </xsl:if>
                            <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '3'">
                                Bon
                            </xsl:if>
                            <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '4'">
                                Czek
                            </xsl:if>
                            <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '5'">
                                Kredyt
                            </xsl:if>
                            <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '6'">
                                Przelew
                            </xsl:if>
                            <xsl:if test="crd:Fa/crd:Platnosc/crd:FormaPlatnosci = '7'">
                                Mobilna
                            </xsl:if>
                        </fo:block>
                    </xsl:if>
                    <!-- Termin płatności -->

                    <xsl:if test="crd:Fa/crd:Platnosc/crd:TerminPlatnosci/crd:Termin">
                        <fo:block font-size="7pt" text-align="left" space-after="1mm">
                            <fo:inline font-weight="bold">Termin płatności: </fo:inline>
                            <xsl:value-of select="crd:Fa/crd:Platnosc/crd:TerminPlatnosci/crd:Termin"/>
                        </fo:block>
                    </xsl:if>

                    <xsl:if test="crd:Fa/crd:Platnosc/crd:TerminPlatnosci/crd:TerminOpis">
                        <fo:block font-size="7pt" text-align="left" space-after="1mm">
                            <fo:inline font-weight="bold">Opis płatności: </fo:inline>
                            <xsl:value-of select="crd:Fa/crd:Platnosc/crd:TerminPlatnosci/crd:TerminOpis"/>
                        </fo:block>
                    </xsl:if>

                    <xsl:if test="
                    crd:Fa/crd:Platnosc/crd:Zaplacono != 1
                    and
                    crd:Fa/crd:Platnosc/crd:ZaplataCzesciowa[1]/crd:KwotaZaplatyCzesciowej > 0">
                        <fo:block font-size="7pt" text-align="left" space-after="1mm">
                            <fo:inline font-weight="bold">Opłacono: </fo:inline>
                            <xsl:value-of select="translate(format-number(number(crd:Fa/crd:Platnosc/crd:ZaplataCzesciowa[1]/crd:KwotaZaplatyCzesciowej),  '#,##0.00'), ',.', ' ,')"/>
                            <xsl:text> </xsl:text> <!-- Dodanie spacji -->
                            <fo:inline>
                                <xsl:value-of select="crd:Fa/crd:KodWaluty"/>
                            </fo:inline>
                        </fo:block>
                    </xsl:if>

                    <xsl:if test="crd:Fa/crd:Platnosc/crd:Zaplacono != 1 and
                    crd:Fa/crd:Platnosc/crd:ZaplataCzesciowa and
                     number(crd:Fa/crd:P_15) - number(crd:Fa/crd:Platnosc/crd:ZaplataCzesciowa[1]/crd:KwotaZaplatyCzesciowej) >= 0">
                        <fo:block font-size="7pt" text-align="left" space-after="1mm">
                            <fo:inline font-weight="bold">Pozostało do zapłaty: </fo:inline>
                            <xsl:value-of
                                    select="translate(format-number(number(crd:Fa/crd:P_15) - number(crd:Fa/crd:Platnosc/crd:ZaplataCzesciowa[1]/crd:KwotaZaplatyCzesciowej), '#,##0.00'), ',.', ' ,')"/>
                            <xsl:text> </xsl:text> <!-- Dodanie spacji -->
                            <fo:inline>
                                <xsl:value-of select="crd:Fa/crd:KodWaluty"/>
                            </fo:inline>
                        </fo:block>
                    </xsl:if>

                    <xsl:if test="crd:Fa/crd:WarunkiTransakcji">
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                        <fo:block font-size="12pt" text-align="left" space-after="4mm">
                            <fo:inline font-weight="bold">Warunki transakcji</fo:inline>
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
                                                        <fo:inline font-weight="bold">Umowa</fo:inline>
                                                    </fo:block>

                                                    <fo:table table-layout="fixed" width="100%">
                                                        <fo:table-column column-width="50%"/> <!-- Data umowy -->
                                                        <fo:table-column column-width="50%"/> <!-- Numer umowy -->
                                                        <fo:table-header>
                                                            <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                                    <fo:block>Data umowy</fo:block>
                                                                </fo:table-cell>
                                                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                                    <fo:block>Numer umowy</fo:block>
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
                                                        <fo:inline font-weight="bold">Zamówienie</fo:inline>
                                                    </fo:block>

                                                    <fo:table table-layout="fixed" width="100%">
                                                        <fo:table-column column-width="50%"/> <!-- Data zamówienia -->
                                                        <fo:table-column column-width="50%"/> <!-- Numer zamówienia -->
                                                        <fo:table-header>
                                                            <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                                    <fo:block>Data zamówienia</fo:block>
                                                                </fo:table-cell>
                                                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                                    <fo:block>Numer zamówienia</fo:block>
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
                                    <fo:inline font-weight="bold">Umowa</fo:inline>
                                </fo:block>

                                <fo:table table-layout="fixed" width="100%">
                                    <fo:table-column column-width="25%"/> <!-- Data umowy -->
                                    <fo:table-column column-width="25%"/> <!-- Numer umowy -->
                                    <fo:table-header>
                                        <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                <fo:block>Data umowy</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                <fo:block>Numer umowy</fo:block>
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
                                    <fo:inline font-weight="bold">Zamowienia</fo:inline>
                                </fo:block>

                                <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                                    <fo:table-column column-width="25%"/> <!-- Data zamowienia -->
                                    <fo:table-column column-width="25%"/> <!-- Numer zamowienia -->
                                    <fo:table-header>
                                        <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                            <fo:table-cell
                                                    xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                <fo:block>Data zamówienia</fo:block>
                                            </fo:table-cell>
                                            <fo:table-cell
                                                    xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                                <fo:block>Numer zamówienia</fo:block>
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
                                <fo:inline font-weight="bold">Warunki dostawy towarów: </fo:inline>
                                <xsl:value-of select="crd:Fa/crd:WarunkiTransakcji/crd:WarunkiDostawy"/>
                            </fo:block>
                        </xsl:if>
                    </xsl:if>

                    <!--                        -->
                    <!--                        <fo:table table-layout="fixed" width="100%" border-collapse="separate">-->
                    <!--                            <fo:table-column column-width="50%"/> &lt;!&ndash; Pierwsza kolumna &ndash;&gt;-->
                    <!--                            <fo:table-column column-width="50%"/> &lt;!&ndash; Druga kolumna &ndash;&gt;-->
                    <!--                            <fo:table-body>-->
                    <!--                                <fo:table-row>-->
                    <!--                                        &lt;!&ndash; Jeśli są i Umowy, i Zamówienia, wyświetl w dwóch kolumnach &ndash;&gt;-->
                    <!--                                        <xsl:when test="$hasUmowy and $hasZamowienia">-->
                    <!--                                            &lt;!&ndash; Umowy &ndash;&gt;-->
                    <!--                                            <fo:table-cell xsl:use-attribute-sets="tableBorder table.cell.padding">-->
                    <!--                                                <fo:block font-size="7pt" text-align="left" space-after="2mm">-->
                    <!--                                                    <fo:inline font-weight="bold">Umowa</fo:inline>-->
                    <!--                                                </fo:block>-->
                    <!--                                                <fo:table table-layout="fixed" width="100%">-->
                    <!--                                                    <fo:table-column column-width="50%"/>-->
                    <!--                                                    <fo:table-column column-width="50%"/>-->
                    <!--                                                    <fo:table-header>-->
                    <!--                                                        <fo:table-row background-color="#f5f5f5" font-weight="bold">-->
                    <!--                                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">-->
                    <!--                                                                <fo:block>Data umowy</fo:block>-->
                    <!--                                                            </fo:table-cell>-->
                    <!--                                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">-->
                    <!--                                                                <fo:block>Numer umowy</fo:block>-->
                    <!--                                                            </fo:table-cell>-->
                    <!--                                                        </fo:table-row>-->
                    <!--                                                    </fo:table-header>-->
                    <!--                                                    <fo:table-body>-->
                    <!--                                                        <xsl:apply-templates select="crd:Fa/crd:WarunkiTransakcji/crd:Umowy"/>-->
                    <!--                                                    </fo:table-body>-->
                    <!--                                                </fo:table>-->
                    <!--                                            </fo:table-cell>-->

                    <!--                                            &lt;!&ndash; Zamówienia &ndash;&gt;-->
                    <!--                                            <fo:table-cell xsl:use-attribute-sets="tableBorder table.cell.padding">-->
                    <!--                                                <fo:block font-size="7pt" text-align="left" space-after="2mm">-->
                    <!--                                                    <fo:inline font-weight="bold">Zamówienie</fo:inline>-->
                    <!--                                                </fo:block>-->
                    <!--                                                <fo:table table-layout="fixed" width="100%">-->
                    <!--                                                    <fo:table-column column-width="50%"/>-->
                    <!--                                                    <fo:table-column column-width="50%"/>-->
                    <!--                                                    <fo:table-header>-->
                    <!--                                                        <fo:table-row background-color="#f5f5f5" font-weight="bold">-->
                    <!--                                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">-->
                    <!--                                                                <fo:block>Data zamówienia</fo:block>-->
                    <!--                                                            </fo:table-cell>-->
                    <!--                                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">-->
                    <!--                                                                <fo:block>Numer zamówienia</fo:block>-->
                    <!--                                                            </fo:table-cell>-->
                    <!--                                                        </fo:table-row>-->
                    <!--                                                    </fo:table-header>-->
                    <!--                                                    <fo:table-body>-->
                    <!--                                                        <xsl:apply-templates select="crd:Fa/crd:WarunkiTransakcji/crd:Zamowienia"/>-->
                    <!--                                                    </fo:table-body>-->
                    <!--                                                </fo:table>-->
                    <!--                                            </fo:table-cell>-->
                    <!--                                        </xsl:when>-->

                    <!--                                        &lt;!&ndash; Jeśli istnieją tylko Umowy &ndash;&gt;-->
                    <!--                                        <xsl:when test="$hasUmowy">-->
                    <!--                                            <fo:table-cell xsl:use-attribute-sets="tableBorder table.cell.padding" number-columns-spanned="2">-->
                    <!--                                                <fo:block font-size="7pt" text-align="left" space-after="2mm">-->
                    <!--                                                    <fo:inline font-weight="bold">Umowa</fo:inline>-->
                    <!--                                                </fo:block>-->
                    <!--                                                <fo:table table-layout="fixed" width="100%">-->
                    <!--                                                    <fo:table-column column-width="50%"/>-->
                    <!--                                                    <fo:table-column column-width="50%"/>-->
                    <!--                                                    <fo:table-header>-->
                    <!--                                                        <fo:table-row background-color="#f5f5f5" font-weight="bold">-->
                    <!--                                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">-->
                    <!--                                                                <fo:block>Data umowy</fo:block>-->
                    <!--                                                            </fo:table-cell>-->
                    <!--                                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">-->
                    <!--                                                                <fo:block>Numer umowy</fo:block>-->
                    <!--                                                            </fo:table-cell>-->
                    <!--                                                        </fo:table-row>-->
                    <!--                                                    </fo:table-header>-->
                    <!--                                                    <fo:table-body>-->
                    <!--                                                        <xsl:apply-templates select="crd:Fa/crd:WarunkiTransakcji/crd:Umowy"/>-->
                    <!--                                                    </fo:table-body>-->
                    <!--                                                </fo:table>-->
                    <!--                                            </fo:table-cell>-->
                    <!--                                        </xsl:when>-->

                    <!--                                        &lt;!&ndash; Jeśli istnieją tylko Zamówienia &ndash;&gt;-->
                    <!--                                        <xsl:when test="$hasZamowienia">-->
                    <!--                                            <fo:table-cell xsl:use-attribute-sets="tableBorder table.cell.padding" number-columns-spanned="2">-->
                    <!--                                                <fo:block font-size="7pt" text-align="left" space-after="2mm">-->
                    <!--                                                    <fo:inline font-weight="bold">Zamówienie</fo:inline>-->
                    <!--                                                </fo:block>-->
                    <!--                                                <fo:table table-layout="fixed" width="100%">-->
                    <!--                                                    <fo:table-column column-width="50%"/>-->
                    <!--                                                    <fo:table-column column-width="50%"/>-->
                    <!--                                                    <fo:table-header>-->
                    <!--                                                        <fo:table-row background-color="#f5f5f5" font-weight="bold">-->
                    <!--                                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">-->
                    <!--                                                                <fo:block>Data zamówienia</fo:block>-->
                    <!--                                                            </fo:table-cell>-->
                    <!--                                                            <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">-->
                    <!--                                                                <fo:block>Numer zamówienia</fo:block>-->
                    <!--                                                            </fo:table-cell>-->
                    <!--                                                        </fo:table-row>-->
                    <!--                                                    </fo:table-header>-->
                    <!--                                                    <fo:table-body>-->
                    <!--                                                        <xsl:apply-templates select="crd:Fa/crd:WarunkiTransakcji/crd:Zamowienia"/>-->
                    <!--                                                    </fo:table-body>-->
                    <!--                                                </fo:table>-->
                    <!--                                            </fo:table-cell>-->
                    <!--                                        </xsl:when>-->
                    <!--                                </fo:table-row>-->
                    <!--                            </fo:table-body>-->
                    <!--                        </fo:table>-->

                    <!-- Rachunki bankowe -->
                    <xsl:if test="count(crd:Fa/crd:Platnosc/crd:RachunekBankowy) > 0">
                        <!-- Blok tytułu -->
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>
                        <fo:block font-size="12pt" text-align="left">
                            <fo:inline font-weight="bold">Numer rachunku bankowego</fo:inline>
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
                                <fo:inline font-weight="bold">Rejestry</fo:inline>
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
                                            <fo:block>Pełna nazwa</fo:block>
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
                                <fo:inline font-weight="bold">Pozostałe informacje</fo:inline>
                            </fo:block>
                            <!-- Rekord pozostałych informacji -->
                            <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                                <fo:table-column column-width="100%"/> <!-- Stopka faktury-->
                                <fo:table-header>
                                    <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                        <fo:table-cell
                                                xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block>Stopka faktury</fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </fo:table-header>
                                <fo:table-body>
                                    <xsl:apply-templates select="crd:Stopka/crd:Informacje"/>
                                </fo:table-body>
                            </fo:table>
                        </fo:block>
                    </xsl:if>

                    <xsl:if test="$verificationLink and $nrKsef and $qrCode">
                        <!-- Kod QR -->
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                        <fo:block font-size="12pt" text-align="left">
                            <fo:inline font-weight="bold">Sprawdź, czy Twoja faktura znajduje się w KSeF</fo:inline>
                        </fo:block>
                        <fo:block>
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
                                                        src="url('data:image/png;base64,{$qrCode}')"/>
                                            </fo:block>
                                            <xsl:if test="$nrKsef">
                                                <fo:block text-align="center" font-weight="600">
                                                    <xsl:value-of select="$nrKsef"/>
                                                </fo:block>
                                            </xsl:if>
                                        </fo:table-cell>
                                        <!-- Komórka z tekstem -->
                                        <fo:table-cell display-align="center" height="auto">
                                            <fo:block font-size="7pt" display-align="center">
                                                <fo:block font-weight="600" space-after="2mm">
                                                    Nie możesz zeskanować kodu z obrazka? Kliknij w link weryfikacyjny i
                                                    przejdź do weryfikacji faktury.
                                                </fo:block>
                                                <fo:block display-align="center" space-after="2mm">
                                                    <fo:basic-link
                                                            external-destination="{$verificationLink}" color="blue">
                                                        <xsl:value-of select="$verificationLink"/>
                                                    </fo:basic-link>
                                                </fo:block>
                                            </fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </fo:table-body>
                            </fo:table>
                        </fo:block>
                    </xsl:if>
                    <xsl:if test="$issuerUser">
                       <fo:block border-bottom="solid 1px grey" space-after="4mm" space-before="4mm"/>

                        <fo:block font-size="10pt" text-align="left">
                            <fo:inline font-weight="bold">Osoba wystawiająca: </fo:inline>
                            <xsl:value-of select="$issuerUser"/>
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


    <!--    TODO Do usunięcia? Powinno korzystać z pliku `invoice-rows.xsl`-->
    <!--    <xsl:template match="crd:Fa/crd:FaWiersz">-->
    <!--        <fo:table-row>-->
    <!--            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">-->
    <!--                <fo:block>-->
    <!--                    <xsl:value-of select="crd:NrWierszaFa"/> &lt;!&ndash; Lp &ndash;&gt;-->
    <!--                </fo:block>-->
    <!--            </fo:table-cell>-->
    <!--            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" padding-left="3pt">-->
    <!--                <fo:block>-->
    <!--                    <xsl:value-of select="crd:P_7"/> &lt;!&ndash; Nazwa &ndash;&gt;-->
    <!--                </fo:block>-->
    <!--            </fo:table-cell>-->
    <!--            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">-->
    <!--                <fo:block>-->
    <!--                    <xsl:value-of select="crd:P_8B"/> &lt;!&ndash; Ilość &ndash;&gt;-->
    <!--                </fo:block>-->
    <!--            </fo:table-cell>-->
    <!--            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">-->
    <!--                <fo:block>-->
    <!--                    <xsl:value-of select="crd:P_8A"/> &lt;!&ndash; Jednostka &ndash;&gt;-->
    <!--                </fo:block>-->
    <!--            </fo:table-cell>-->
    <!--            <xsl:if test="crd:P_9A">-->
    <!--                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">-->
    <!--                    <fo:block>-->
    <!--                        <xsl:value-of-->
    <!--                                select="translate(format-number(number(crd:P_9A), '#,##0.00'), ',.', ' ,')"/> &lt;!&ndash; Cena netto &ndash;&gt;-->
    <!--                    </fo:block>-->
    <!--                </fo:table-cell>-->
    <!--            </xsl:if>-->
    <!--            <xsl:if test="crd:P_9B">-->
    <!--                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">-->
    <!--                    <fo:block>-->
    <!--                        <xsl:value-of-->
    <!--                                select="translate(format-number(number(crd:P_9B), '#,##0.00'), ',.', ' ,')"/> &lt;!&ndash; Cena brutto &ndash;&gt;-->
    <!--                    </fo:block>-->
    <!--                </fo:table-cell>-->
    <!--            </xsl:if>-->
    <!--            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">-->
    <!--                <xsl:choose>-->
    <!--                    <xsl:when test="crd:P_10">-->
    <!--                        <fo:block>-->
    <!--                            <xsl:value-of-->
    <!--                                    select="translate(format-number(number(crd:P_10), '#,##0.00'), ',.', ' ,')"/> &lt;!&ndash; Rabat&ndash;&gt;-->
    <!--                        </fo:block>-->
    <!--                    </xsl:when>-->
    <!--                    <xsl:otherwise>-->
    <!--                        <fo:block/>-->
    <!--                    </xsl:otherwise>-->
    <!--                </xsl:choose>-->

    <!--            </fo:table-cell>-->
    <!--            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">-->
    <!--                <fo:block>-->
    <!--                    <xsl:choose>-->
    <!--                        <xsl:when test="number(crd:P_12) = number(crd:P_12)">-->
    <!--                            <xsl:value-of select="crd:P_12"/>%-->
    <!--                        </xsl:when>-->
    <!--                        <xsl:otherwise>-->
    <!--                            <xsl:value-of select="crd:P_12"/>-->
    <!--                        </xsl:otherwise>-->
    <!--                    </xsl:choose>-->
    <!--                </fo:block>-->
    <!--            </fo:table-cell>-->
    <!--            <xsl:if test="//crd:FaWiersz/crd:P_11">-->
    <!--                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">-->
    <!--                    <fo:block>-->
    <!--                        <xsl:choose>-->
    <!--                            <xsl:when test="crd:P_11">-->
    <!--                                <xsl:value-of select="translate(format-number(number(crd:P_11), '#,##0.00'), ',.', ' ,')"/> &lt;!&ndash; Wartość sprzedaży netto &ndash;&gt;-->
    <!--                            </xsl:when>-->
    <!--                            <xsl:otherwise>-->
    <!--                                <fo:block/>-->
    <!--                            </xsl:otherwise>-->
    <!--                        </xsl:choose>-->
    <!--                    </fo:block>-->
    <!--                </fo:table-cell>-->
    <!--            </xsl:if>-->
    <!--            <xsl:if test="//crd:FaWiersz/crd:P_11Vat">-->
    <!--                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">-->
    <!--                    <fo:block>-->
    <!--                        <xsl:choose>-->
    <!--                            <xsl:when test="crd:P_11Vat">-->
    <!--                                <xsl:value-of select="translate(format-number(number(crd:P_11Vat), '#,##0.00'), ',.', ' ,')"/> &lt;!&ndash; Kwota VAT&ndash;&gt;-->
    <!--                            </xsl:when>-->
    <!--                            <xsl:otherwise>-->
    <!--                                <fo:block/>-->
    <!--                            </xsl:otherwise>-->
    <!--                        </xsl:choose>-->
    <!--                    </fo:block>-->
    <!--                </fo:table-cell>-->
    <!--            </xsl:if>-->
    <!--            <xsl:if test="//crd:FaWiersz/crd:P_11A">-->
    <!--                <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">-->
    <!--                    <fo:block>-->
    <!--                        <xsl:choose>-->
    <!--                            <xsl:when test="crd:P_11A">-->
    <!--                                <xsl:value-of select="translate(format-number(number(crd:P_11A), '#,##0.00'), ',.', ' ,')"/> &lt;!&ndash; Wartość sprzedaży brutto &ndash;&gt;-->
    <!--                            </xsl:when>-->
    <!--                            <xsl:otherwise>-->
    <!--                                <fo:block/>-->
    <!--                            </xsl:otherwise>-->
    <!--                        </xsl:choose>-->
    <!--                    </fo:block>-->
    <!--                </fo:table-cell>-->
    <!--            </xsl:if>-->
    <!--        </fo:table-row>-->

    <!--    </xsl:template>-->

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
                                <fo:inline font-weight="600">Numer EORI: </fo:inline>
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
                                <fo:inline font-weight="600">Prefiks VAT: </fo:inline>
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
                            <fo:inline font-weight="600">NIP: </fo:inline>
                            <xsl:value-of
                                    select="crd:DaneIdentyfikacyjne/crd:NIP"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell>
                        <fo:block text-align="left">
                            <fo:inline font-weight="600">Nazwa: </fo:inline>
                            <xsl:value-of
                                    select="crd:DaneIdentyfikacyjne/crd:Nazwa"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell padding-top="16px">
                        <fo:block text-align="left" padding-bottom="3px">
                            <fo:inline font-weight="bold">Adres</fo:inline>
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
                                <fo:inline font-weight="bold">Dane kontaktowe</fo:inline>
                            </fo:block>
                            <xsl:if test="crd:DaneKontaktowe/crd:Email">
                                <fo:block text-align="left" padding-bottom="2px">
                                    <fo:inline font-weight="600">E-mail: </fo:inline>
                                    <xsl:value-of
                                            select="crd:DaneKontaktowe/crd:Email"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="crd:DaneKontaktowe/crd:Telefon">
                                <fo:block text-align="left" padding-bottom="2px">
                                    <fo:inline font-weight="600">Tel.: </fo:inline>
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
                                <fo:inline font-weight="600">Numer EORI: </fo:inline>
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
                                <fo:inline font-weight="600">Numer Vat-UE: </fo:inline>
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
                                <fo:inline font-weight="600">NIP: </fo:inline>
                                <xsl:value-of
                                        select="crd:DaneIdentyfikacyjne/crd:NIP"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </xsl:if>
                <fo:table-row>
                    <fo:table-cell>
                        <fo:block text-align="left">
                            <fo:inline font-weight="600">Nazwa: </fo:inline>
                            <xsl:value-of
                                    select="crd:DaneIdentyfikacyjne/crd:Nazwa"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell padding-top="16px">
                        <fo:block text-align="left" padding-bottom="3px">
                            <fo:inline font-weight="bold">Adres</fo:inline>
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
                                <fo:inline font-weight="bold">Dane kontaktowe</fo:inline>
                            </fo:block>
                            <xsl:if test="crd:DaneKontaktowe/crd:Email">
                                <fo:block text-align="left" padding-bottom="2px">
                                    <fo:inline font-weight="600">E-mail: </fo:inline>
                                    <xsl:value-of
                                            select="crd:DaneKontaktowe/crd:Email"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="crd:DaneKontaktowe/crd:Telefon">
                                <fo:block text-align="left" padding-bottom="2px">
                                    <fo:inline font-weight="600">Tel.: </fo:inline>
                                    <xsl:value-of
                                            select="crd:DaneKontaktowe/crd:Telefon"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="crd:NrKlienta">
                                <fo:block text-align="left" padding-bottom="2px">
                                    <fo:inline font-weight="600">Numer klienta: </fo:inline>
                                    <xsl:value-of select="crd:NrKlienta"/>
                                </fo:block>
                            </xsl:if>
                            <xsl:if test="crd:IDNabywcy">
                                <fo:block text-align="left">
                                    <fo:inline font-weight="600">ID Nabywcy: </fo:inline>
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
                        Faktor
                    </xsl:if>
                    <xsl:if test="crd:Rola = '2'">
                        Odbiorca
                    </xsl:if>
                    <xsl:if test="crd:Rola = '3'">
                        Podmiot pierwotny
                    </xsl:if>
                    <xsl:if test="crd:Rola = '4'">
                        Dodatkowy nabywca
                    </xsl:if>
                    <xsl:if test="crd:Rola = '6'">
                        Płatnik
                    </xsl:if>
                    <xsl:if test="crd:Rola = '7'">
                        Jednostka samorządu terytorialnego - wystawca
                    </xsl:if>
                    <xsl:if test="crd:Rola = '8'">
                        Jednostka samorządu terytorialnego - odbiorca
                    </xsl:if>
                    <xsl:if test="crd:Rola = '9'">
                        Członek grupy VAT - wystawca
                    </xsl:if>
                    <xsl:if test="crd:Rola = '10'">
                        Członek grupy VAT - odbiorca
                    </xsl:if>
                </fo:block>
            </xsl:otherwise>
        </xsl:choose>
        <fo:block text-align="left" padding-bottom="3px">
            <xsl:if test="crd:NrEORI">
                <fo:inline font-weight="600">Numer EORI: </fo:inline>
                <xsl:value-of
                        select="crd:NrEORI"/>
            </xsl:if>
        </fo:block>
        <fo:block text-align="left" padding-bottom="3px">
            <xsl:if test="crd:DaneIdentyfikacyjne/crd:NrVatUE">
                <fo:inline font-weight="600">Numer Vat-UE: </fo:inline>
                <xsl:value-of select="crd:DaneIdentyfikacyjne/crd:KodUE"/>
                <xsl:value-of select="crd:DaneIdentyfikacyjne/crd:NrVatUE"/>
            </xsl:if>
            <xsl:if test="crd:DaneIdentyfikacyjne/crd:NIP">
                <fo:inline font-weight="600">NIP: </fo:inline>
                <xsl:value-of
                        select="crd:DaneIdentyfikacyjne/crd:NIP"/>
            </xsl:if>
        </fo:block>
        <fo:block text-align="left" padding-bottom="3px">
            <fo:inline font-weight="600">Nazwa: </fo:inline>
            <xsl:value-of
                    select="crd:DaneIdentyfikacyjne/crd:Nazwa"/>
        </fo:block>
        <xsl:if test="crd:Udzial">
            <fo:inline font-weight="600">Udział: </fo:inline>
            <xsl:value-of select="crd:Udzial"/>%
        </xsl:if>
        <fo:block text-align="left" padding-bottom="3px" padding-top="16px">
            <fo:inline font-weight="bold">Adres</fo:inline>
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
                <fo:inline font-weight="bold">Dane kontaktowe
                </fo:inline>
            </fo:block>
            <xsl:if test="crd:DaneKontaktowe/crd:Email">
                <fo:block text-align="left" padding-bottom="2px">
                    <fo:inline font-weight="600">E-mail:</fo:inline>
                    <xsl:value-of
                            select="crd:DaneKontaktowe/crd:Email"/>
                </fo:block>
            </xsl:if>
            <xsl:if test="crd:DaneKontaktowe/crd:Telefon">
                <fo:block text-align="left" padding-bottom="2px">
                    <fo:inline font-weight="600">Tel.:</fo:inline>
                    <xsl:value-of
                            select="crd:DaneKontaktowe/crd:Telefon"/>
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
                            <fo:block>Numer rachunku bankowego</fo:block>
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
                            <fo:block>Kod SWIFT</fo:block>
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
                            <fo:block>Nazwa banku</fo:block>
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
                            <fo:block>Opis rachunku</fo:block>
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
                Dane identyfikacyjne faktury korygowanej <xsl:value-of select="$numer"/>
            </fo:block>
        </xsl:if>

        <xsl:if test="$faktura/crd:DataWystFaKorygowanej">
            <fo:block text-align="left" space-after="1mm">
                <fo:inline font-weight="600">Data wystawienia faktury, której dotyczy faktura korygująca: </fo:inline>
                <xsl:value-of select="$faktura/crd:DataWystFaKorygowanej"/>
            </fo:block>
        </xsl:if>

        <xsl:if test="$faktura/crd:NrFaKorygowanej">
            <fo:block text-align="left" space-after="1mm">
                <fo:inline font-weight="600">Numer faktury korygowanej: </fo:inline>
                <xsl:value-of select="$faktura/crd:NrFaKorygowanej"/>
            </fo:block>
        </xsl:if>

        <xsl:if test="$faktura/crd:NrKSeFFaKorygowanej">
            <fo:block text-align="left">
                <fo:inline font-weight="600">Numer KSeF faktury korygowanej: </fo:inline>
                <xsl:value-of select="$faktura/crd:NrKSeFFaKorygowanej"/>
            </fo:block>
        </xsl:if>
    </xsl:template>


</xsl:stylesheet>
