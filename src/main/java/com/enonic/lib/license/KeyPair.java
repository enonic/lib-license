package com.enonic.lib.license;

import com.google.common.base.MoreObjects;

public final class KeyPair
{
    private final PrivateKey privateKey;

    private final PublicKey publicKey;

    public KeyPair( final java.security.KeyPair rsaKeyPair )
    {
        this.privateKey = new PrivateKey( rsaKeyPair.getPrivate() );
        this.publicKey = new PublicKey( rsaKeyPair.getPublic() );
    }

    public PrivateKey getPrivateKey()
    {
        return privateKey;
    }

    public PublicKey getPublicKey()
    {
        return publicKey;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).
            add( "private", privateKey ).
            add( "public", publicKey ).
            toString();
    }

    // Serialize
}
