package com.enonic.lib.license;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

final class LicenseDetailsJson
{
    @JsonProperty("name")
    private String name;

    @JsonProperty("organization")
    private String organization;

    @JsonProperty("issueTime")
    private String issueTime;

    @JsonProperty("expiryTime")
    private String expiryTime;

    @JsonProperty("properties")
    private Map<String, String> properties;

    @SuppressWarnings("unused")
    public LicenseDetailsJson()
    {
    }

    public LicenseDetailsJson( final LicenseDetails licenseDetails )
    {
        this.name = licenseDetails.getName();
        this.organization = licenseDetails.getOrganization();
        this.issueTime = instantToString( licenseDetails.getIssueTime() );
        this.expiryTime = instantToString( licenseDetails.getExpiryTime() );
        this.properties = licenseDetails.getProperties();
    }

    public LicenseDetails toLicense()
    {
        return LicenseDetails.create().
            name( this.name ).
            organization( this.organization ).
            expiryTime( stringToInstant( this.expiryTime ) ).
            issueTime( stringToInstant( this.issueTime ) ).
            properties( this.properties ).
            build();
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
