[![Renovate enabled](https://img.shields.io/badge/renovate-enabled-brightgreen.svg)](https://renovatebot.com/)
[![Maven Central](http://img.shields.io/maven-central/v/io.alapierre.ksef/ksef-fop)](https://search.maven.org/artifact/io.alapierre.ksef/ksef-fop)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ksef4dev_ksef-fop&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ksef4dev_ksef-fop)

## Table of contents
* [General information](#general-information)
* [Technologies](#technologies)
* [Configuration](#configuration)
* [Invoice PDF](#invoices)
* [Examples](#examples)
* [FOP Schema](#fop-schema)

# General information
### PDF generator for KSeF
Our PDF Generator project allows you to automatically create upo/invoice documents in PDF format based on data contained in XML files.
It is a flexible solution that allows you to quickly generate professional-looking upo/invoices without the need for manual data processing.


What do you need to use it in your application:

1. Fonts if you want polish diacritical letters 
2. FOP config file 
3. Dependency `io.alapierre.ksef:ksef-fop` 

# Technologies
- Java 17
- Apache FOP

# Configuration

##### Example FOP config

````xml
<fop version="1.0">
    <renderers>
        <renderer mime="application/pdf">
            <fonts>
                <font kerning="yes" embed-url="file:fonts/OpenSans-Regular.ttf">
                    <font-triplet name="sans" style="normal" weight="normal"/>
                </font>
                <font kerning="yes" embed-url="file:fonts/OpenSans-Bold.ttf">
                    <font-triplet name="sans" style="normal" weight="bold"/>
                </font>
            </fonts>
        </renderer>
    </renderers>
</fop>
````

Tailor your font path and name - FOP template uses font family name `sans`.
You can read more about fonts in FOP here: https://xmlgraphics.apache.org/fop/0.95/fonts.html

# Invoices
The PDF invoice generator currently offers the following features:

- Generating basic invoice data: Support for invoice number, invoice type and KSeF number
- Data about entities: Possibility to generate data about three different entities (Entity 1, Entity 2, Entity 3) containing information such as name, address, contact data etc.
- Invoice details: Date of issue, date of sale, place of invoice issue.
- Invoice items: List of products or services with prices and quantities.
- Tax Rate Summary: Automatically calculate and present a summary of the various tax rates on your invoice.
- Payment Details: Information regarding payment terms, payment methods, etc.
- Bank account number: Option to add a bank account number to facilitate the payment process.
- Verification Data: QR code and verification link


# Examples

##### Generate UPO
````java

PdfGenerator generator = new PdfGenerator("fop.xconf");

try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/upo.pdf"))) {

    InputStream xml = new FileInputStream("src/test/resources/20231111-SE-E8DDA726E2-F87F056923-EC.xml");
    Source src = new StreamSource(xml);
    generator.generateUpo(src, out);
}
````

##### Generate Invoice
````java
PdfGenerator generator = new PdfGenerator(new FileInputStream("src/test/resources/fop.xconf"));
String ksefNumber = "6891152920-20231221-B3242FB4B54B-DF";
String verificationLink = "https://ksef-test.mf.gov.pl/web/verify/6891152920-20231221-B3242FB4B54B-DF/ssTckvmMFEeA3vp589ExHzTRVhbDksjcFzKoXi4K%2F%2F0%3D";
File qrCodeFile = new File("src/test/resources/barcode.png");

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("src/test/resources/invoice.pdf"))) {

            InputStream xml = new FileInputStream("src/test/resources/faktury/podstawowa/FA_2_Przyklad_20.xml");
            Source src = new StreamSource(xml);
            generator.generateInvoice(src, ksefNumber, verificationLink, qrCode, out);
        }
````

# Fop Schema 

https://svn.apache.org/repos/asf/xmlgraphics/fop/trunk/fop/src/foschema/fop.xsd
