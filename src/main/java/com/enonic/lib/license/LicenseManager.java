package com.enonic.lib.license;

public interface LicenseManager
{
    KeyPair generateKeyPair();

    String generateLicense( PrivateKey privateKey, LicenseDetails license );

    LicenseDetails validateLicense( PublicKey publicKey, String license );
}
