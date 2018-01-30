package com.enonic.lib.license;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;

public final class KeyPair
{
    private static final String KEY_PAIR_HEADER = "LICENSE-KEY-PAIR";

    private final PrivateKey privateKey;

    private final PublicKey publicKey;

    public KeyPair( final java.security.KeyPair rsaKeyPair )
    {
        this.privateKey = new PrivateKey( rsaKeyPair.getPrivate() );
        this.publicKey = new PublicKey( rsaKeyPair.getPublic() );
    }

    private KeyPair( final PublicKey publicKey, final PrivateKey privateKey )
    {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public PrivateKey getPrivateKey()
    {
        return privateKey;
    }

    public PublicKey getPublicKey()
    {
        return publicKey;
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
        final KeyPair keyPair = (KeyPair) o;
        return Objects.equals( privateKey, keyPair.privateKey ) && Objects.equals( publicKey, keyPair.publicKey );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( privateKey, publicKey );
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper( this ).
            add( "private", privateKey ).
            add( "public", publicKey ).
            toString();
    }

    public String serialize()
    {
        final String priv = privateKey.serialize();
        final String pub = publicKey.serialize();
        final String pair = priv + "." + pub;
        return FormatHelper.asPEM( pair, KEY_PAIR_HEADER );
    }

    public static KeyPair from( String value )
    {
        if ( value == null )
        {
            return null;
        }
        final String allLines[] = value.split( "\\r?\\n" );
        final List<String> lines = Arrays.stream( allLines ).
            map( String::trim ).
            filter( s -> !s.isEmpty() ).
            collect( Collectors.toList() );
        if ( lines.size() < 3 )
        {
            return null;
        }

        final String header = lines.get( 0 );
        final String footer = lines.get( lines.size() - 1 );
        lines.remove( 0 );
        lines.remove( lines.size() - 1 );
        if ( !FormatHelper.isPEMHeader( header, KEY_PAIR_HEADER ) || !FormatHelper.isPEMFooter( footer, KEY_PAIR_HEADER ) )
        {
            return null;
        }

        final String keyPairStr = lines.stream().collect( Collectors.joining( "" ) );
        final int p = keyPairStr.indexOf( '.' );
        if ( p == -1 )
        {
            return null;
        }
        final String priv = keyPairStr.substring( 0, p ).trim();
        final String pub = keyPairStr.substring( p + 1 ).trim();

        final PrivateKey privateKey = PrivateKey.from( priv );
        final PublicKey publicKey = PublicKey.from( pub );

        if ( privateKey == null || publicKey == null )
        {
            return null;
        }

        return new KeyPair( publicKey, privateKey );
    }
}
