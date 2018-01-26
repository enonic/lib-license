package com.enonic.lib.license.js;

import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.LicenseManagerImpl;
import com.enonic.xp.testing.ScriptRunnerSupport;

public class GenerateLicenseScriptTest
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
        return "/test/generatelicense-test.js";
    }

}
