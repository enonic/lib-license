package com.enonic.lib.license;

final class Hex
{
    private Hex()
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
}