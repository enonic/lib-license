package com.enonic.lib.license;

import org.junit.Test;

import com.enonic.lib.license.internal.LicenseManagerImpl;

import static org.junit.Assert.*;

public class LicenseManagerImplTest
{

    @Test
    public void generateKeyPair()
        throws Exception
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();
        final KeyPair keyPair = licMan.generateKeyPair();

        assertNotNull( keyPair );
        assertNotNull( keyPair.getPrivateKey() );
        assertNotNull( keyPair.getPublicKey() );

        System.out.println( keyPair );
    }

    @Test
    public void generateLicense()
        throws Exception
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();
        final KeyPair keyPair = licMan.generateKeyPair();

        final License license = new License( "name" );
        final String signature = licMan.generateLicense( license, keyPair.getPrivateKey() );
        final boolean valid = licMan.validateLicense( license, signature, keyPair.getPublicKey() );
        assertTrue( valid );
    }
}