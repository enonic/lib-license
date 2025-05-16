package com.enonic.lib.license.js;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.LicenseManagerImpl;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.testing.ScriptRunnerSupport;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenerateLicenseScriptTest
    extends ScriptRunnerSupport
{

    @Override
    protected void initialize()
        throws Exception
    {
        super.initialize();

        BundleContext bundleContext = mock( BundleContext.class );
        Bundle bundle = mock( Bundle.class );
        when( bundleContext.getBundle() ).thenReturn( null );
        when( bundle.getSymbolicName() ).thenReturn( "com.enonic.myapp" );
        final LicenseManagerImpl licenseManager = new LicenseManagerImpl( bundleContext );

        addService( LicenseManager.class, licenseManager );
    }

    @Override
    public String getScriptTestFile()
    {
        return "/test/generatelicense-test.js";
    }

    public String load( final ResourceKey resource )
    {
        return loadResource( resource ).readString();
    }
}
