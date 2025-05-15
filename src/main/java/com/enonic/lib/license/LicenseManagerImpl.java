package com.enonic.lib.license;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.branch.Branch;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.home.HomeDir;
import com.enonic.xp.node.CreateNodeParams;
import com.enonic.xp.node.DeleteNodeParams;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.node.UpdateNodeParams;
import com.enonic.xp.repository.CreateRepositoryParams;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.repository.RepositoryService;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.User;
import com.enonic.xp.security.acl.AccessControlEntry;
import com.enonic.xp.security.acl.AccessControlList;
import com.enonic.xp.security.auth.AuthenticationInfo;

@Component(immediate = true, service = LicenseManager.class)
public final class LicenseManagerImpl
    implements LicenseManager
{
    private final static ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion( JsonInclude.Include.NON_NULL );

    private final static Logger LOG = LoggerFactory.getLogger( LicenseManagerImpl.class );

    static final int KEY_SIZE = 2048;

    private static final String LICENSE_HEADER = "LICENSE";

    private static final String LIC_FILE_EXT = ".lic";

    private static final String LICENSE_DIR = "license";

    public static final RepositoryId REPO_ID = RepositoryId.from( "system-repo" );

    public static final PrincipalKey MANAGER_ROLE = PrincipalKey.ofRole( "com.enonic.app.licensemanager" );

    private static final String INSTALLED_LICENSES = "licenses";

    public static final NodePath INSTALLED_LICENSES_PATH = NodePath.create( NodePath.ROOT).addElement(  INSTALLED_LICENSES ).build();

    public static final String NODE_LICENSE_PROPERTY = "license";

    private NodeService nodeService;

    private RepositoryService repositoryService;

    private ResourceService resourceService;

    private final ApplicationKey currentApp;

    private final Cache<String, Optional<LicenseDetails>> appValidationCache;

    public LicenseManagerImpl( BundleContext context )
    {
        this.currentApp = ApplicationKey.from( context.getBundle() );
        this.appValidationCache = CacheBuilder.newBuilder().maximumSize( 10 ).expireAfterWrite( 20, TimeUnit.SECONDS ).build();
    }

    @Override
    public KeyPair generateKeyPair()
    {
        final KeyPairGenerator keyGen = getRSAKeyPairGenerator();
        keyGen.initialize( KEY_SIZE );
        final java.security.KeyPair rsaKeyPair = keyGen.genKeyPair();
        return new KeyPair( rsaKeyPair );
    }

    public String generateLicense( final com.enonic.lib.license.PrivateKey privateKey, final LicenseDetails license )
    {
        String result;
        try
        {
            result = MAPPER.writeValueAsString( new LicenseDetailsJson( license ) );
        }
        catch ( JsonProcessingException e )
        {
            throw new IllegalArgumentException( "Cannot serialize license", e );
        }
        String licenseData = result;
        licenseData = Base64.getEncoder().withoutPadding().encodeToString( licenseData.getBytes( StandardCharsets.UTF_8 ) );

        final String signature = sign( licenseData, privateKey.getRsaKey() );
        return FormatHelper.asPEM( licenseData + "." + signature, LICENSE_HEADER );
    }

    @Override
    public LicenseDetails validateLicense( final com.enonic.lib.license.PublicKey publicKey, final String license )
    {
        final String licenseStr = unwrapLicense( license );
        final int p = licenseStr.indexOf( '.' );
        if ( p == -1 )
        {
            return null;
        }
        final String licenseData = licenseStr.substring( 0, p );
        final String signature = licenseStr.substring( p + 1 );

        final boolean valid = verify( licenseData, signature, publicKey.getRsaKey() );
        if ( !valid )
        {
            return null;
        }

        try
        {
            final byte[] licenseDataBytes = Base64.getUrlDecoder().decode( licenseData );
            final String licenseDataJson = new String( licenseDataBytes, StandardCharsets.UTF_8 );

            final LicenseDetailsJson licenseDetailsJson = MAPPER.readValue( licenseDataJson, LicenseDetailsJson.class );
            return licenseDetailsJson.toLicense();
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    @Override
    public LicenseDetails validateLicense( final String appKey )
    {
        try
        {
            final Optional<LicenseDetails> licenseValue = appValidationCache.get( appKey, () -> {
                final LicenseDetails license = doValidateLicense( appKey );
                return license == null ? Optional.empty() : Optional.of( license );
            } );
            return licenseValue.orElse( null );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
    }

    private LicenseDetails doValidateLicense( String appKey )
    {
        final ApplicationKey app = this.currentApp;
        final Resource pubKeyRes = this.resourceService.getResource( ResourceKey.from( app, "/app.pub" ) );
        if ( !pubKeyRes.exists() )
        {
            return null;
        }

        String publicKeyStr = pubKeyRes.readString();
        final com.enonic.lib.license.PublicKey publicKey = com.enonic.lib.license.PublicKey.from( publicKeyStr );
        if ( publicKey == null )
        {
            return null;
        }

        if ( appKey == null )
        {
            appKey = app.toString();
        }

        String license = readLicenseFile( appKey );
        if ( license == null )
        {
            license = readLicenseFromRepo( appKey );
        }

        if ( license == null )
        {
            return null;
        }

        return this.validateLicense( publicKey, license );
    }

    @Override
    public boolean installLicense( final String license, final com.enonic.lib.license.PublicKey publicKey, final String appKey )
    {
        final boolean installed = doInstallLicense( license, publicKey, appKey );
        appValidationCache.invalidate( appKey );
        return installed;
    }

    private boolean doInstallLicense( final String license, final com.enonic.lib.license.PublicKey publicKey, final String appKey )
    {
        final LicenseDetails licDetails = this.validateLicense( publicKey, license );
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
        ctxRepo.runWith( () -> this.storeLicense( license, appKey ) );

        return true;
    }

    @Override
    public boolean uninstallLicense( final String appKey )
    {
        final boolean uninstalled = doUninstallLicense( appKey );
        appValidationCache.invalidate( appKey );
        return uninstalled;
    }

    private boolean doUninstallLicense( final String appKey )
    {
        final Context currentCtx = ContextAccessor.current();
        if ( !currentCtx.getAuthInfo().hasRole( RoleKeys.AUTHENTICATED ) )
        {
            LOG.warn( "License could not be uninstalled, user not authenticated" );
            return false;
        }

        final Context ctx = ContextBuilder.from( currentCtx ).
            repositoryId( REPO_ID ).
            branch( Branch.from( "master" ) ).
            authInfo( AuthenticationInfo.create().principals( RoleKeys.ADMIN ).user( User.ANONYMOUS ).build() ).
            build();

        ctx.runWith( () -> {
            if ( repositoryService.isInitialized( REPO_ID ) )
            {
                deleteLicense( appKey );
            }
        } );
        return true;
    }

    private void deleteLicense( final String appKey )
    {
        final NodePath path = NodePath.create( INSTALLED_LICENSES_PATH).addElement( appKey ).build();
        nodeService.delete( DeleteNodeParams.create().nodePath( path ).build() );
    }

    private void storeLicense( final String license, final String appKey )
    {
        PropertyTree data = new PropertyTree();
        data.setString( NODE_LICENSE_PROPERTY, license );

        final NodePath path = NodePath.create( INSTALLED_LICENSES_PATH).addElement( appKey ).build();
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

    private boolean initializeRepo()
    {
        if ( !repositoryService.isInitialized( REPO_ID ) )
        {
            final AccessControlList acl = AccessControlList.create().
                add( AccessControlEntry.create().
                    principal( RoleKeys.ADMIN ).
                    allowAll().
                    build() ).
                add( AccessControlEntry.create().
                    principal( MANAGER_ROLE ).
                    allowAll().
                    build() ).
                build();

            final CreateRepositoryParams createRepo = CreateRepositoryParams.create().
                repositoryId( REPO_ID ).
                rootPermissions( acl ).
                build();
            repositoryService.createRepository( createRepo );
        }

        try
        {
            if ( nodeService.nodeExists( INSTALLED_LICENSES_PATH ) )
            {
                return true;
            }
            final CreateNodeParams createNode = CreateNodeParams.create().
                name( INSTALLED_LICENSES_PATH.getName() ).
                inheritPermissions( true ).
                parent( NodePath.ROOT ).
                build();

            nodeService.create( createNode );

            return true;
        }
        catch ( Exception e )
        {
            LOG.warn( "Could not initialize license repo", e );
            return false;
        }
    }

    private String unwrapLicense( final String license )
    {
        if ( license == null )
        {
            return "";
        }
        final String allLines[] = license.split( "\\r?\\n" );
        final List<String> lines = Arrays.stream( allLines ).
            map( String::trim ).
            filter( s -> !s.isEmpty() ).
            collect( Collectors.toList() );
        if ( lines.size() < 3 )
        {
            return "";
        }

        final String header = lines.get( 0 );
        final String footer = lines.get( lines.size() - 1 );
        lines.remove( 0 );
        lines.remove( lines.size() - 1 );
        if ( !FormatHelper.isPEMHeader( header, LICENSE_HEADER ) || !FormatHelper.isPEMFooter( footer, LICENSE_HEADER ) )
        {
            return "";
        }

        return lines.stream().collect( Collectors.joining( "" ) );
    }

    private String sign( String plainText, PrivateKey privateKey )
    {
        try
        {
            final Signature privateSignature = Signature.getInstance( "SHA256withRSA" );
            privateSignature.initSign( privateKey );
            privateSignature.update( plainText.getBytes( StandardCharsets.UTF_8 ) );

            final byte[] signature = privateSignature.sign();
            return Base64.getEncoder().withoutPadding().encodeToString( signature );
        }
        catch ( GeneralSecurityException e )
        {
            throw new RuntimeException( e );
        }
    }

    private boolean verify( String plainText, String signature, PublicKey publicKey )
    {
        try
        {
            Signature publicSignature = Signature.getInstance( "SHA256withRSA" );
            publicSignature.initVerify( publicKey );
            publicSignature.update( plainText.getBytes( StandardCharsets.UTF_8 ) );

            byte[] signatureBytes = Base64.getDecoder().decode( signature );

            return publicSignature.verify( signatureBytes );
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    private KeyPairGenerator getRSAKeyPairGenerator()
    {
        try
        {
            return KeyPairGenerator.getInstance( "RSA" );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( e );
        }
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
            repositoryId( REPO_ID ).
            branch( Branch.from( "master" ) ).
            authInfo( AuthenticationInfo.create().principals( RoleKeys.ADMIN ).user( User.ANONYMOUS ).build() ).
            build();
        return ctxRepo.callWith( () -> loadLicense( appKey ) );
    }

    private String loadLicense( final String appKey )
    {
        try
        {
            final NodePath path = NodePath.create( INSTALLED_LICENSES_PATH).addElement( appKey ).build();
            final Node licenseNode = nodeService.getByPath( path );
            if ( licenseNode == null )
            {
                return null;
            }
            return licenseNode.data().getString( NODE_LICENSE_PROPERTY );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    @Reference
    public void setNodeService( final NodeService nodeService )
    {
        this.nodeService = nodeService;
    }

    @Reference
    public void setResourceService( final ResourceService resourceService )
    {
        this.resourceService = resourceService;
    }

    @Reference
    public void setRepositoryService( final RepositoryService repositoryService )
    {
        this.repositoryService = repositoryService;
    }
}
