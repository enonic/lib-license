package com.enonic.lib.license.js;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.lib.license.LicenseDetails;
import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.PublicKey;
import com.enonic.xp.branch.Branch;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.node.CreateNodeParams;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.node.UpdateNodeParams;
import com.enonic.xp.repository.CreateRepositoryParams;
import com.enonic.xp.repository.Repository;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.repository.RepositoryService;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.User;
import com.enonic.xp.security.acl.AccessControlEntry;
import com.enonic.xp.security.acl.AccessControlList;
import com.enonic.xp.security.auth.AuthenticationInfo;

import static com.enonic.xp.security.acl.Permission.CREATE;
import static com.enonic.xp.security.acl.Permission.DELETE;
import static com.enonic.xp.security.acl.Permission.MODIFY;
import static com.enonic.xp.security.acl.Permission.PUBLISH;
import static com.enonic.xp.security.acl.Permission.READ;
import static com.enonic.xp.security.acl.Permission.READ_PERMISSIONS;
import static com.enonic.xp.security.acl.Permission.WRITE_PERMISSIONS;

public final class InstallLicense
    implements ScriptBean
{
    private final static Logger LOG = LoggerFactory.getLogger( InstallLicense.class );

    static final RepositoryId REPO_ID = RepositoryId.from( "com.enonic.licensemanager" );

    static final String INSTALLED_LICENSES = "installed-licenses";

    static final NodePath INSTALLED_LICENSES_PATH = NodePath.create( NodePath.ROOT, INSTALLED_LICENSES ).build();

    static final String NODE_LICENSE_PROPERTY = "license";

    private LicenseManager licenseManager;

    private RepositoryService repositoryService;

    private NodeService nodeService;

    private String publicKey;

    private String license;

    private String appKey;

    public boolean install()
    {
        final PublicKey publicKey = PublicKey.from( this.publicKey );
        if ( publicKey == null )
        {
            throw new IllegalArgumentException( "Invalid public key" );
        }

        final LicenseDetails licDetails = licenseManager.validateLicense( publicKey, license );
        if ( licDetails == null )
        {
            return false;
        }

        final Context currentCtx = ContextAccessor.current();
        if ( !currentCtx.getAuthInfo().hasRole( RoleKeys.AUTHENTICATED ) )
        {
            LOG.warn( "License could not be installed, user not authenticated" );
            return false;
        }

        final Context ctxSudo = ContextBuilder.from( currentCtx ).
            repositoryId( REPO_ID ).
            branch( Branch.from( "master" ) ).
            authInfo( AuthenticationInfo.create().principals( RoleKeys.ADMIN ).user( User.ANONYMOUS ).build() ).
            build();

        if ( !ctxSudo.callWith( this::initializeRepo ) )
        {
            return false;
        }

        final Context ctxRepo = ContextBuilder.from( currentCtx ).
            repositoryId( REPO_ID ).
            branch( Branch.from( "master" ) ).
            build();
        ctxRepo.runWith( this::storeLicense );

        return true;
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
            repositoryId( REPO_ID ).
            branch( Branch.from( "master" ) ).
            authInfo( AuthenticationInfo.create().principals( RoleKeys.ADMIN ).user( User.ANONYMOUS ).build() ).
            build();

        ctx.runWith( () -> {
            if ( repositoryService.isInitialized( REPO_ID ) )
            {
                deleteLicense();
            }
        } );
    }

    private void storeLicense()
    {
        PropertyTree data = new PropertyTree();
        data.setString( NODE_LICENSE_PROPERTY, this.license );

        final NodePath path = NodePath.create( INSTALLED_LICENSES_PATH, appKey ).build();
        if ( nodeService.nodeExists( path ) )
        {
            final UpdateNodeParams updateNode = UpdateNodeParams.create().
                path( path ).
                editor( node -> node.data = data ).
                build();
            nodeService.update( updateNode );
            return;
        }

        final CreateNodeParams createNode = CreateNodeParams.create().
            parent( INSTALLED_LICENSES_PATH ).
            name( appKey ).
            data( data ).
            inheritPermissions( true ).
            build();
        nodeService.create( createNode );
    }

    private void deleteLicense()
    {
        final NodePath path = NodePath.create( INSTALLED_LICENSES_PATH, appKey ).build();
        nodeService.deleteByPath( path );
    }

    private boolean initializeRepo()
    {
        if ( !repositoryService.isInitialized( REPO_ID ) )
        {
            final AccessControlList acl = AccessControlList.create().
                add( AccessControlEntry.create().
                    principal( RoleKeys.AUTHENTICATED ).
                    allow( READ, CREATE, MODIFY, DELETE, PUBLISH, READ_PERMISSIONS, WRITE_PERMISSIONS ).
                    build() ).
                build();

            final CreateRepositoryParams createRepo = CreateRepositoryParams.create().
                repositoryId( REPO_ID ).
                rootPermissions( acl ).
                build();
            final Repository repo = repositoryService.createRepository( createRepo );
        }

        try
        {
            if ( nodeService.nodeExists( INSTALLED_LICENSES_PATH ) )
            {
                return true;
            }
            final CreateNodeParams createNode = CreateNodeParams.create().
                name( INSTALLED_LICENSES ).
                inheritPermissions( true ).
                parent( NodePath.ROOT ).
                build();

            final Node node = nodeService.create( createNode );

            return true;
        }
        catch ( Exception e )
        {
            LOG.warn( "Could not initialize license repo", e );
            return false;
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

    public void setAppKey( final String appKey )
    {
        this.appKey = appKey;
    }

    @Override
    public void initialize( final BeanContext context )
    {
        this.licenseManager = context.getService( LicenseManager.class ).get();
        this.repositoryService = context.getService( RepositoryService.class ).get();
        this.nodeService = context.getService( NodeService.class ).get();
    }
}
