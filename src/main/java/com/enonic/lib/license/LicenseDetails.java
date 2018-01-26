package com.enonic.lib.license;

import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class LicenseDetails
{
    private final String name;

    private final String organization;

    private LicenseDetails( final Builder builder )
    {
        this.name = builder.name == null ? "" : builder.name;
        this.organization = builder.organization == null ? "" : builder.organization;
    }

    public String getName()
    {
        return name;
    }

    public String getOrganization()
    {
        return organization;
    }

    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        final LicenseDetails license = (LicenseDetails) o;
        return Objects.equals( name, license.name ) && Objects.equals( organization, license.organization );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( name, organization );
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "name", name ).add( "organization", organization ).toString();
    }

    public static Builder create()
    {
        return new LicenseDetails.Builder();
    }

    public static Builder create( final LicenseDetails license )
    {
        return new LicenseDetails.Builder( license );
    }

    public static class Builder
    {
        private String name;

        private String organization;

        private Builder()
        {
        }

        private Builder( final LicenseDetails license )
        {
            this.name = license.name;
            this.organization = license.organization;
        }

        public Builder name( final String name )
        {
            this.name = name;
            return this;
        }

        public Builder organization( final String organization )
        {
            this.organization = organization;
            return this;
        }

        public LicenseDetails build()
        {
            return new LicenseDetails( this );
        }
    }
}
