package com.enonic.lib.license.js;

import com.enonic.lib.license.LicenseDetails;
import com.enonic.xp.script.serializer.MapGenerator;
import com.enonic.xp.script.serializer.MapSerializable;

public final class LicenseMapper
    implements MapSerializable
{

    private final LicenseDetails license;

    LicenseMapper( final LicenseDetails license )
    {
        this.license = license;
    }

    @Override
    public void serialize( final MapGenerator gen )
    {
        gen.value( "issuedTo", license.getIssuedTo() );
        gen.value( "issuedBy", license.getIssuedBy() );
        gen.value( "expiryTime", license.getExpiryTime() );
        gen.value( "issueTime", license.getIssueTime() );
        gen.value( "expired", license.isExpired() );
        gen.map( "data" );
        license.getProperties().forEach( gen::value );
        gen.end();
    }

}
