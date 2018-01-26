package com.enonic.lib.license;

import java.security.GeneralSecurityException;

public interface LicenseManager
{
    KeyPair generateKeyPair()
        throws GeneralSecurityException;

    String generateLicense( PrivateKey privateKey, LicenseDetails license )
        throws GeneralSecurityException;
}
