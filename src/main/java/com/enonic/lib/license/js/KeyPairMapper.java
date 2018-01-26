package com.enonic.lib.license.js;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteSource;

import com.enonic.lib.license.KeyPair;
import com.enonic.xp.script.serializer.MapGenerator;
import com.enonic.xp.script.serializer.MapSerializable;

public final class KeyPairMapper
    implements MapSerializable
{

    private final byte[] privateKey;

    private final byte[] publicKey;

    KeyPairMapper( final KeyPair keyPair )
    {
        this.privateKey = keyPair.getPrivateKey().getRsaKey().getEncoded();
        this.publicKey = keyPair.getPublicKey().getRsaKey().getEncoded();
    }

    @Override
    public void serialize( final MapGenerator gen )
    {
        gen.value( "privateKeyBytes", ByteSource.wrap( privateKey ) );
        gen.value( "publicKeyBytes", ByteSource.wrap( publicKey ) );

        gen.value( "privateKey", BaseEncoding.base64Url().omitPadding().encode( privateKey ) );
        gen.value( "publicKey", BaseEncoding.base64Url().omitPadding().encode( publicKey ) );
    }

}
