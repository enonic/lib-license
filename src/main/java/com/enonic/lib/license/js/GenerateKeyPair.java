package com.enonic.lib.license.js;

import com.enonic.lib.license.KeyPair;
import com.enonic.lib.license.LicenseManager;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;

public final class GenerateKeyPair
    implements ScriptBean
{
    private LicenseManager licenseManager;

    public KeyPairMapper generate()
    {
        final KeyPair keyPair = licenseManager.generateKeyPair();
        return new KeyPairMapper( keyPair );
    }

    public KeyPairMapper load( String value )
    {
        final KeyPair keyPair = KeyPair.from( value );
        return keyPair != null ? new KeyPairMapper( keyPair ) : null;
    }

    @Override
    public void initialize( final BeanContext context )
    {
        this.licenseManager = context.getService( LicenseManager.class ).get();
    }
}
