package com.enonic.lib.license;

import java.security.GeneralSecurityException;

public interface LicenseManager
{
    KeyPair generateKeyPair()
        throws GeneralSecurityException;
}
