package com.lifeform.main.data;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;

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
        StringBuilder sb = new StringBuilder();
        for(byte b:array)
        {
           sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static String byteToHex(byte b) {
        return String.format("%02x", b);
    }

    public static byte[] mapToByteArray(Map obj) throws IOException {
        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
        return bytes;
    }

    public static Map toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        Map obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = (Map) ois.readObject();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }


    public static String toBase64(byte[] array)
    {
        try {
            return new String(Base64.getEncoder().encode(array), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static byte[] fromBase64(String b64)
    {
        return Base64.getDecoder().decode(b64);
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

    public static byte hexToByte(String s) {
        if (s.length() > 2) return 0;
        return toByteArray(s)[0];
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
