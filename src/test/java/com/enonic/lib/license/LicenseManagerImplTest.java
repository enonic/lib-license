package com.enonic.lib.license;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

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
    }

    @Test
    public void generateLicense()
        throws Exception
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();
        final KeyPair keyPair = licMan.generateKeyPair();

        final LicenseDetails licenseDetails = LicenseDetails.create().issuedTo( "name" ).issuedBy( "org" ).build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        assertNotNull( license );
    }

    @Test
    public void validateLicense()
        throws Exception
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();
        final KeyPair keyPair = licMan.generateKeyPair();
        final LicenseDetails licenseDetails = LicenseDetails.create().issuedTo( "name" ).issuedBy( "org" ).build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        final LicenseDetails validLicense = licMan.validateLicense( keyPair.getPublicKey(), license );

        assertNotNull( validLicense );
    }

    @Test
    public void validateExpiredLicense()
        throws Exception
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();
        final KeyPair keyPair = licMan.generateKeyPair();
        final LicenseDetails licenseDetails = LicenseDetails.create().
            issuedTo( "name" ).
            issuedBy( "org" ).
            issueTime( Instant.now() ).
            expiryTime( Instant.now().minus( 10, ChronoUnit.MINUTES ) ).
            build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        final LicenseDetails validLicense = licMan.validateLicense( keyPair.getPublicKey(), license );

        assertNotNull( validLicense );
        assertTrue( validLicense.isExpired() );
    }
}