package com.enonic.lib.license.js;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.enonic.lib.license.LicenseDetails;
import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.PublicKey;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.branch.Branch;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.home.HomeDir;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;

import static com.enonic.lib.license.js.InstallLicense.INSTALLED_LICENSES_PATH;

public final class ValidateLicense
    implements ScriptBean
{

    private static final String LIC_FILE_EXT = ".lic";

    private static final String LICENSE_DIR = "license";

    private NodeService nodeService;

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

            this.license = readLicenseFile( currentApp.toString() );
            if ( this.license == null )
            {
                this.license = readLicenseFromRepo( currentApp.toString() );
            }
        }

        if ( this.license == null )
        {
            return null;
        }

        final LicenseDetails licDetails = licenseManager.validateLicense( publicKey, this.license );
        return licDetails == null ? null : new LicenseMapper( licDetails );
    }

    private String readLicenseFile( final String appKey )
    {
        final String fileName = appKey + LIC_FILE_EXT;
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

    private String readLicenseFromRepo( final String appKey )
    {
        final Context ctxRepo = ContextBuilder.from( ContextAccessor.current() ).
            repositoryId( InstallLicense.REPO_ID ).
            branch( Branch.from( "master" ) ).
            build();
        return ctxRepo.callWith( () -> loadLicense( appKey ) );
    }

    private String loadLicense( final String appKey )
    {
        try
        {
            final NodePath path = NodePath.create( INSTALLED_LICENSES_PATH, appKey ).build();
            final Node licenseNode = nodeService.getByPath( path );
            if ( licenseNode == null )
            {
                return null;
            }
            return licenseNode.data().getString( InstallLicense.NODE_LICENSE_PROPERTY );
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
        this.nodeService = context.getService( NodeService.class ).get();
    }
}
