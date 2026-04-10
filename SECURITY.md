# Security Policy

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| 1.2.x   | :white_check_mark: |
| < 1.2   | :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability, please DO NOT create a public issue.

Instead, use GitHub Security Advisories (Private Vulnerability Reporting):

👉 https://github.com/ksef4dev/ksef-fop/security/advisories/new

This allows us to:
- investigate the issue confidentially
- prepare and test a fix
- coordinate responsible disclosure

If necessary, a CVE identifier will be assigned.

## Security Scope and Assumptions

The library processes two types of XML input:

- XSLT stylesheets — trusted (trusted, bundled with the library or provided by the developer)
- E-invoices / UPO documents — untrusted (provided by the caller)

All user-supplied XML must be considered untrusted.

Starting from version 1.2.18, the XML parser is configured in a secure mode:

- external entity resolution is disabled
- DOCTYPE declarations are rejected

This mitigates common XML-based attacks such as:
- XXE (XML External Entity)
- SSRF via XML entities
- entity expansion attacks (e.g. "billion laughs")

## Recommendations for Users

Even with secure defaults, applications using this library should:

- treat all XML input as untrusted
- validate input where appropriate (schema validation, size limits)
- avoid passing untrusted data directly from external sources without verification

## Disclosure Policy

We aim to:
- acknowledge reports within 3–5 working days
- provide a fix or mitigation within a reasonable timeframe
- publish a security advisory after a fix is available

Security fixes will be released as part of a patch version update.