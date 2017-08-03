package com.lifeform.main.data;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * Created by Bryan on 5/30/2017.
 */
public class Utils {
    /**
     *
     * @param array byte array to convert to hex string
     * @return hex array string corresponding to input byte array
     */
    public static String toHexArray(byte[] array)
    {
        String s = "";

        for(byte b:array)
        {
            s = s + (String.format("%02X", b));
        }
        return s;
    }





    /**
     *
     * @param s hex array as a string
     * @return byte array corresponding to input hex
     */
    public static byte[] toByteArray(String s) {
        int len = s.length();
        if(len % 2 > 0) {
            s = "0" + s;
            len++;
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static double hexToDouble(String s)
    {
        if(s.isEmpty()) return 0.0;
        return Double.longBitsToDouble(new BigInteger(s, 16).longValue());
    }

    public static String doubleToHex(Double d)
    {
        return Long.toHexString(Double.doubleToRawLongBits(d));
    }
    public static String longToHex(Long l)
    {
        return Long.toHexString(l);
    }
    public static long hexToLong(String s)
    {
        if(s.isEmpty()) return 0;
        return Long.parseLong(s,16);
    }


    public static byte[] concat(byte[]... arrays)
    {
        int i = 0;
        for(byte[] a: arrays)
        {

            i = i + a.length;
        }
        byte[] c = new byte[i];
        i = 0;
        for(byte[] a: arrays)
        {
            for(int p = 0; p < a.length; p++) {
                c[i] = a[p];
                i++;
            }
        }

        return c;
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
        buffer.putLong(x);
        return buffer.array();
    }
}
