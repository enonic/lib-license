package com.enonic.lib.license;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;

final class LicenseDetailsJson
{
    public String issuedTo;

    public String issuedBy;

    public String issueTime;

    public String expiryTime;

    public Map<String, String> properties;

    @SuppressWarnings("unused")
    public LicenseDetailsJson()
    {
    }

    public LicenseDetailsJson( final LicenseDetails licenseDetails )
    {
        this.issuedTo = licenseDetails.getIssuedTo();
        this.issuedBy = licenseDetails.getIssuedBy();
        this.issueTime = instantToString( licenseDetails.getIssueTime() );
        this.expiryTime = instantToString( licenseDetails.getExpiryTime() );
        this.properties = licenseDetails.getProperties();
    }

    public LicenseDetails toLicense()
    {
        return LicenseDetails.create()
            .issuedTo( this.issuedTo )
            .issuedBy( this.issuedBy )
            .expiryTime( stringToInstant( this.expiryTime ) )
            .issueTime( stringToInstant( this.issueTime ) )
            .properties( this.properties )
            .build();
    }

    private String instantToString( final Instant instant )
    {
        return instant == null ? null : instant.toString();
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
}
