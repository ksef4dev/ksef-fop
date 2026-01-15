package io.alapierre.ksef.fop.qr.helpers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.junit.platform.commons.util.StringUtils;


public class CertificateBuilders {
    X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);

    public void withOrganizationName(String organizationName) {
        if (StringUtils.isNotBlank(organizationName)) {
            nameBuilder.addRDN(BCStyle.O, organizationName);
        }
    }

    public void withOrganizationIdentifier(String organizationIdentifier) {
        if (StringUtils.isNotBlank(organizationIdentifier)) {
            nameBuilder.addRDN(BCStyle.ORGANIZATION_IDENTIFIER, organizationIdentifier);
        }
    }

    public void withCommonName(String commonName) {
        if (StringUtils.isNotBlank(commonName)) {
            nameBuilder.addRDN(BCStyle.CN, commonName);
        }
    }

    public void withSerialNumber(String serialNumber) {
        if (StringUtils.isNotBlank(serialNumber)) {
            nameBuilder.addRDN(BCStyle.SERIALNUMBER, serialNumber);
        }
    }

    public void withGivenName(String givenName) {
        if (StringUtils.isNotBlank(givenName)) {
            nameBuilder.addRDN(BCStyle.GIVENNAME, givenName);
        }
    }

    public void withSurname(String surname) {
        if (StringUtils.isNotBlank(surname)) {
            nameBuilder.addRDN(BCStyle.SURNAME, surname);
        }
    }


    public void withCountryCode(String countryCode) {
        if (StringUtils.isNotBlank(countryCode)) {
            nameBuilder.addRDN(BCStyle.C, countryCode);
        }
    }

    public X500NameHolder build() {
        X500Name x500Name = nameBuilder.build();

        return new X500NameHolder(x500Name);
    }

    public X500NameHolder buildForOrganization(String organizationName, String organizationIdentifier, String commonName, String countryCode) {
        withOrganizationIdentifier(organizationIdentifier);
        withOrganizationName(organizationName);
        withCommonName(commonName);
        withCountryCode(countryCode);

        return build();
    }

    public X500NameHolder buildForPerson(String givenName, String surname, String serialNumber, String commonName, String countryCode) {
        withGivenName(givenName);
        withSurname(surname);
        withSerialNumber(serialNumber);
        withCommonName(commonName);
        withCountryCode(countryCode);

        return build();
    }

    @RequiredArgsConstructor
    @Getter
    public class X500NameHolder {

        private final X500Name x500Name;
    }
}
