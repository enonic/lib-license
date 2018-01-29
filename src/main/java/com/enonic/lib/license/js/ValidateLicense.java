package com.enonic.lib.license.js;

import com.enonic.lib.license.LicenseDetails;
import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.PublicKey;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;

public final class ValidateLicense
    implements ScriptBean
{
    private LicenseManager licenseManager;

    private String publicKey;

    private String license;

    public LicenseMapper validate()
    {
        final PublicKey publicKey = PublicKey.from( this.publicKey );
        if ( publicKey == null )
        {
            throw new IllegalArgumentException( "Invalid public key" );
        }

        final LicenseDetails licDetails = licenseManager.validateLicense( publicKey, license );
        return licDetails == null ? null : new LicenseMapper( licDetails );
    }

    public void setPublicKey( final String publicKey )
    {
        this.publicKey = publicKey;
    }

    public void setLicense( final String license )
    {
        this.license = license;
    }

    @Override
    public void initialize( final BeanContext context )
    {
        this.licenseManager = context.getService( LicenseManager.class ).get();
    }
}
