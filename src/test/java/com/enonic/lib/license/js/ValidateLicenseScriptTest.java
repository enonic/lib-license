package com.enonic.lib.license.js;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.LicenseManagerImpl;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.testing.ScriptRunnerSupport;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidateLicenseScriptTest
    extends ScriptRunnerSupport
{
    @TempDir
    private static Path tempDir;

    private ResourceService resourceService;

    private NodeService nodeService;

    @BeforeAll
    public static void setUp()
        throws Exception
    {
        System.setProperty( "xp.home", tempDir.toAbsolutePath().toString() );

        final Path licFile = Files.createDirectory( tempDir.resolve( "license" ) ).resolve( "com.enonic.app.myapp.lic" );
        Files.copy( ValidateLicenseScriptTest.class.getClassLoader().getResourceAsStream( "test/validate_license.txt" ), licFile );
    }

    @AfterAll
    public static void tearDown()
        throws IOException
    {
        System.clearProperty( "xp.home" );
    }

    @Override
    protected void initialize()
        throws Exception
    {
        setAppKey( "com.enonic.app.myapp" );
        super.initialize();

        BundleContext bundleContext = mock( BundleContext.class );
        Bundle bundle = mock( Bundle.class );
        when( bundleContext.getBundle() ).thenReturn( bundle );
        when( bundle.getSymbolicName() ).thenReturn( "com.enonic.app.myapp" );
        final LicenseManagerImpl licenseManager = new LicenseManagerImpl( bundleContext );

        addService( LicenseManager.class, licenseManager );
        this.resourceService = Mockito.mock( ResourceService.class );
        this.nodeService = Mockito.mock( NodeService.class );
        licenseManager.setResourceService( resourceService );
        licenseManager.setNodeService( nodeService );
        Mockito.when( resourceService.getResource( Mockito.any() ) ).then( this::getResource );
    }


    @Override
    public String getScriptTestFile()
    {
        return "/test/validatelicense-test.js";
    }

    public String load( final ResourceKey resource )
    {
        return loadResource( resource ).readString();
    }

    private Resource getResource( final InvocationOnMock invocation )
    {
        final ResourceKey key = (ResourceKey) invocation.getArguments()[0];
        return loadResource( ResourceKey.from( key.getApplicationKey(), "/myapp" + key.getPath() ) );
    }
}
