package com.enonic.lib.license.js;

import com.enonic.lib.license.KeyPair;
import com.enonic.xp.script.serializer.MapGenerator;
import com.enonic.xp.script.serializer.MapSerializable;

public final class KeyPairMapper
    implements MapSerializable
{

    private final KeyPair keyPair;

    KeyPairMapper( final KeyPair keyPair )
    {
        this.keyPair = keyPair;
    }

    @Override
    public void serialize( final MapGenerator gen )
    {
        gen.value( "privateKey", keyPair.getPrivateKey().serialize() );
        gen.value( "publicKey", keyPair.getPublicKey().serialize() );
    }

}
