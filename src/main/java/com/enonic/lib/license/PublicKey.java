package com.enonic.lib.license;

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

    @Override
    public String toString()
    {
        final String pre = Hex.bytesToHex( publicKey.getEncoded(), 3 );
        final String str = pre + "..." + Hex.bytesToHex( publicKey.getEncoded(), -3 );
        return MoreObjects.toStringHelper( this ).add( "key", str ).toString();
    }
}
