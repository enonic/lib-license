package com.enonic.lib.license;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;


import org.junit.jupiter.api.Test;

import static com.enonic.lib.license.LicenseManagerImpl.KEY_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PublicKeyTest
{
    private PublicKey genKey()
        throws NoSuchAlgorithmException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance( "RSA" );
        keyGen.initialize( KEY_SIZE );
        final java.security.KeyPair rsaKeyPair = keyGen.genKeyPair();
        return new KeyPair( rsaKeyPair ).getPublicKey();
    }

    @Test
    public void serialize()
        throws Exception
    {
        PublicKey publicKey = genKey();
        final String k = publicKey.serialize();
        PublicKey publicKey2 = PublicKey.from( publicKey.serialize() );

        assertEquals( k, publicKey.serialize() );
        assertNotEquals( publicKey, k );
        assertEquals( publicKey, publicKey2 );
        assertEquals( publicKey.toString(), publicKey2.toString() );
        assertEquals( publicKey.hashCode(), publicKey2.hashCode() );
        assertEquals( publicKey.getRsaKey(), publicKey2.getRsaKey() );
    }

    @Test
    public void testFrom()
    {
        String k = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy";
        PublicKey publicKey = PublicKey.from( k );

        assertNull( publicKey );

        publicKey = PublicKey.from( null );

        assertNull( publicKey );
    }

}
