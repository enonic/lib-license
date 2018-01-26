package com.enonic.lib.license.internal;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import javax.crypto.Cipher;

import org.osgi.service.component.annotations.Component;

import com.enonic.lib.license.KeyPair;
import com.enonic.lib.license.License;
import com.enonic.lib.license.LicenseManager;

@Component(immediate = true)
public final class LicenseManagerImpl
    implements LicenseManager
{
    private static final int KEY_SIZE = 2048;

    @Override
    public KeyPair generateKeyPair()
        throws GeneralSecurityException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance( "RSA" );
        keyGen.initialize( KEY_SIZE );
        final java.security.KeyPair rsaKeyPair = keyGen.genKeyPair();
        return new KeyPair( rsaKeyPair );
    }

    public String generateLicense( final License license, final com.enonic.lib.license.PrivateKey privateKey )
        throws GeneralSecurityException
    {
        final String plainText = license.toString();
        final String signature = sign( plainText, privateKey.getRsaKey() );
        System.out.println( signature );
        return signature;
    }

    public boolean validateLicense( final License license, String signature, final com.enonic.lib.license.PublicKey publicKey )
        throws GeneralSecurityException
    {
        final String plainText = license.toString();
        final boolean valid = verify( plainText, signature, publicKey.getRsaKey() );
        System.out.println( valid );
        return valid;
    }

    private String sign( String plainText, PrivateKey privateKey )
        throws GeneralSecurityException
    {
        Signature privateSignature = Signature.getInstance( "SHA256withRSA" );
        privateSignature.initSign( privateKey );
        privateSignature.update( plainText.getBytes( StandardCharsets.UTF_8 ) );

        byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString( signature );
    }

    private boolean verify( String plainText, String signature, PublicKey publicKey )
        throws GeneralSecurityException
    {
        Signature publicSignature = Signature.getInstance( "SHA256withRSA" );
        publicSignature.initVerify( publicKey );
        publicSignature.update( plainText.getBytes( StandardCharsets.UTF_8 ) );

        byte[] signatureBytes = Base64.getDecoder().decode( signature );

        return publicSignature.verify( signatureBytes );
    }

    private String encrypt( String plainText, PublicKey publicKey )
        throws Exception
    {
        Cipher encryptCipher = Cipher.getInstance( "RSA" );
        encryptCipher.init( Cipher.ENCRYPT_MODE, publicKey );

        byte[] cipherText = encryptCipher.doFinal( plainText.getBytes( StandardCharsets.UTF_8 ) );

        return Base64.getEncoder().encodeToString( cipherText );
    }


}
