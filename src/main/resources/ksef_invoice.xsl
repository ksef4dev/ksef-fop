<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:crd="http://crd.gov.pl/wzor/2023/06/29/12648/">
    <!-- Autor: Karol Bryzgiel (karol.bryzgiel@soft-project.pl) -->

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
                <fo:static-content flow-name="xsl-region-after">
                    <!-- Linia oddzielająca -->
                    <fo:block border-bottom="solid 1px black" padding-bottom="2mm" space-after="2mm"/>
                    <!-- System, z którego wytworzono fakture -->
                    <fo:block font-size="8pt" text-align="left">
                        Wytworzona w: <xsl:value-of select="crd:Naglowek/crd:SystemInfo"/>
                    </fo:block>
                </fo:static-content>

                <!-- Faktura -->
                <fo:flow flow-name="xsl-region-body" color="#343a40">
                    <!-- Tytuł strony -->
                    <fo:block font-size="20pt" font-weight="bold" text-align="left">
                        Krajowy System e-Faktur
                    </fo:block>
                    <!-- Numer faktury -->
                    <fo:block font-size="9pt" text-align="right" padding-top="-5mm">
                        Numer faktury
                    </fo:block>
                    <fo:block font-size="16pt" text-align="right" space-after="2mm">
                        <fo:inline font-weight="bold"><xsl:value-of select="crd:Fa/crd:P_2"/></fo:inline>
                    </fo:block>
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
                            Faktura zaliczkowa korygująca
                        </xsl:if>
                        <xsl:if test="crd:Fa/crd:RodzajFaktury = 'KOR_ROZ'">
                            Faktura rozliczeniowa korygująca
                        </xsl:if>
                    </fo:block>
                    <!-- Numer KSeF-->
                    <fo:block font-size="9pt" text-align="right" space-after="5mm">
                        <fo:inline font-weight="bold">Numer KSeF: </fo:inline>
                        <fo:inline><xsl:value-of select="crd:Fa/crd:NrKSeF"/></fo:inline>
                    </fo:block>
                    <!-- Linia oddzielająca -->
                    <fo:block border-bottom="solid 1px grey" space-after="5mm"/>
                    <!-- Sprzedawca / Nabywca -->
                    <fo:table font-size="7pt">
                        <fo:table-column column-width="50%"/>
                        <fo:table-column column-width="50%"/>
                        <fo:table-body>
                            <fo:table-row space-after="5mm">
                                <fo:table-cell padding-bottom="8px">
                                    <fo:block font-size="12pt" text-align="left">
                                        <fo:inline font-weight="bold">Sprzedawca</fo:inline>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding-bottom="8px">
                                    <fo:block font-size="12pt" text-align="left">
                                        <fo:inline font-weight="bold">Nabywca</fo:inline>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>

                            <!-- Dane sprzedawcy i nabywcy -->
                            <fo:table-row>
                                <fo:table-cell>
                                    <fo:block text-align="left" padding-bottom="3px">
                                        <fo:inline font-weight="600">NIP: </fo:inline>
                                        <xsl:value-of select="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:NIP"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block text-align="left">
                                        <fo:inline font-weight="600" padding-bottom="3px">NIP: </fo:inline>
                                        <xsl:value-of select="crd:Podmiot2/crd:DaneIdentyfikacyjne/crd:NIP"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                            <fo:table-row>
                                <fo:table-cell>
                                    <fo:block text-align="left">
                                        <fo:inline font-weight="600">Nazwa: </fo:inline>
                                        <xsl:value-of select="crd:Podmiot1/crd:DaneIdentyfikacyjne/crd:Nazwa"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block text-align="left">
                                        <fo:inline  font-weight="600">Nazwa: </fo:inline>
                                        <xsl:value-of select="crd:Podmiot2/crd:DaneIdentyfikacyjne/crd:Nazwa"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>

                            <!-- Adres-->
                            <fo:table-row>
                                <fo:table-cell padding-top="16px">
                                    <fo:block text-align="left" padding-bottom="3px">
                                        <fo:inline font-weight="bold">Adres</fo:inline>
                                    </fo:block>
                                    <fo:block text-align="left">
                                        <xsl:value-of select="crd:Podmiot1/crd:Adres/crd:AdresL1"/>
                                        <xsl:if test="crd:Podmiot1/crd:Adres/crd:AdresL2">
                                            <fo:inline>, </fo:inline>
                                            <xsl:value-of select="crd:Podmiot1/crd:Adres/crd:AdresL2"/>
                                        </xsl:if>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding-top="16px">
                                    <fo:block text-align="left" padding-bottom="3px">
                                        <fo:inline font-weight="bold">Adres</fo:inline>
                                    </fo:block>
                                    <fo:block text-align="left">
                                        <xsl:value-of select="crd:Podmiot2/crd:Adres/crd:AdresL1"/>
                                        <xsl:if test="crd:Podmiot2/crd:Adres/crd:AdresL2">
                                            <fo:inline>, </fo:inline>
                                            <xsl:value-of select="crd:Podmiot2/crd:Adres/crd:AdresL2"/>
                                        </xsl:if>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>

                            <!-- Dane kontaktowe-->
                            <xsl:if test="crd:Podmiot1/crd:DaneKontaktowe|crd:Podmiot2/crd:DaneKontaktowe|crd:Podmiot2/crd:NrKlienta|crd:Podmiot2/crd:IDNabywcy">
                                <fo:table-row>
                                    <xsl:if test="crd:Podmiot1/crd:DaneKontaktowe/crd:Email|crd:Podmiot1/crd:DaneKontaktowe/crd:Telefon">
                                        <fo:table-cell padding-top="16px">
                                            <fo:block text-align="left" padding-bottom="3px">
                                                <fo:inline font-weight="bold">Dane kontaktowe</fo:inline>
                                            </fo:block>
                                            <xsl:if test="crd:Podmiot1/crd:DaneKontaktowe/crd:Email">
                                                <fo:block text-align="left" padding-bottom="2px">
                                                    <fo:inline font-weight="600">E-mail: </fo:inline>
                                                    <xsl:value-of select="crd:Podmiot1/crd:DaneKontaktowe/crd:Email"/>
                                                </fo:block>
                                            </xsl:if>
                                            <xsl:if test="crd:Podmiot1/crd:DaneKontaktowe/crd:Telefon">
                                                <fo:block text-align="left" padding-bottom="2px">
                                                    <fo:inline font-weight="600">Tel.: </fo:inline>
                                                    <xsl:value-of select="crd:Podmiot1/crd:DaneKontaktowe/crd:Telefon"/>
                                                </fo:block>
                                            </xsl:if>
                                        </fo:table-cell>
                                    </xsl:if>
                                    <xsl:if test="crd:Podmiot2/crd:DaneKontaktowe/crd:Email|crd:Podmiot2/crd:DaneKontaktowe/crd:Telefon|crd:Podmiot2/crd:NrKlienta|crd:Podmiot2/crd:IDNabywcy">
                                        <fo:table-cell padding-top="16px">
                                            <fo:block text-align="left" padding-bottom="3px">
                                                <fo:inline font-weight="bold">Dane kontaktowe</fo:inline>
                                            </fo:block>
                                            <xsl:if test="crd:Podmiot2/crd:DaneKontaktowe/crd:Email">
                                                <fo:block text-align="left" padding-bottom="2px">
                                                    <fo:inline font-weight="600">E-mail: </fo:inline>
                                                    <xsl:value-of select="crd:Podmiot2/crd:DaneKontaktowe/crd:Email"/>
                                                </fo:block>
                                            </xsl:if>
                                            <xsl:if test="crd:Podmiot2/crd:DaneKontaktowe/crd:Telefon">
                                                <fo:block text-align="left" padding-bottom="2px">
                                                    <fo:inline font-weight="600">Tel.: </fo:inline>
                                                    <xsl:value-of select="crd:Podmiot2/crd:DaneKontaktowe/crd:Telefon"/>
                                                </fo:block>
                                            </xsl:if>
                                            <xsl:if test="crd:Podmiot2/crd:NrKlienta">
                                                <fo:block text-align="left" padding-bottom="2px">
                                                    <fo:inline font-weight="600">Numer klienta: </fo:inline>
                                                    <xsl:value-of select="crd:Podmiot2/crd:NrKlienta"/>
                                                </fo:block>
                                            </xsl:if>
                                            <xsl:if test="crd:Podmiot2/crd:IDNabywcy">
                                                <fo:block text-align="left">
                                                    <fo:inline font-weight="600">ID Nabywcy: </fo:inline>
                                                    <xsl:value-of select="crd:Podmiot2/crd:IDNabywcy"/>
                                                </fo:block>
                                            </xsl:if>
                                        </fo:table-cell>
                                    </xsl:if>
                                </fo:table-row>
                            </xsl:if>
                        </fo:table-body>
                    </fo:table>
                    <!-- Linia oddzielająca -->
                    <fo:block border-bottom="solid 1px grey" space-after="5mm" space-before="5mm"/>

                    <!-- Szczegóły -->
                    <fo:block font-size="12pt" text-align="left" space-after="5mm">
                        <fo:inline font-weight="bold">Szczegóły</fo:inline>
                    </fo:block>
                    <fo:table font-size="7pt">
                        <fo:table-column column-width="50%"/>
                        <fo:table-column column-width="50%"/>
                        <fo:table-body>
                            <!-- Data wystawienia, Miejsce wystawienia -->
                            <xsl:if test="crd:Fa/crd:P_1|crd:Fa/crd:P_1M">
                                <fo:table-row space-after="5mm">
                                    <xsl:if test="crd:Fa/crd:P_1">
                                        <fo:table-cell padding-bottom="6px">
                                            <fo:block  text-align="left">
                                                <fo:inline  font-weight="bold">Data wystawienia, z zastrzeżeniem art. 106na ust. 1 ustawy: </fo:inline>
                                                <xsl:value-of select="crd:Fa/crd:P_1"/>
                                            </fo:block>
                                        </fo:table-cell>
                                    </xsl:if>
                                    <xsl:if test="crd:Fa/crd:P_1M">
                                        <fo:table-cell padding-bottom="6px">
                                            <fo:block  text-align="left">
                                                <fo:inline  font-weight="bold">Miejsce wystawienia: </fo:inline>
                                                <xsl:value-of select="crd:Fa/crd:P_1M"/>
                                            </fo:block>
                                        </fo:table-cell>
                                    </xsl:if>
                                </fo:table-row>
                            </xsl:if>

                            <!-- Data dokonania lub zakończenia dostawy towarów lub wykonania: -->
                            <xsl:if test="crd:Fa/crd:P_6">
                                <fo:table-row>
                                    <fo:table-cell>
                                        <fo:block text-align="left">
                                            <fo:inline  font-weight="bold">Data dokonania lub zakończenia dostawy towarów lub wykonania: </fo:inline>
                                            <xsl:value-of select="crd:Fa/crd:P_6"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </xsl:if>
                        </fo:table-body>
                    </fo:table>

                    <!-- Linia oddzielająca -->
                    <fo:block border-bottom="solid 1px grey" space-after="5mm" space-before="5mm"/>

                    <fo:block>
                        <fo:block text-align="left" space-after="5mm">
                            <fo:inline font-weight="bold">Pozycje</fo:inline>
                        </fo:block>
                        <!-- Pozycje na FV-->
                        <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                            <fo:table-column column-width="5%"/> <!-- Lp. -->
                            <fo:table-column column-width="57%"/> <!-- Nazwa -->
                            <fo:table-column column-width="10%"/> <!-- Cena jednostkowa netto -->
                            <fo:table-column column-width="5%"/> <!-- Ilość -->
                            <fo:table-column column-width="5%"/> <!-- Jednostka -->
                            <fo:table-column column-width="8%"/> <!-- Stawka podatku -->
                            <fo:table-column column-width="10%"/> <!-- Wartość sprzedaży netto-->
                            <fo:table-header >
                                <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block>Lp.</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block>Nazwa towaru lub usługi</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block>Cena jedn. netto</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block>Ilość</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block>Jedn.</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block>Stawka podatku</fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                        <fo:block>Wartość sprzedaży netto</fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </fo:table-header>
                            <fo:table-body>
                                <xsl:apply-templates select="crd:Fa/crd:FaWiersz"></xsl:apply-templates>
                            </fo:table-body>
                        </fo:table>
                    </fo:block>
                    <!-- Kwota należności ogółem -->
                    <fo:block color="#343a40" font-size="10pt" text-align="right" space-before="3mm">
                        <fo:inline font-weight="bold">Kwota należności ogółem: </fo:inline>
                        <fo:inline>
                            <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_15), '#,##0.00'), ',.', ' ,')"/>
                            <fo:inline>
                                <xsl:value-of select="crd:Fa/crd:KodWaluty"/>
                            </fo:inline>
                        </fo:inline>
                    </fo:block>
                    <!-- Linia oddzielająca -->
                    <fo:block border-bottom="solid 1px grey" space-after="5mm" space-before="3mm"/>

                    <fo:block font-size="12pt" text-align="left" space-after="5mm">
                        <fo:inline font-weight="bold">Podsumowanie stawek podatku</fo:inline>
                    </fo:block>

                    <!-- Podsumowanie stawek podatku-->
                    <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                        <fo:table-column column-width="25%"/> <!-- Stawka podatku -->
                        <fo:table-column column-width="25%"/> <!-- Kwota netto-->
                        <fo:table-column column-width="25%"/> <!-- Kwota podatku -->
                        <fo:table-column column-width="25%"/> <!-- Kwota brutto -->
                        <fo:table-header>
                            <fo:table-row background-color="#f5f5f5" font-weight="bold">
                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                    <fo:block>Stawka podatku</fo:block>
                                </fo:table-cell>
                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                    <fo:block>Kwota netto</fo:block>
                                </fo:table-cell>
                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                    <fo:block>Kwota podatku</fo:block>
                                </fo:table-cell>
                                <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                    <fo:block>Kwota brutto</fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-header>
                        <fo:table-body>
                            <xsl:if test="crd:Fa/crd:P_13_1|crd:Fa/crd:P_14_1">
                                <fo:table-row>
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                        <fo:block>
                                            <xsl:text>22% lub 23%</xsl:text>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                        <fo:block>
                                            <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_13_1), '#,##0.00'), ',.', ' ,')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                        <fo:block>
                                            <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_14_1), '#,##0.00'), ',.', ' ,')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                        <fo:block>
                                            <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_13_1) + number(crd:Fa/crd:P_14_1), '#,##0.00'), ',.', ' ,')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </xsl:if>
                            <xsl:if test="crd:Fa/crd:P_13_2|crd:Fa/crd:P_14_2">
                               <fo:table-row>
                                   <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                       <fo:block>
                                           <xsl:text>7% lub 8%</xsl:text>
                                       </fo:block>
                                   </fo:table-cell>
                                   <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                       <fo:block>
                                           <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_13_2), '#,##0.00'), ',.', ' ,')"/>
                                       </fo:block>
                                   </fo:table-cell>
                                   <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                       <fo:block>
                                           <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_14_2), '#,##0.00'), ',.', ' ,')"/>
                                       </fo:block>
                                   </fo:table-cell>
                                   <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                       <fo:block>
                                           <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_13_2) + number(crd:Fa/crd:P_14_2), '#,##0.00'), ',.', ' ,')"/>
                                       </fo:block>
                                   </fo:table-cell>
                               </fo:table-row>
                            </xsl:if>
                            <xsl:if test="crd:Fa/crd:P_13_3|crd:Fa/crd:P_14_3">
                                <fo:table-row>
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding">
                                        <fo:block>
                                            <xsl:text>5%</xsl:text>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                        <fo:block>
                                            <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_13_3), '#,##0.00'), ',.', ' ,')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                        <fo:block>
                                            <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_14_3), '#,##0.00'), ',.', ' ,')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                                        <fo:block>
                                            <xsl:value-of select="translate(format-number(number(crd:Fa/crd:P_13_3) + number(crd:Fa/crd:P_14_3), '#,##0.00'), ',.', ' ,')"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </xsl:if>
                        </fo:table-body>
                    </fo:table>

                    <!-- Płatność -->
                    <fo:block border-bottom="solid 1px grey" space-after="5mm" space-before="5mm"/>

                    <fo:block font-size="12pt" text-align="left" space-after="5mm">
                        <fo:inline font-weight="bold">Płatność</fo:inline>
                    </fo:block>

                    <!-- Informacja o płatności -->
                    <xsl:if test="crd:Fa/crd:Platnosc/crd:Zaplacono = 1">
                        <fo:block font-size="7pt" text-align="left">
                            <fo:inline font-weight="bold">Informacja o płatności: </fo:inline>
                            Zapłacono
                        </fo:block>
                    </xsl:if>

                    <!-- DataZaplaty -->
                    <xsl:if test="crd:Fa/crd:Platnosc/crd:DataZaplaty">
                        <fo:block font-size="7pt" text-align="left">
                            <fo:inline font-weight="bold">Data zapłaty: </fo:inline>
                            <xsl:value-of select="crd:Fa/crd:Platnosc/crd:DataZaplaty"/>
                        </fo:block>
                    </xsl:if>

                    <!-- Forma płatności -->
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
                    <!-- Termin płatności -->

                    <xsl:if test="crd:Fa/crd:Platnosc/crd:TerminPlatnosci/crd:Termin">
                        <fo:block font-size="7pt" text-align="left">
                            <fo:inline font-weight="bold">Termin płatności: </fo:inline>
                            <xsl:value-of select="crd:Fa/crd:Platnosc/crd:TerminPlatnosci/crd:Termin"/>
                        </fo:block>
                    </xsl:if>


                    <!-- Numer rachunku bankowego -->
                    <xsl:if test="crd:Fa/crd:Platnosc/crd:RachunekBankowy">
                        <fo:block border-bottom="solid 1px grey" space-after="5mm" space-before="5mm"/>

                        <fo:block font-size="12pt" text-align="left" space-after="5mm">
                            <fo:inline font-weight="bold">Numer rachunku bankowego</fo:inline>
                        </fo:block>
                        <fo:table table-layout="fixed" width="100%" border-collapse="separate">
                            <fo:table-column column-width="75mm"/>
                            <fo:table-column column-width="75mm"/>

                            <fo:table-body>
                                <xsl:if test="crd:Fa/crd:Platnosc/crd:RachunekBankowy/crd:NrRB">
                                    <fo:table-row>
                                        <fo:table-cell background-color="#f5f5f5"
                                                       font-weight="bold"
                                                       xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block>Numer rachunku bankowego</fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block><xsl:value-of select="crd:Fa/crd:Platnosc/crd:RachunekBankowy/crd:NrRB"/></fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:Platnosc/crd:RachunekBankowy/crd:NazwaBanku">
                                    <fo:table-row>
                                        <fo:table-cell background-color="#f5f5f5"
                                                       font-weight="bold"
                                                       xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block>Nazwa banku</fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block><xsl:value-of select="crd:Fa/crd:Platnosc/crd:RachunekBankowy/crd:NazwaBanku"/></fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:if>
                                <xsl:if test="crd:Fa/crd:Platnosc/crd:RachunekBankowy/crd:OpisRachunku">
                                    <fo:table-row>
                                        <fo:table-cell background-color="#f5f5f5"
                                                       font-weight="bold"
                                                       xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block>Opis Rachunku</fo:block>
                                        </fo:table-cell>
                                        <fo:table-cell xsl:use-attribute-sets="tableHeaderFont tableBorder table.cell.padding">
                                            <fo:block><xsl:value-of select="crd:Fa/crd:Platnosc/crd:RachunekBankowy/crd:OpisRachunku"/></fo:block>
                                        </fo:table-cell>
                                    </fo:table-row>
                                </xsl:if>
                            </fo:table-body>
                        </fo:table>
                    </xsl:if>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template match="crd:Fa/crd:FaWiersz">
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
                    <xsl:value-of select="translate(format-number(number(crd:P_9A), '#,##0.00'), ',.', ' ,')"/> <!-- Cena netto -->
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
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="left">
                <fo:block>
                    <xsl:value-of select="crd:P_12"/> <!-- Stawka podatku-->
                    %
                </fo:block>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="tableFont tableBorder table.cell.padding" text-align="right">
                <fo:block>
                    <xsl:value-of select="translate(format-number(number(crd:P_11), '#,##0.00'), ',.', ' ,')"/> <!-- Wartość sprzedaży netto -->
                </fo:block>
            </fo:table-cell>
        </fo:table-row>

    </xsl:template>


</xsl:stylesheet>
