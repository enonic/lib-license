package com.enonic.lib.license.js;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.enonic.lib.license.LicenseDetails;
import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.PrivateKey;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;

public final class GenerateLicense
    implements ScriptBean
{
    private LicenseManager licenseManager;

    private String issuedBy;

    private String issuedTo;

    private Instant issueTime;

    private Instant expiryTime;

    private Map<String, String> properties;

    private String privateKey;

    public String generate()
    {
        final PrivateKey privateKey = PrivateKey.from( this.privateKey );
        if ( privateKey == null )
        {
            throw new IllegalArgumentException( "Invalid private key" );
        }

        final LicenseDetails licDetails = LicenseDetails.create().
            issuedBy( this.issuedBy ).
            issuedTo( this.issuedTo ).
            expiryTime( this.expiryTime ).
            issueTime( this.issueTime ).
            properties( this.properties ).
            build();
        return licenseManager.generateLicense( privateKey, licDetails );
    }

    public void setIssuedBy( final String issuedBy )
    {
        this.issuedBy = issuedBy;
    }

    public void setIssuedTo( final String issuedTo )
    {
        this.issuedTo = issuedTo;
    }

    public void setIssueTime( final String issueTime )
    {
        this.issueTime = issueTime != null ? Instant.parse( issueTime ) : null;
    }

    public void setExpiryTime( final String expiryTime )
    {
        this.expiryTime = expiryTime != null ? Instant.parse( expiryTime ) : null;
    }

    public void setProperties( final Map<String, Object> properties )
    {
        this.properties = filterMap( properties );
    }

    public void setPrivateKey( final String privateKey )
    {
        this.privateKey = privateKey;
    }

    private Map<String, String> filterMap( final Map<String, Object> values )
    {
        if ( values == null )
        {
            return null;
        }
        final Map<String, String> filtered = new HashMap<>();
        values.forEach( ( k, v ) -> {
            if ( v != null )
            {
                filtered.put( k, v.toString() );
            }
        } );
        return filtered;
    }

    @Override
    public void initialize( final BeanContext context )
    {
        this.licenseManager = context.getService( LicenseManager.class ).get();
    }
}
