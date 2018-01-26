package com.enonic.lib.license;

import com.google.common.base.MoreObjects;

public final class PrivateKey
{
    private final java.security.PrivateKey privateKey;

    PrivateKey( final java.security.PrivateKey privateKey )
    {
        this.privateKey = privateKey;
    }

    public java.security.PrivateKey getRsaKey()
    {
        return privateKey;
    }

    @Override
    public String toString()
    {
        final String pre = Hex.bytesToHex( privateKey.getEncoded(), 3 );
        final String str = pre + "..." + Hex.bytesToHex( privateKey.getEncoded(), -3 );
        return MoreObjects.toStringHelper( this ).add( "key", str ).toString();
    }
}
