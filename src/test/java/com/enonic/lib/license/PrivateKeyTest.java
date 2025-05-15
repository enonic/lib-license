package com.enonic.lib.license;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;


import org.junit.jupiter.api.Test;

import static com.enonic.lib.license.LicenseManagerImpl.KEY_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PrivateKeyTest
{

    private PrivateKey genKey()
        throws NoSuchAlgorithmException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance( "RSA" );
        keyGen.initialize( KEY_SIZE );
        final java.security.KeyPair rsaKeyPair = keyGen.genKeyPair();
        return new KeyPair( rsaKeyPair ).getPrivateKey();
    }

    @Test
    public void serialize()
        throws Exception
    {
        PrivateKey privateKey = genKey();
        final String k = privateKey.serialize();
        PrivateKey privateKey2 = PrivateKey.from( privateKey.serialize() );

        assertEquals( k, privateKey.serialize() );
        assertNotEquals( privateKey, k );
        assertEquals( privateKey, privateKey2 );
        assertEquals( privateKey.toString(), privateKey2.toString() );
        assertEquals( privateKey.hashCode(), privateKey2.hashCode() );
        assertEquals( privateKey.getRsaKey(), privateKey2.getRsaKey() );
    }

    @Test
    public void testFrom()
    {
        String k = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKY";
        PrivateKey privateKey = PrivateKey.from( k );

        assertNull( privateKey );

        privateKey = PrivateKey.from( null );

        assertNull( privateKey );
    }

}
