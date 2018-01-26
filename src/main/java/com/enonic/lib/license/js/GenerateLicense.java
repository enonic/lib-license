package com.enonic.lib.license.js;

import java.security.GeneralSecurityException;

import com.enonic.lib.license.LicenseDetails;
import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.PrivateKey;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;

public final class GenerateLicense
    implements ScriptBean
{
    private LicenseManager licenseManager;

    private String name;

    private String organization;

    private String privateKey;

    public String generate()
        throws GeneralSecurityException
    {
        final PrivateKey privateKey = PrivateKey.from( this.privateKey );
        if ( privateKey == null )
        {
            throw new IllegalArgumentException( "Invalid private key" );
        }

        final LicenseDetails licDetails = LicenseDetails.create().
            name( this.name ).
            organization( this.organization ).
            build();
        return licenseManager.generateLicense( privateKey, licDetails );
    }

    public void setName( final String name )
    {
        this.name = name;
    }

    public void setOrganization( final String organization )
    {
        this.organization = organization;
    }

    public void setPrivateKey( final String privateKey )
    {
        this.privateKey = privateKey;
    }

    @Override
    public void initialize( final BeanContext context )
    {
        this.licenseManager = context.getService( LicenseManager.class ).get();
    }
}
