package com.enonic.lib.license.js;

import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.LicenseManagerImpl;
import com.enonic.xp.resource.ResourceKey;
import com.enonic.xp.testing.ScriptRunnerSupport;

public class ValidateLicenseScriptTest
    extends ScriptRunnerSupport
{

    @Override
    protected void initialize()
        throws Exception
    {
        super.initialize();

        final LicenseManager licenseManager = new LicenseManagerImpl();
        addService( LicenseManager.class, licenseManager );
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
}
