package com.enonic.lib.license;

import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class PublicKey
{
    private final java.security.PublicKey publicKey;

    PublicKey( final java.security.PublicKey publicKey )
    {
        this.publicKey = publicKey;
    }

    public java.security.PublicKey getRsaKey()
    {
        return publicKey;
    }

    public String serialize()
    {
        return Base64.getUrlEncoder().withoutPadding().encodeToString( publicKey.getEncoded() );
    }

    public static PublicKey from( final String value )
    {
        if ( value == null )
        {
            return null;
        }
        try
        {
            final byte[] keyBytes = Base64.getUrlDecoder().decode( value );
            java.security.PublicKey publicKey = KeyFactory.getInstance( "RSA" ).generatePublic( new X509EncodedKeySpec( keyBytes ) );
            return new PublicKey( publicKey );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        final PublicKey publicKey1 = (PublicKey) o;
        return Objects.equals( publicKey, publicKey1.publicKey );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( publicKey );
    }

    @Override
    public String toString()
    {
        final String pre = FormatHelper.bytesToHex( publicKey.getEncoded(), 3 );
        final String str = pre + "..." + FormatHelper.bytesToHex( publicKey.getEncoded(), -3 );
        return MoreObjects.toStringHelper( this ).add( "key", str ).toString();
    }
}
