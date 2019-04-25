package com.enonic.lib.license.js;

import com.enonic.lib.license.LicenseDetails;
import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.PublicKey;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;

public final class ValidateLicense
    implements ScriptBean
{

    private LicenseManager licenseManager;

    private ResourceService resourceService;

    private String publicKey;

    private String license;

    private String app;

    private ResourceKey publicKeyResource;

    public LicenseMapper validate()
    {
        if ( app != null && license == null)
        {
            final LicenseDetails licDetails = licenseManager.validateLicense( app );
            if ( licDetails != null )
            {
                return new LicenseMapper( licDetails );
            }
        }

        if ( this.license == null )
        {
            return null;
        }

        if ( this.publicKey == null && this.publicKeyResource != null )
        {
            final Resource pubKeyRes = this.resourceService.getResource( publicKeyResource );
            if ( pubKeyRes.exists() )
            {
                this.publicKey = pubKeyRes.readString();
            }
        }
        final PublicKey publicKey = PublicKey.from( this.publicKey );
        if ( publicKey == null )
        {
            return null;
        }

        if ( app == null )
        {
            app = ApplicationKey.from( getClass() ).toString();
        }

        final LicenseDetails licDetails = licenseManager.validateLicense( publicKey, this.license );
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

    public void setApp( final String app )
    {
        this.app = app;
    }

    public void setPublicKeyResource( final ResourceKey publicKeyResource )
    {
        this.publicKeyResource = publicKeyResource;
    }

    @Override
    public void initialize( final BeanContext context )
    {
        this.licenseManager = context.getService( LicenseManager.class ).get();
        this.resourceService = context.getService( ResourceService.class ).get();
    }
}
