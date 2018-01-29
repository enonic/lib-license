package com.enonic.lib.license.js;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.enonic.lib.license.LicenseDetails;
import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.PublicKey;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.home.HomeDir;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;

public final class ValidateLicense
    implements ScriptBean
{

    private static final String LIC_FILE_EXT = ".lic";

    private static final String LICENSE_DIR = "license";

    private LicenseManager licenseManager;

    private ResourceService resourceService;

    private String publicKey;

    private String license;

    private String app;

    private ResourceKey publicKeyResource;

    public LicenseMapper validate()
    {
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
            throw new IllegalArgumentException( "Public key not found" );
        }

        if ( license == null )
        {
            final ApplicationKey currentApp;
            if ( this.app == null )
            {
                currentApp = ApplicationKey.from( getClass() );
            }
            else
            {
                currentApp = ApplicationKey.from( this.app );
            }

            this.license = readLicenseFile( currentApp );
        }

        final LicenseDetails licDetails = licenseManager.validateLicense( publicKey, license );
        return licDetails == null ? null : new LicenseMapper( licDetails );
    }

    private String readLicenseFile( final ApplicationKey app )
    {
        final String fileName = app.toString() + LIC_FILE_EXT;
        final Path xpHome = HomeDir.get().toFile().toPath();
        final Path licenseDir = xpHome.resolve( LICENSE_DIR );
        final Path licensePath = licenseDir.resolve( fileName );
        if ( !licensePath.toFile().isFile() )
        {
            return null;
        }
        try
        {
            return new String( Files.readAllBytes( licensePath ), StandardCharsets.UTF_8 );
        }
        catch ( Exception e )
        {
            return null;
        }
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
