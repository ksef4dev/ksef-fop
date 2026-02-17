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
* [References](#references)
* [License](#license)

# General information
### PDF generator for KSeF
Our PDF Generator allows you to automatically create UPO and invoice documents in PDF format from KSeF XML data.
The layout and styling of the generated PDFs are designed to closely match the visualisation available in the official KSeF taxpayer application (Aplikacja Podatnika KSeF), so you can produce consistent, professional-looking documents without manual data processing.


What do you need to use it in your application:

1. Fonts if you want Polish diacritical letters 
2. FOP config file 
3. Dependency `io.alapierre.ksef:ksef-fop` 

# Technologies
- Java 21
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

- **Invoice schemas**: support for structured invoice FA3 (FA3_1_0_E).
- **Basic data**: invoice number, type, KSeF number, issue and sale dates, place of issue.
- **Parties**: Seller (Entity 1), Buyer (Entity 2), and optional third party (name, address, contact details).
- **Line items and summaries**: list of goods/services with prices and quantities, VAT summary, payment details, bank account number.
- **Verification data**: QR code and verification link (KOD I in ONLINE mode; KOD I + KOD II in OFFLINE mode) configured via `InvoiceQRCodeGeneratorRequest` (URLs or parameters for link generation).
- **Logo**: invoice logo from bytes (`logo`) or from URI (`logoUri`).
- **Additional options**: currency date (`currencyDate`), issuer user (`issuerUser`), highlight differences on correction invoices (`showCorrectionDifferences`)
- **Localization**: Support PL and EN languages.
- **Attachment**: Support XSD Attachment (Zalacznik).

# Examples

##### Generate UPO
````java
PdfGenerator generator = new PdfGenerator("fop.xconf");

try (OutputStream out = new BufferedOutputStream(new FileOutputStream("upo.pdf"))) {
    InputStream xml = new FileInputStream("upo.xml");
    Source src = new StreamSource(xml);
    UpoGenerationParams params = UpoGenerationParams.builder()
            .schema(UpoSchema.UPO_V3)
            .language(Language.PL)
            .build();
    generator.generateUpo(src, params, out);
}
````

##### Generate Invoice (FA3 with online QR)
````java
PdfGenerator generator = new PdfGenerator(new FileInputStream("fop.xconf"));
byte[] invoiceXml = Files.readAllBytes(Paths.get("invoice.xml"));

String verificationLink = "https://qr-test.ksef.mf.gov.pl/invoice/NIP/data/TOKEN";
InvoiceQRCodeGeneratorRequest qrRequest = InvoiceQRCodeGeneratorRequest.onlineQrBuilder(verificationLink);
InvoiceGenerationParams params = InvoiceGenerationParams.builder()
        .schema(InvoiceSchema.FA3_1_0_E)
        .ksefNumber("1234567890-20231221-XXXXXXXX-XX")
        .invoiceQRCodeGeneratorRequest(qrRequest)
        .language(Language.PL)
        .build();

try (OutputStream out = new BufferedOutputStream(new FileOutputStream("invoice.pdf"))) {
    generator.generateInvoice(invoiceXml, params, out);
}
````

For **OFFLINE** invoices (two QR codes: KOD I + KOD II), use `InvoiceQRCodeGeneratorRequest.offlineCertificateQrBuilder(onlineQrCodeUrl, certificateQrCodeUrl)` or the variant with parameters (environment URL, NIP, date, certificate, etc.) — see the `InvoiceQRCodeGeneratorRequest` Javadoc for details.

# FOP Schema

https://svn.apache.org/repos/asf/xmlgraphics/fop/trunk/fop/src/foschema/fop.xsd

# References

- [KSeF – QR codes documentation (KOD I, KOD II)](https://github.com/CIRFMF/ksef-docs/blob/main/kody-qr.md)

# License

This project is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).