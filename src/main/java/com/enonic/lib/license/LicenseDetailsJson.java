package com.enonic.lib.license;

import com.fasterxml.jackson.annotation.JsonProperty;

final class LicenseDetailsJson
{
    @JsonProperty("name")
    private String nodeId;

    @JsonProperty("organization")
    private String organization;

    public LicenseDetailsJson( final LicenseDetails licenseDetails )
    {
        this.nodeId = licenseDetails.getName();
        this.organization = licenseDetails.getOrganization();
    }
}
