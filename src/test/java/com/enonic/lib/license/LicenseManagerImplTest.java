package com.enonic.lib.license;

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
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.resource.UrlResource;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.User;
import com.enonic.xp.security.auth.AuthenticationInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.Mock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LicenseManagerImplTest
{
    @TempDir
    static Path tempDir;

    static final ApplicationKey app = ApplicationKey.from( "com.enonic.myapp" );

    @BeforeAll
    public static void setUp()
            throws Exception
    {
        System.setProperty( "xp.home", tempDir.toAbsolutePath().toString() );
    }

    @AfterAll
    public static void tearDown()
            throws IOException
    {
        System.clearProperty( "xp.home" );
    }

    @Test
    public void generateKeyPair()
    {
        final BundleContext bundleContext = mock( BundleContext.class, Answers.RETURNS_DEEP_STUBS );
        when( bundleContext.getBundle().getSymbolicName() ).thenReturn( app.getName() );
        final LicenseManagerImpl licMan = new LicenseManagerImpl( bundleContext );

        final KeyPair keyPair = licMan.generateKeyPair();

        assertNotNull( keyPair );
        assertNotNull( keyPair.getPrivateKey() );
        assertNotNull( keyPair.getPublicKey() );
    }

    @Test
    public void generateLicense()
    {
        final BundleContext bundleContext = mock( BundleContext.class, Answers.RETURNS_DEEP_STUBS );
        when( bundleContext.getBundle().getSymbolicName() ).thenReturn( app.getName() );
        final LicenseManagerImpl licMan = new LicenseManagerImpl( bundleContext );

        final KeyPair keyPair = licMan.generateKeyPair();

        final LicenseDetails licenseDetails = LicenseDetails.create().issuedTo( "name" ).issuedBy( "org" ).build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        assertNotNull( license );
    }

    @Test
    public void validateLicense()
    {
        final BundleContext bundleContext = mock( BundleContext.class, Answers.RETURNS_DEEP_STUBS );
        when( bundleContext.getBundle().getSymbolicName() ).thenReturn( app.getName() );
        final LicenseManagerImpl licMan = new LicenseManagerImpl( bundleContext );

        final KeyPair keyPair = licMan.generateKeyPair();
        final LicenseDetails licenseDetails = LicenseDetails.create().issuedTo( "name" ).issuedBy( "org" ).build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        final LicenseDetails validLicense = licMan.validateLicense( keyPair.getPublicKey(), license );

        assertNotNull( validLicense );
    }

    @Test
    public void validateExpiredLicense()
    {
        final BundleContext bundleContext = mock( BundleContext.class, Answers.RETURNS_DEEP_STUBS );
        when( bundleContext.getBundle().getSymbolicName() ).thenReturn( app.getName() );
        final LicenseManagerImpl licMan = new LicenseManagerImpl( bundleContext );

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
        final BundleContext bundleContext = mock( BundleContext.class, Answers.RETURNS_DEEP_STUBS );
        when( bundleContext.getBundle().getSymbolicName() ).thenReturn( app.getName() );
        final LicenseManagerImpl licMan = new LicenseManagerImpl( bundleContext );

        final ApplicationKey app = ApplicationKey.from( "com.enonic.myapp" );

        final RepositoryService repoService = mock( RepositoryService.class );
        final NodeService nodeService = mock( NodeService.class );
        final ResourceService resourceService = mock( ResourceService.class );
        licMan.setRepositoryService( repoService );
        licMan.setNodeService( nodeService );
        licMan.setResourceService( resourceService );


        final Path licFile = Files.createDirectory( tempDir.resolve( "license" ) ).resolve( app + ".lic" );
        Files.copy( LicenseManagerImplTest.class.getClassLoader().getResourceAsStream( "test/validate_license.txt" ), licFile );

        final ResourceKey key = ResourceKey.from( app, "/app.pub" );
        final URL url = getClass().getResource( "/myapp" + key.getPath() );
        final Resource res = new UrlResource( key, url );
        when( resourceService.getResource( eq( key ) ) ).thenReturn( res );

        final LicenseDetails validLicense = licMan.validateLicense( "com.enonic.myapp" );

        assertNotNull( validLicense );
    }

    @Test
    public void validateLicenseInRepo()
        throws Exception
    {
        Files.deleteIfExists(tempDir.resolve( "license" ).resolve( app + ".lic" ) );

        final BundleContext bundleContext = mock( BundleContext.class, Answers.RETURNS_DEEP_STUBS );
        when( bundleContext.getBundle().getSymbolicName() ).thenReturn( app.getName() );
        final LicenseManagerImpl licMan = new LicenseManagerImpl( bundleContext );

        final RepositoryService repoService = mock( RepositoryService.class );
        final NodeService nodeService = mock( NodeService.class );
        final ResourceService resourceService = mock( ResourceService.class );
        licMan.setRepositoryService( repoService );
        licMan.setNodeService( nodeService );
        licMan.setResourceService( resourceService );

        final KeyPair keyPair = licMan.generateKeyPair();

        final ResourceKey key = ResourceKey.from( app, "/app.pub" );
        final Resource resource = mock( Resource.class );
        when( resource.exists() ).thenReturn( true );
        when( resource.readString() ).thenReturn( keyPair.getPublicKey().serialize() );

        when( resourceService.getResource( eq( key ) ) ).thenReturn( resource );

        final LicenseDetails licenseDetails = LicenseDetails.create().issuedTo( "name" ).issuedBy( "org" ).build();
        final String license = licMan.generateLicense( keyPair.getPrivateKey(), licenseDetails );

        final PropertyTree nodeData = new PropertyTree();
        nodeData.setString( "license", license );
        final Node node = Node.create().data( nodeData ).build();
        when( nodeService.getByPath( eq( NodePath.create().addElement( "licenses" ).addElement( "com.enonic.myapp" ).build() ) ) ).thenReturn( node );

        final LicenseDetails validLicense = licMan.validateLicense( "com.enonic.myapp" );

        assertNotNull( validLicense );
    }

    @Test
    public void installLicense()
    {
        final BundleContext bundleContext = mock( BundleContext.class, Answers.RETURNS_DEEP_STUBS );
        when( bundleContext.getBundle().getSymbolicName() ).thenReturn( app.getName() );
        final LicenseManagerImpl licMan = new LicenseManagerImpl( bundleContext );

        final RepositoryService repoService = mock( RepositoryService.class );
        final NodeService nodeService = mock( NodeService.class );
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
        final BundleContext bundleContext = mock( BundleContext.class, Answers.RETURNS_DEEP_STUBS );
        when( bundleContext.getBundle().getSymbolicName() ).thenReturn( app.getName() );
        final LicenseManagerImpl licMan = new LicenseManagerImpl( bundleContext );

        final RepositoryService repoService = mock( RepositoryService.class );
        final NodeService nodeService = mock( NodeService.class );
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
        final BundleContext bundleContext = mock( BundleContext.class, Answers.RETURNS_DEEP_STUBS );
        when( bundleContext.getBundle().getSymbolicName() ).thenReturn( app.getName() );
        final LicenseManagerImpl licMan = new LicenseManagerImpl( bundleContext );

        final RepositoryService repoService = mock( RepositoryService.class );
        final NodeService nodeService = mock( NodeService.class );
        licMan.setRepositoryService( repoService );
        licMan.setNodeService( nodeService );

        when( nodeService.nodeExists( any( NodePath.class ) ) ).thenReturn( true );

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
        final BundleContext bundleContext = mock( BundleContext.class, Answers.RETURNS_DEEP_STUBS );
        when( bundleContext.getBundle().getSymbolicName() ).thenReturn( app.getName() );
        final LicenseManagerImpl licMan = new LicenseManagerImpl( bundleContext );

        final RepositoryService repoService = mock( RepositoryService.class );
        final NodeService nodeService = mock( NodeService.class );
        licMan.setRepositoryService( repoService );
        licMan.setNodeService( nodeService );

        when( repoService.isInitialized( any( RepositoryId.class ) ) ).thenReturn( true );

        final Context ctxAuthenticated = ContextBuilder.from( ContextAccessor.current() ).
            authInfo( AuthenticationInfo.create().principals( RoleKeys.AUTHENTICATED ).user( User.ANONYMOUS ).build() ).
            build();

        final boolean uninstalled = ctxAuthenticated.callWith( () -> licMan.uninstallLicense( "com.enonic.myapp" ) );

        assertTrue( uninstalled );
    }
}
