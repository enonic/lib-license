package com.enonic.lib.license.js;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.LicenseManagerImpl;
import com.enonic.xp.node.NodeService;
import com.enonic.xp.resource.Resource;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.resource.ResourceService;
import com.enonic.xp.testing.ScriptRunnerSupport;

public class ValidateLicenseScriptTest
    extends ScriptRunnerSupport
{
    private static Path tempDir;

    private ResourceService resourceService;

    private NodeService nodeService;

    @BeforeClass
    public static void setUp()
    {
        initLicenseFolder();
    }

    @AfterClass
    public static void tearDown()
        throws IOException
    {
        System.clearProperty( "xp.home" );
        FileUtils.deleteDirectory( tempDir.toFile() );
    }

    @Override
    protected void initialize()
        throws Exception
    {
        setAppKey( "com.enonic.app.myapp" );
        super.initialize();

        final LicenseManager licenseManager = new LicenseManagerImpl();
        addService( LicenseManager.class, licenseManager );
        this.resourceService = Mockito.mock( ResourceService.class );
        addService( ResourceService.class, resourceService );
        this.nodeService = Mockito.mock( NodeService.class );
        addService( NodeService.class, nodeService );

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

    private static void initLicenseFolder()
    {
        try
        {
            final Class<ValidateLicenseScriptTest> clazz = ValidateLicenseScriptTest.class;
            tempDir = Files.createTempDirectory( clazz.getCanonicalName() );
            System.setProperty( "xp.home", tempDir.toAbsolutePath().toString() );

            final Path licenseFolder = Files.createDirectory( tempDir.resolve( "license" ) );
            final Path licFile = licenseFolder.resolve( "com.enonic.app.myapp.lic" );
            File srcFile = new File( clazz.getClassLoader().getResource( "test/validate_license.txt" ).getFile() );
            Files.copy( srcFile.toPath(), licFile );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
}
