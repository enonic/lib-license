package com.enonic.lib.license;

import com.fasterxml.jackson.annotation.JsonProperty;

final class LicenseDetailsJson
{
    @JsonProperty("name")
    private String name;

    @JsonProperty("organization")
    private String organization;

    @SuppressWarnings("unused")
    public LicenseDetailsJson()
    {
    }

    public LicenseDetailsJson( final LicenseDetails licenseDetails )
    {
        this.name = licenseDetails.getName();
        this.organization = licenseDetails.getOrganization();
    }

    public LicenseDetails toLicense()
    {
        return LicenseDetails.create().
            name( this.name ).
            organization( this.organization ).
            build();
    }
}
