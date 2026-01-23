package io.alapierre.ksef.fop.qr.enums;

public enum ContextIdentifierType {
    NIP("Nip"),
    INTERNAL_ID("InternalId"),
    NIP_VAT_UE("NipVatUe"),
    PEPPOL_ID("PeppolId");

    private final String pathPart;
    ContextIdentifierType(String v) { this.pathPart = v; }
    public String pathPart() { return pathPart; }
}