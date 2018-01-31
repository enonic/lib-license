package com.enonic.lib.license;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class LicenseDetailsTest
{

    private static final Instant TIME1 = Instant.parse( "2018-01-30T09:00:00Z" );

    private static final Instant TIME2 = Instant.parse( "2018-01-05T09:00:00Z" );

    @Test
    public void testEquals()
    {
        final LicenseDetails lic1 = LicenseDetails.create().
            issuedTo( "name" ).
            issuedBy( "organization" ).
            expiryTime( TIME1 ).
            issueTime( TIME2 ).
            property( "key1", "value1" ).
            property( "key2", "value2" ).
            build();
        final LicenseDetails lic2 = LicenseDetails.create().
            issuedTo( "name" ).
            issuedBy( "organization" ).
            expiryTime( TIME1 ).
            issueTime( TIME2 ).
            property( "key1", "value1" ).
            property( "key2", "value2" ).
            build();

        assertEquals( lic1, lic1 );
        assertEquals( lic1, lic2 );
        assertNotEquals( lic1, "" );
        assertEquals( lic1.hashCode(), lic2.hashCode() );
        assertEquals( lic1.toString(), lic2.toString() );
    }

    @Test
    public void testCopy()
    {
        final LicenseDetails lic1 = LicenseDetails.create().
            issuedTo( "name" ).
            issuedBy( "organization" ).
            expiryTime( TIME1 ).
            issueTime( TIME2 ).
            property( "key1", "value1" ).
            property( "key2", "value2" ).
            build();
        final LicenseDetails lic2 = LicenseDetails.create( lic1 ).build();

        assertEquals( lic1, lic2 );
    }


    @Test
    public void testGetters()
    {
        final Map<String, String> props = new HashMap<>();
        props.put( "key1", "value1" );
        props.put( "key2", "value2" );

        final LicenseDetails lic1 = LicenseDetails.create().
            issuedTo( "name" ).
            issuedBy( "organization" ).
            expiryTime( TIME1 ).
            issueTime( TIME2 ).
            properties( props ).
            build();

        assertEquals( "name", lic1.getIssuedTo() );
        assertEquals( "organization", lic1.getIssuedBy() );
        assertEquals( TIME1, lic1.getExpiryTime() );
        assertEquals( TIME2, lic1.getIssueTime() );
        assertEquals( "value1", lic1.getProperty( "key1" ) );
        assertEquals( "value2", lic1.getProperty( "key2" ) );
        assertEquals( "default", lic1.getProperty( "key3", "default" ) );
        assertEquals( 2, lic1.getProperties().size() );
    }
}