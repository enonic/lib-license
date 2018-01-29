package com.enonic.lib.license;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

final class LicenseDetailsJSONConverter
{
    private final static ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion( JsonInclude.Include.NON_NULL );

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

    public static LicenseDetails parse( final String licenseDetails )
    {
        try
        {
            final LicenseDetailsJson licenseDetailsJson = MAPPER.readValue( licenseDetails, LicenseDetailsJson.class );
            return licenseDetailsJson.toLicense();

        }
        catch ( IOException e )
        {
            return null;
        }
    }
}
