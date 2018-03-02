package com.enonic.lib.license;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.enonic.lib.license.js.ValidateLicenseScriptTest;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.repository.RepositoryService;
import com.enonic.xp.resource.BytesResource;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.resource.UrlResource;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.User;
import com.enonic.xp.security.auth.AuthenticationInfo;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;

public class LicenseManagerImplTest
{
    private static Path tempDir;

    @Before
    public void setUp()
    {
        initLicenseFolder();
    }

    @After
    public void tearDown()
        throws IOException
    {
        System.clearProperty( "xp.home" );
        FileUtils.deleteDirectory( tempDir.toFile() );
    }

    @Test
    public void generateKeyPair()
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();
        final KeyPair keyPair = licMan.generateKeyPair();

        assertNotNull( keyPair );
        assertNotNull( keyPair.getPrivateKey() );
        assertNotNull( keyPair.getPublicKey() );
    }

    @Test
    public void generateLicense()
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();
        final KeyPair keyPair = licMan.generateKeyPair();

        final LicenseDetails licenseDetails = LicenseDetails.create().issuedTo( "name" ).issuedBy( "org" ).build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        assertNotNull( license );
    }

    @Test
    public void validateLicense()
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();
        final KeyPair keyPair = licMan.generateKeyPair();
        final LicenseDetails licenseDetails = LicenseDetails.create().issuedTo( "name" ).issuedBy( "org" ).build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        final LicenseDetails validLicense = licMan.validateLicense( keyPair.getPublicKey(), license );

        assertNotNull( validLicense );
    }

    @Test
    public void validateExpiredLicense()
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();
        final KeyPair keyPair = licMan.generateKeyPair();
        final LicenseDetails licenseDetails = LicenseDetails.create().
            issuedTo( "name" ).
            issuedBy( "org" ).
            issueTime( Instant.now() ).
            expiryTime( Instant.now().minus( 10, ChronoUnit.MINUTES ) ).
            build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        final LicenseDetails validLicense = licMan.validateLicense( keyPair.getPublicKey(), license );

        assertNotNull( validLicense );
        assertTrue( validLicense.isExpired() );
    }

    @Test
    public void validateLicenseInFile()
        throws Exception
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();
        final ApplicationKey app = ApplicationKey.from( "com.enonic.myapp" );
        licMan.setCurrentApp( app );

        final RepositoryService repoService = Mockito.mock( RepositoryService.class );
        final NodeService nodeService = Mockito.mock( NodeService.class );
        final ResourceService resourceService = Mockito.mock( ResourceService.class );
        licMan.setRepositoryService( repoService );
        licMan.setNodeService( nodeService );
        licMan.setResourceService( resourceService );

        setLicenseFile( "com.enonic.myapp" );

        final ResourceKey key = ResourceKey.from( app, "/app.pub" );
        final URL url = getClass().getResource( "/myapp" + key.getPath() );
        final Resource res = new UrlResource( key, url );
        Mockito.when( resourceService.getResource( eq( key ) ) ).thenReturn( res );

        final LicenseDetails validLicense = licMan.validateLicense( "com.enonic.myapp" );

        assertNotNull( validLicense );
    }

    @Test
    public void validateLicenseInRepo()
        throws Exception
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();
        final ApplicationKey app = ApplicationKey.from( "com.enonic.myapp" );
        licMan.setCurrentApp( app );

        final RepositoryService repoService = Mockito.mock( RepositoryService.class );
        final NodeService nodeService = Mockito.mock( NodeService.class );
        final ResourceService resourceService = Mockito.mock( ResourceService.class );
        licMan.setRepositoryService( repoService );
        licMan.setNodeService( nodeService );
        licMan.setResourceService( resourceService );

        final KeyPair keyPair = licMan.generateKeyPair();

        final ResourceKey key = ResourceKey.from( app, "/app.pub" );
        final BytesResource res = new BytesResource( key, keyPair.getPublicKey().serialize().getBytes( "UTF-8" ) );
        Mockito.when( resourceService.getResource( eq( key ) ) ).thenReturn( res );

        final LicenseDetails licenseDetails = LicenseDetails.create().issuedTo( "name" ).issuedBy( "org" ).build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        final PropertyTree nodeData = new PropertyTree();
        nodeData.setString( "license", license );
        final Node node = Node.create().data( nodeData ).build();
        Mockito.when( nodeService.getByPath( eq( NodePath.create( "/installed-licenses/com.enonic.myapp" ).build() ) ) ).thenReturn( node );

        final LicenseDetails validLicense = licMan.validateLicense( "com.enonic.myapp" );

        assertNotNull( validLicense );
    }

    @Test
    public void installLicense()
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();

        final RepositoryService repoService = Mockito.mock( RepositoryService.class );
        final NodeService nodeService = Mockito.mock( NodeService.class );
        licMan.setRepositoryService( repoService );
        licMan.setNodeService( nodeService );

        final KeyPair keyPair = licMan.generateKeyPair();
        final LicenseDetails licenseDetails = LicenseDetails.create().issuedTo( "name" ).issuedBy( "org" ).build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        final Context ctxAuthenticated = ContextBuilder.from( ContextAccessor.current() ).
            authInfo( AuthenticationInfo.create().principals( RoleKeys.AUTHENTICATED ).user( User.ANONYMOUS ).build() ).
            build();

        final boolean installed =
            ctxAuthenticated.callWith( () -> licMan.installLicense( license, keyPair.getPublicKey(), "com.enonic.myapp" ) );

        assertTrue( installed );
    }

    @Test
    public void installLicenseNotAuthenticated()
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();

        final RepositoryService repoService = Mockito.mock( RepositoryService.class );
        final NodeService nodeService = Mockito.mock( NodeService.class );
        licMan.setRepositoryService( repoService );
        licMan.setNodeService( nodeService );

        final KeyPair keyPair = licMan.generateKeyPair();
        final LicenseDetails licenseDetails = LicenseDetails.create().issuedTo( "name" ).issuedBy( "org" ).build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        final boolean installed = licMan.installLicense( license, keyPair.getPublicKey(), "com.enonic.myapp" );

        assertFalse( installed );
    }

    @Test
    public void installLicenseReplaceExisting()
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();

        final RepositoryService repoService = Mockito.mock( RepositoryService.class );
        final NodeService nodeService = Mockito.mock( NodeService.class );
        licMan.setRepositoryService( repoService );
        licMan.setNodeService( nodeService );

        Mockito.when( nodeService.nodeExists( any( NodePath.class ) ) ).thenReturn( true );

        final KeyPair keyPair = licMan.generateKeyPair();
        final LicenseDetails licenseDetails = LicenseDetails.create().issuedTo( "name" ).issuedBy( "org" ).build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        final Context ctxAuthenticated = ContextBuilder.from( ContextAccessor.current() ).
            authInfo( AuthenticationInfo.create().principals( RoleKeys.AUTHENTICATED ).user( User.ANONYMOUS ).build() ).
            build();

        final boolean installed =
            ctxAuthenticated.callWith( () -> licMan.installLicense( license, keyPair.getPublicKey(), "com.enonic.myapp" ) );

        assertTrue( installed );
    }

    @Test
    public void uninstallLicense()
    {
        final LicenseManagerImpl licMan = new LicenseManagerImpl();

        final RepositoryService repoService = Mockito.mock( RepositoryService.class );
        final NodeService nodeService = Mockito.mock( NodeService.class );
        licMan.setRepositoryService( repoService );
        licMan.setNodeService( nodeService );

        Mockito.when( repoService.isInitialized( any( RepositoryId.class ) ) ).thenReturn( true );

        final Context ctxAuthenticated = ContextBuilder.from( ContextAccessor.current() ).
            authInfo( AuthenticationInfo.create().principals( RoleKeys.AUTHENTICATED ).user( User.ANONYMOUS ).build() ).
            build();

        final boolean uninstalled = ctxAuthenticated.callWith( () -> licMan.uninstallLicense( "com.enonic.myapp" ) );

        assertTrue( uninstalled );
    }

    private void setLicenseFile( final String appKey )
        throws IOException
    {
        final Path licenseFolder = Files.createDirectory( tempDir.resolve( "license" ) );
        final Path licFile = licenseFolder.resolve( appKey + ".lic" );
        File srcFile = new File( LicenseManagerImplTest.class.getClassLoader().getResource( "test/validate_license.txt" ).getFile() );
        Files.copy( srcFile.toPath(), licFile );
    }

    private static void initLicenseFolder()
    {
        try
        {
            final Class<ValidateLicenseScriptTest> clazz = ValidateLicenseScriptTest.class;
            tempDir = Files.createTempDirectory( clazz.getCanonicalName() );
            System.setProperty( "xp.home", tempDir.toAbsolutePath().toString() );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}