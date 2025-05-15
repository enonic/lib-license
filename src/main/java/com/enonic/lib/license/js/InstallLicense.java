package com.enonic.lib.license.js;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.LicenseManagerImpl;
import com.enonic.lib.license.PublicKey;
import com.enonic.xp.branch.Branch;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.node.DeleteNodeParams;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.repository.RepositoryService;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.User;
import com.enonic.xp.security.auth.AuthenticationInfo;

public final class InstallLicense
    implements ScriptBean
{
    private final static Logger LOG = LoggerFactory.getLogger( InstallLicense.class );

    private LicenseManager licenseManager;

    private RepositoryService repositoryService;

    private NodeService nodeService;

    private ResourceService resourceService;

    private String publicKey;

    private String license;

    private String appKey;

    private ResourceKey publicKeyResource;

    public boolean install()
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
            throw new IllegalArgumentException( "Invalid public key" );
        }

        return licenseManager.installLicense( license, publicKey, appKey );
    }

    public void uninstall()
    {
        final Context currentCtx = ContextAccessor.current();
        if ( !currentCtx.getAuthInfo().hasRole( RoleKeys.AUTHENTICATED ) )
        {
            LOG.warn( "License could not be uninstalled, user not authenticated" );
            return;
        }

        final Context ctx = ContextBuilder.from( currentCtx ).
            repositoryId( LicenseManagerImpl.REPO_ID ).
            branch( Branch.from( "master" ) ).
            authInfo( AuthenticationInfo.create().principals( RoleKeys.ADMIN ).user( User.ANONYMOUS ).build() ).
            build();

        ctx.runWith( () -> {
            if ( repositoryService.isInitialized( LicenseManagerImpl.REPO_ID ) )
            {
                deleteLicense();
            }
        } );
    }

    private void deleteLicense()
    {
        final NodePath path = NodePath.create( LicenseManagerImpl.INSTALLED_LICENSES_PATH ).addElement( appKey ).build();
        nodeService.delete( DeleteNodeParams.create().nodePath( path ).build() );
    }

    public void setPublicKey( final String publicKey )
    {
        this.publicKey = publicKey;
    }

    public void setLicense( final String license )
    {
        this.license = license;
    }

    public void setAppKey( final String appKey )
    {
        this.appKey = appKey;
    }

    public void setPublicKeyResource( final ResourceKey publicKeyResource )
    {
        this.publicKeyResource = publicKeyResource;
    }

    @Override
    public void initialize( final BeanContext context )
    {
        this.licenseManager = context.getService( LicenseManager.class ).get();
        this.repositoryService = context.getService( RepositoryService.class ).get();
        this.resourceService = context.getService( ResourceService.class ).get();
        this.nodeService = context.getService( NodeService.class ).get();
    }
}
