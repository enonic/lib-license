package com.enonic.lib.license;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

final class LicenseDetailsSerializer
{
    private final static ObjectMapper MAPPER = new ObjectMapper();

    public static String serialize( final LicenseDetails licenseDetails )
    {
        try
        {
            return MAPPER.writeValueAsString( new LicenseDetailsJson( licenseDetails ) );
        }
        catch ( JsonProcessingException e )
        {
            throw new IllegalArgumentException( "Cannot serialize license", e );
        }
    }
}
