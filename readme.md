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
(invoice headers, column names, annotations, QR code captions, etc.). Labels live as
XML documents at `i18n/labels.xml` (base / Polish) and `i18n/labels_<lang>.xml` per
locale. The XSLT stylesheets load these files at runtime via `document()`, routed
through the **same URIResolver** that resolves custom templates. This means label
overrides use exactly the same mechanism as template overrides — there is no separate
"translation root" concept: overrides live under `InvoiceGenerationParams.templateRoots`.

### 1. Create your labels XML file(s)

Drop them next to any templates you might be overriding, under an `i18n/` directory
inside one of your `templateRoots`:

`/etc/ksef/i18n/labels.xml` (Polish / base):

````xml
<?xml version="1.0" encoding="UTF-8"?>
<labels>
  <entry key="seller">Dostawca</entry>
  <entry key="buyer">Klient</entry>
</labels>
````

`/etc/ksef/i18n/labels_en.xml` (English):

````xml
<?xml version="1.0" encoding="UTF-8"?>
<labels>
  <entry key="seller">Vendor</entry>
  <entry key="buyer">Client</entry>
</labels>
````

### 2. Point `templateRoots` at the directory containing `i18n/`

````java
InvoiceGenerationParams params = InvoiceGenerationParams.builder()
        .schema(InvoiceSchema.FA3_1_0_E)
        .ksefNumber("1234567890-20231221-XXXXXXXX-XX")
        .language(InvoiceLanguage.EN)
        .templateRoot(Path.of("/etc/ksef"))
        .build();

PdfGenerator generator = new PdfGenerator("fop.xconf");
generator.generateInvoice(invoiceXml, params, out);
````

That's it. Anywhere the XSLT looks up a key (`key('kLabels', …, $labels)`), it will see
the merged view. No extra plumbing — the same resolver is shared between the stylesheet
and the QR-code label lookups.

### Lookup order

Per language, candidates are tried most-specific first (`labels_en_US.xml` →
`labels_en.xml` → `labels.xml`). For each candidate the resolver walks:

1. Each configured filesystem root, in insertion order
2. The library classpath (built-in defaults)

Within the XSLT the locale-specific file wins **key-by-key** over the base file:

| Source | Keys contributed |
|--------|------------------|
| `labels_<lang>.xml` (override or classpath) | All keys it contains |
| `labels.xml` (override or classpath) | Any keys not in the locale file |

So a partial locale override merges cleanly with the base file; keys missing from the
locale file fall through to Polish defaults (or your overridden Polish base, if you
supplied one).

> **Note:** an override replaces the library's file at the same path. If your
> `labels_en.xml` has only `seller`, other English keys come from the base file (Polish
> built-ins or your `labels.xml` override). Copy the library file in full if you want
> to keep English defaults while changing a single key.

### Security

- Each root is canonicalised via `Path.toRealPath()`; non-existing or non-directory
  roots are rejected up front.
- Every resource loaded from a root must remain under it (`..` escapes and symlink
  escapes are rejected — the lookup silently falls through to the classpath).
- By default, the XML parser used for labels runs with `FEATURE_SECURE_PROCESSING`,
  DTDs disabled, and external entity resolution disabled.

### Customizing translation XML parser factory

If needed, you can customize the default `DocumentBuilderFactory` used for translation
XML parsing. The library creates its default secure factory first, then invokes your
customizer.

Default factory configuration applied by the library:

- `setNamespaceAware(false)`
- `setValidating(false)`
- `setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)`
- `setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)`
- `setFeature("http://xml.org/sax/features/external-general-entities", false)`
- `setFeature("http://xml.org/sax/features/external-parameter-entities", false)`
- `setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)`
- `setXIncludeAware(false)`
- `setExpandEntityReferences(false)`
- `setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")`
- `setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")`

Invoice example:

````java
InvoiceGenerationParams params = InvoiceGenerationParams.builder()
        .schema(InvoiceSchema.FA3_1_0_E)
        .languageLocale("en")
        .translationDocumentBuilderFactoryCustomizer(factory -> {
            try {
                // Example: keep secure processing, but tweak parser-specific settings.
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            } catch (ParserConfigurationException e) {
                throw new IllegalStateException("Invalid XML parser feature", e);
            }
        })
        .build();
````

UPO example:

````java
UpoGenerationParams params = UpoGenerationParams.builder()
        .schema(UpoSchema.UPO_V4_3)
        .languageLocale("pl")
        .translationDocumentBuilderFactoryCustomizer(factory -> {
            try {
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (ParserConfigurationException e) {
                throw new IllegalStateException(e);
            }
        })
        .build();
````

> **Important:** customizer runs on your responsibility. If you weaken XML parser
> settings, you can re-enable XXE-style risks.

### Using `TranslationService` directly

You rarely need to touch this class: `PdfGenerator` creates a per-invocation instance
from your `templateRoots`. If you need to look up labels from Java (e.g. for tooling or
tests), pass your own `URIResolver` (most likely a `TemplateResolver`):

````java
URIResolver resolver = new TemplateResolver(List.of(Path.of("/etc/ksef")));
TranslationService ts = new TranslationService(resolver);

String label = ts.getTranslation("en", "seller"); // "Vendor"
Document merged = ts.getTranslationsAsXml("en");  // full merged document
````

> **Tip:** the full list of available translation keys lives in the library's
> `i18n/labels.xml` and `i18n/labels_en.xml` (under `src/main/resources/i18n/`).

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