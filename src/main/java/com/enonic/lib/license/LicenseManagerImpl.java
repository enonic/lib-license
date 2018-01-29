package com.enonic.lib.license;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

@Component(immediate = true)
public final class LicenseManagerImpl
    implements LicenseManager
{
    static final int KEY_SIZE = 2048;

    private static final String LICENSE_HEADER = "LICENSE";

    @Override
    public KeyPair generateKeyPair()
    {
        final KeyPairGenerator keyGen = getRSAKeyPairGenerator();
        keyGen.initialize( KEY_SIZE );
        final java.security.KeyPair rsaKeyPair = keyGen.genKeyPair();
        return new KeyPair( rsaKeyPair );
    }

    public String generateLicense( final com.enonic.lib.license.PrivateKey privateKey, final LicenseDetails license )
    {
        String licenseData = LicenseDetailsJSONConverter.serialize( license );
        licenseData = Base64.getEncoder().withoutPadding().encodeToString( licenseData.getBytes( StandardCharsets.UTF_8 ) );

        final String signature = sign( licenseData, privateKey.getRsaKey() );
        return FormatHelper.asPEM( licenseData + "." + signature, LICENSE_HEADER );
    }

    @Override
    public LicenseDetails validateLicense( final com.enonic.lib.license.PublicKey publicKey, final String license )
    {
        final String licenseStr = unwrapLicense( license );
        final int p = licenseStr.indexOf( '.' );
        if ( p == -1 )
        {
            return null;
        }
        final String licenseData = licenseStr.substring( 0, p );
        final String signature = licenseStr.substring( p + 1 );

        final boolean valid = verify( licenseData, signature, publicKey.getRsaKey() );
        if ( !valid )
        {
            return null;
        }

        final byte[] licenseDataBytes = Base64.getUrlDecoder().decode( licenseData );
        final String licenseDataJson = new String( licenseDataBytes, StandardCharsets.UTF_8 );

        return LicenseDetailsJSONConverter.parse( licenseDataJson );
    }

    private String unwrapLicense( final String license )
    {
        if ( license == null )
        {
            return "";
        }
        final String allLines[] = license.split( "\\r?\\n" );
        final List<String> lines = Arrays.stream( allLines ).
            map( String::trim ).
            filter( s -> !s.isEmpty() ).
            collect( Collectors.toList() );
        if ( lines.size() < 3 )
        {
            return "";
        }

        final String header = lines.get( 0 );
        final String footer = lines.get( lines.size() - 1 );
        lines.remove( 0 );
        lines.remove( lines.size() - 1 );
        if ( !FormatHelper.isPEMHeader( header, LICENSE_HEADER ) || !FormatHelper.isPEMFooter( footer, LICENSE_HEADER ) )
        {
            return "";
        }

        return lines.stream().collect( Collectors.joining( "" ) );
    }

    private String sign( String plainText, PrivateKey privateKey )
    {
        try
        {
            final Signature privateSignature = Signature.getInstance( "SHA256withRSA" );
            privateSignature.initSign( privateKey );
            privateSignature.update( plainText.getBytes( StandardCharsets.UTF_8 ) );

            final byte[] signature = privateSignature.sign();
            return Base64.getEncoder().withoutPadding().encodeToString( signature );
        }
        catch ( GeneralSecurityException e )
        {
            throw new RuntimeException( e );
        }
    }

    private boolean verify( String plainText, String signature, PublicKey publicKey )
    {
        try
        {
            Signature publicSignature = Signature.getInstance( "SHA256withRSA" );
            publicSignature.initVerify( publicKey );
            publicSignature.update( plainText.getBytes( StandardCharsets.UTF_8 ) );

            byte[] signatureBytes = Base64.getDecoder().decode( signature );

            return publicSignature.verify( signatureBytes );
        }
        catch ( GeneralSecurityException e )
        {
            throw new RuntimeException( e );
        }
    }

    private KeyPairGenerator getRSAKeyPairGenerator()
    {
        try
        {
            return KeyPairGenerator.getInstance( "RSA" );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( e );
        }
    }
}
