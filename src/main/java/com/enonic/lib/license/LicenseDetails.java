package com.enonic.lib.license;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

public final class LicenseDetails
{
    private final String name;

    private final String organization;

    private final Instant issueTime;

    private final Instant expiryTime;

    private final ImmutableMap<String, String> data;

    private LicenseDetails( final Builder builder )
    {
        this.name = builder.name == null ? "" : builder.name;
        this.organization = builder.organization == null ? "" : builder.organization;
        this.issueTime = builder.issueTime;
        this.expiryTime = builder.expiryTime;
        this.data = ImmutableMap.copyOf( builder.data );
    }

    public String getName()
    {
        return name;
    }

    public String getOrganization()
    {
        return organization;
    }

    public Instant getIssueTime()
    {
        return issueTime;
    }

    public Instant getExpiryTime()
    {
        return expiryTime;
    }

    public ImmutableMap<String, String> getProperties()
    {
        return data;
    }

    public String getProperty( final String key )
    {
        return data.get( key );
    }

    public String getProperty( final String key, final String defaultValue )
    {
        return data.getOrDefault( key, defaultValue );
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
        final LicenseDetails that = (LicenseDetails) o;
        return Objects.equals( name, that.name ) && Objects.equals( organization, that.organization ) &&
            Objects.equals( issueTime, that.issueTime ) && Objects.equals( expiryTime, that.expiryTime ) &&
            Objects.equals( data, that.data );
    }

    @Override
    public int hashCode()
    {

        return Objects.hash( name, organization, issueTime, expiryTime, data );
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this )
            .add( "name", name )
            .add( "organization", organization )
            .add( "issueTime", issueTime )
            .add( "expiryTime", expiryTime )
            .add( "data", data )
            .toString();
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

        private Instant issueTime;

        private Instant expiryTime;

        private Map<String, String> data;

        private Builder()
        {
            this.data = new HashMap<>();
        }

        private Builder( final LicenseDetails license )
        {
            this.name = license.name;
            this.organization = license.organization;
            this.issueTime = license.issueTime;
            this.expiryTime = license.expiryTime;
            this.data = new HashMap<>();
            this.data.putAll( license.data );
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

        public Builder issueTime( final Instant issueTime )
        {
            this.issueTime = issueTime;
            return this;
        }

        public Builder expiryTime( final Instant expiryTime )
        {
            this.expiryTime = expiryTime;
            return this;
        }

        public Builder property( final String key, final String value )
        {
            this.data.put( key, value );
            return this;
        }

        public Builder properties( final Map<String, String> properties )
        {
            if ( properties != null )
            {
                this.data.putAll( properties );
            }
            return this;
        }

        public LicenseDetails build()
        {
            return new LicenseDetails( this );
        }
    }
}
