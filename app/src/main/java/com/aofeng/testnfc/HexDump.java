package com.aofeng.testnfc;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author liuyi
 * @create 2023/5/8
 **/
public class HexDump {
    private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public HexDump() {
    }

    public static String decBytesToHex(byte[] buffs) {
        if (buffs != null && buffs.length != 0) {
            StringBuffer buffer = new StringBuffer();
            int len = buffs.length;

            for(int i = 0; i < len; ++i) {
                buffer.append(String.format("%02X", buffs[i]));
            }

            return buffer.toString();
        } else {
            return null;
        }
    }

    public static String toHexString(byte[] array) {
        return array == null ? "null" : toHexString(array, 0, array.length);
    }

    public static String toHexString(byte[] array, int offset, int length)
    {
        char[] buf = new char[length * 2];

        int bufIndex = 0;
        for (int i = offset ; i < offset + length; i++)
        {
            byte b = array[i];
            buf[bufIndex++] = HEX_DIGITS[(b >>> 4) & 0x0F];
            buf[bufIndex++] = HEX_DIGITS[b & 0x0F];
        }

        return new String(buf);
    }

    public static String toHexString(int i) {
        return toHexString(toByteArray(i));
    }

    public static String toHexStringX(int i) {
        return "0x" + toHexString(toByteArray(i));
    }

    public static byte[] toByteArray(byte b) {
        byte[] array = new byte[]{b};
        return array;
    }

    public static byte[] toByteArray(int i) {
        byte[] array = new byte[]{(byte)(i >> 24 & 255), (byte)(i >> 16 & 255), (byte)(i >> 8 & 255), (byte)(i & 255)};
        return array;
    }

    private static int toByte(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        } else if (c >= 'A' && c <= 'F') {
            return c - 65 + 10;
        } else if (c >= 'a' && c <= 'f') {
            return c - 97 + 10;
        } else {
            throw new RuntimeException("Invalid hex char '" + c + "'");
        }
    }

    public static byte[] hexStringToByteArray(String hexString) {
        int length = hexString.length();
        byte[] buffer = new byte[length / 2];

        for(int i = 0; i < length; i += 2) {
            buffer[i / 2] = (byte)(toByte(hexString.charAt(i)) << 4 | toByte(hexString.charAt(i + 1)));
        }

        return buffer;
    }

    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);

        for(int i = 0; i < bytes.length; ++i) {
            temp.append((byte)((bytes[i] & 240) >> 4));
            temp.append((byte)(bytes[i] & 15));
        }

        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
    }

    public static String byte2bcd(byte[] bcds) {
        char[] ascii = "0123456789abcdef".toCharArray();
        byte[] temp = new byte[bcds.length * 2];

        for(int i = 0; i < bcds.length; ++i) {
            temp[i * 2] = (byte)(bcds[i] >> 4 & 15);
            temp[i * 2 + 1] = (byte)(bcds[i] & 15);
        }

        StringBuffer res = new StringBuffer();

        for(int i = 0; i < temp.length; ++i) {
            res.append(ascii[temp[i]]);
        }

        return res.toString().toUpperCase();
    }

    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }

        byte[] abt = new byte[len];
        if (len >= 2) {
            len /= 2;
        }

        byte[] bbt = new byte[len];
        abt = asc.getBytes();

        for(int p = 0; p < asc.length() / 2; ++p) {
            int j;
            if (abt[2 * p] >= 48 && abt[2 * p] <= 57) {
                j = abt[2 * p] - 48;
            } else if (abt[2 * p] >= 97 && abt[2 * p] <= 122) {
                j = abt[2 * p] - 97 + 10;
            } else {
                j = abt[2 * p] - 65 + 10;
            }

            int k;
            if (abt[2 * p + 1] >= 48 && abt[2 * p + 1] <= 57) {
                k = abt[2 * p + 1] - 48;
            } else if (abt[2 * p + 1] >= 97 && abt[2 * p + 1] <= 122) {
                k = abt[2 * p + 1] - 97 + 10;
            } else {
                k = abt[2 * p + 1] - 65 + 10;
            }

            int a = (j << 4) + k;
            byte b = (byte)a;
            bbt[p] = b;
        }

        return bbt;
    }

    public static int byteToInt(byte[] b) {
        if (b != null && b.length <= 4) {
            int value = 0;

            for(int i = 0; i < b.length; ++i) {
                int shift = (b.length - 1 - i) * 8;
                value += (b[i + 0] & 255) << shift;
            }

            return value;
        } else {
            return 0;
        }
    }

    public static String post(String posturl, JSONObject jsonObject) {
        try {
            byte[] postDataBytes = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
            URL url = new URL(posturl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            urlConnection.getOutputStream().write(postDataBytes);
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                return readStream(in);
            } finally {
                urlConnection.disconnect();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private static String readStream(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = "";
        StringBuilder result = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

}