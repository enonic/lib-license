package com.enonic.lib.license.js;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import com.enonic.lib.license.LicenseDetails;
import com.enonic.lib.license.LicenseManager;
import com.enonic.lib.license.PrivateKey;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;

public final class GenerateLicense
    implements ScriptBean
{
    private LicenseManager licenseManager;

    private String name;

    private String organization;

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
            name( this.name ).
            organization( this.organization ).
            expiryTime( this.expiryTime ).
            issueTime( this.issueTime ).
            properties( this.properties ).
            build();
        return licenseManager.generateLicense( privateKey, licDetails );
    }

    public void setName( final String name )
    {
        this.name = name;
    }

    public void setOrganization( final String organization )
    {
        this.organization = organization;
    }

    public void setIssueTime( final Object issueTime )
    {
        this.issueTime = toInstant( issueTime );
    }

    public void setExpiryTime( final Object expiryTime )
    {
        this.expiryTime = toInstant( expiryTime );
    }

    public void setProperties( final Map<String, Object> properties )
    {
        this.properties = filterMap( properties );
    }

    public void setPrivateKey( final String privateKey )
    {
        this.privateKey = privateKey;
    }


    private Instant toInstant( final Object value )
    {
        if ( value instanceof Instant )
        {
            return (Instant) value;
        }
        if ( value instanceof Date )
        {
            return ( (Date) value ).toInstant();
        }
        if ( value instanceof String )
        {
            return stringToInstant( (String) value );
        }
        if ( value instanceof ScriptObjectMirror )
        {
            try
            {
                final Long epochMilli = ( (ScriptObjectMirror) value ).to( Long.class );
                return Instant.ofEpochMilli( epochMilli );
            }
            catch ( Exception e )
            {
                return null;
            }
        }
        return null;
    }

    private Instant stringToInstant( final String instant )
    {
        if ( instant == null )
        {
            return null;
        }
        else
        {
            DateTimeFormatter f = DateTimeFormatter.ISO_INSTANT;
            try
            {
                return Instant.from( f.parse( instant ) );
            }
            catch ( Exception e )
            {
                return null;
            }
        }
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
