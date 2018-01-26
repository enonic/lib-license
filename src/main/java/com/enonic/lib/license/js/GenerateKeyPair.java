package com.enonic.lib.license.js;

import java.security.GeneralSecurityException;

import com.enonic.lib.license.KeyPair;
import com.enonic.lib.license.LicenseManager;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;

public final class GenerateKeyPair
    implements ScriptBean
{
    private LicenseManager licenseManager;

    public KeyPairMapper generateKeyPair()
        throws GeneralSecurityException
    {
        final KeyPair keyPair = licenseManager.generateKeyPair();
        return new KeyPairMapper( keyPair );
    }

    @Override
    public void initialize( final BeanContext context )
    {
        this.licenseManager = context.getService( LicenseManager.class ).get();
    }
}
