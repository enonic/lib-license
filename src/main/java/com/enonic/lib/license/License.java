package com.enonic.lib.license;

import com.google.common.base.MoreObjects;

public final class License
{
    private final String name;

    public License( final String name )
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).add( "name", name ).toString();
    }
}
