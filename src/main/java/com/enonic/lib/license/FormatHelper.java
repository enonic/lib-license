package com.enonic.lib.license;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;

final class FormatHelper
{
    private FormatHelper()
    {
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex( final byte[] bytes )
    {
        return bytesToHex( bytes, 0, bytes.length );
    }

    public static String bytesToHex( final byte[] bytes, int len )
    {
        int from = 0;
        if ( len < 0 )
        {
            from = bytes.length + len;
            len = -len;
        }
        return bytesToHex( bytes, from, len );
    }

    private static String bytesToHex( final byte[] bytes, final int from, final int len )
    {
        char[] hexChars = new char[len * 2];
        int l = from + len;
        for ( int j = from; j < l; j++ )
        {
            int v = bytes[j] & 0xFF;
            hexChars[( j - from ) * 2] = hexArray[v >>> 4];
            hexChars[( j - from ) * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String( hexChars );
    }

    public static String asPEM( final String value, final String label )
    {
        final String header = header( label );
        final String footer = footer( label );
        final List<String> lines = new ArrayList<>( Splitter.fixedLength( 64 ).splitToList( value ) );
        lines.add( 0, header );
        lines.add( footer );
        return lines.stream().collect( Collectors.joining( "\r\n" ) );
    }

    public static boolean isPEMHeader( final String line, final String label )
    {
        return header( label ).equalsIgnoreCase( line );
    }

    public static boolean isPEMFooter( final String line, final String label )
    {
        return footer( label ).equalsIgnoreCase( line );
    }

    private static String header( final String label )
    {
        return "-----BEGIN " + label + "-----";
    }

    private static String footer( final String label )
    {
        return "-----END " + label + "-----";
    }
}