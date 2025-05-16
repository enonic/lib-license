package com.enonic.lib.license;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;


import org.junit.jupiter.api.Test;

import static com.enonic.lib.license.LicenseManagerImpl.KEY_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class KeyPairTest
{
    private KeyPair genKeyPair()
        throws NoSuchAlgorithmException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance( "RSA" );
        keyGen.initialize( KEY_SIZE );
        final java.security.KeyPair rsaKeyPair = keyGen.genKeyPair();
        return new KeyPair( rsaKeyPair );
    }

    @Test
    public void serialize()
        throws Exception
    {
        KeyPair keyPair = genKeyPair();
        final String k = keyPair.serialize();
        KeyPair keyPair2 = KeyPair.from( keyPair.serialize() );

        assertEquals( k, keyPair.serialize() );
        assertNotEquals( keyPair, k );
        assertEquals( keyPair, keyPair2 );
        assertEquals( keyPair.toString(), keyPair2.toString() );
        assertEquals( keyPair.hashCode(), keyPair2.hashCode() );
    }

    @Test
    public void testFrom()
    {
        String k = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKY";
        KeyPair keyPair = KeyPair.from( k );

        assertNull( keyPair );

        keyPair = KeyPair.from( null );

        assertNull( keyPair );
    }
}
