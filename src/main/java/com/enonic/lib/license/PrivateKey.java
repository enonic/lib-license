package com.enonic.lib.license;

import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class PrivateKey
{
    private final java.security.PrivateKey privateKey;

    PrivateKey( final java.security.PrivateKey privateKey )
    {
        this.privateKey = privateKey;
    }

    java.security.PrivateKey getRsaKey()
    {
        return privateKey;
    }

    public String serialize()
    {
        return Base64.getUrlEncoder().withoutPadding().encodeToString( privateKey.getEncoded() );
    }

    public static PrivateKey from( final String value )
    {
        if ( value == null )
        {
            return null;
        }
        try
        {
            final byte[] keyBytes = Base64.getUrlDecoder().decode( value );
            java.security.PrivateKey privateKey = KeyFactory.getInstance( "RSA" ).generatePrivate( new PKCS8EncodedKeySpec( keyBytes ) );
            return new PrivateKey( privateKey );
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
        final PrivateKey that = (PrivateKey) o;
        return Objects.equals( privateKey, that.privateKey );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( privateKey );
    }

    @Override
    public String toString()
    {
        final String pre = FormatHelper.bytesToHex( privateKey.getEncoded(), 3 );
        final String str = pre + "..." + FormatHelper.bytesToHex( privateKey.getEncoded(), -3 );
        return MoreObjects.toStringHelper( this ).add( "key", str ).toString();
    }
}
