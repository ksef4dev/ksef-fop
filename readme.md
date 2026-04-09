[![Renovate enabled](https://img.shields.io/badge/renovate-enabled-brightgreen.svg)](https://renovatebot.com/)
[![Maven Central](http://img.shields.io/maven-central/v/io.alapierre.ksef/ksef-fop)](https://search.maven.org/artifact/io.alapierre.ksef/ksef-fop)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ksef4dev_ksef-fop&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ksef4dev_ksef-fop)

## Table of contents
* [General information](#general-information)
* [Technologies](#technologies)
* [Configuration](#configuration)
* [Invoice PDF](#invoices)
* [Examples](#examples)
* [Custom templates](#custom-templates)
* [Custom translations](#custom-translations)
* [Custom properties](#custom-properties)
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

# Custom templates

By default the library picks a built-in XSL-FO template based on the `InvoiceSchema` value
(`FA2_1_0_E` → `templates/fa2/ksef_invoice.xsl`, `FA3_1_0_E` → `templates/fa3/ksef_invoice.xsl`).
If you need full control over the PDF layout you can provide your own XSL-FO stylesheet instead.

Set `templatePath` on `InvoiceGenerationParams` to a **classpath-relative** path pointing to
your custom XSL file:

````java
InvoiceGenerationParams params = InvoiceGenerationParams.builder()
        .schema(InvoiceSchema.FA3_1_0_E)
        .ksefNumber("1234567890-20231221-XXXXXXXX-XX")
        .templatePath("templates/custom/my_invoice.xsl")
        .build();
````

When `templatePath` is set the library uses it directly; when it is `null` (the default) the
template is resolved automatically from the schema.

> **Security note:** `templatePath` is used as-is to load a classpath resource. Make sure
> untrusted users cannot control this value or the underlying XSL content.

## Custom translations

The library ships with built-in Polish and English translations for all PDF labels
(invoice headers, column names, annotations, QR code captions, etc.).
You can **override any subset** of these labels - only the keys you provide will be
replaced; everything else falls back to the built-in defaults.

### 1. Create your properties files

Place them on the classpath using the standard Java `ResourceBundle` naming convention.
Pick any base name you like, for example `i18n/custom_messages`:

```
src/main/resources/
└── i18n/
    ├── custom_messages.properties        ← default / Polish overrides
    └── custom_messages_en.properties     ← English overrides (optional)
```

Each file should contain **only the keys you want to change**. For example, to rename
the seller and buyer labels:

`i18n/custom_messages.properties`:

````properties
seller=Dostawca
buyer=Klient
````

`i18n/custom_messages_en.properties`:

````properties
seller=Vendor
buyer=Client
````

You don't need to copy the entire default file - unlisted keys will keep their
built-in values automatically.

### 2. Create a TranslationService with your bundle base name

Pass the classpath-relative base name (without the `.properties` extension and without
the locale suffix) to the `TranslationService` constructor:

````java
TranslationService translationService = new TranslationService("i18n/custom_messages");
````

### 3. Pass the TranslationService to PdfGenerator

````java
TranslationService translationService = new TranslationService("i18n/custom_messages");
PdfGenerator generator = new PdfGenerator("fop.xconf", translationService);
````

Or together with `InvoicePdfConfig`:

````java
TranslationService translationService = new TranslationService("i18n/custom_messages");
PdfGenerator generator = new PdfGenerator("fop.xconf", invoicePdfConfig, translationService);
````

That's it - the generator will now use your values for the overridden keys and the
library defaults for everything else, for both invoice and UPO PDFs.

### How it works

| Priority | Source | Description |
|----------|--------|-------------|
| 1 (highest) | Your `.properties` file | Only the keys you defined |
| 2 (fallback) | Built-in `i18n/messages.properties` / `i18n/messages_en.properties` | All remaining keys |

The resolution happens **per language**: if you provide `custom_messages_en.properties`,
it only affects English output. Polish output will use `custom_messages.properties`
(or fall back to the built-in Polish bundle if that file doesn't exist).

> **Tip:** To see the full list of available translation keys, look at the built-in
> `i18n/messages.properties` (Polish) and `i18n/messages_en.properties` (English)
> inside the library JAR or in the source repository under `src/main/resources/i18n/`.

## Custom properties

Built-in parameters (`nrKsef`, `logo`, `currencyDate`, …) cover the standard use cases, but
a custom template will almost certainly need extra, template-specific data - company
branding fields, feature flags, additional metadata, etc.

`InvoiceGenerationParams` exposes a `customProperties` map for exactly this purpose.
Every entry in the map is forwarded as an XSLT parameter to the transformer, so your
template can declare and use it like any other parameter.

````java
InvoiceGenerationParams params = InvoiceGenerationParams.builder()
        .schema(InvoiceSchema.FA3_1_0_E)
        .ksefNumber("1234567890-20231221-XXXXXXXX-XX")
        .templatePath("templates/custom/my_invoice.xsl")
        .customProperties(Map.of(
                "companySlogan", "We deliver on time!",
                "showWatermark", true
        ))
        .build();
````

In your XSL template declare matching parameters and use them normally:

````xml
<xsl:param name="companySlogan"/>
<xsl:param name="showWatermark"/>

<fo:block font-size="8pt">
    <xsl:value-of select="$companySlogan"/>
</fo:block>
````

Custom properties are applied **after** all built-in parameters, so a key that collides with
a built-in name will override it - use distinct names to avoid surprises.

> **Security note:** `customProperties` entries are forwarded as-is to the XSLT transformer.
> Make sure untrusted users cannot control parameter names or values when rendering trusted templates.

The map defaults to an empty `HashMap`, so existing callers that do not set it are
unaffected.

# FOP Schema

https://svn.apache.org/repos/asf/xmlgraphics/fop/trunk/fop/src/foschema/fop.xsd

# References

- [KSeF – QR codes documentation (KOD I, KOD II)](https://github.com/CIRFMF/ksef-docs/blob/main/kody-qr.md)

# License

This project is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).