package com.aofeng.testnfc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author liuyi
 * @create 2023/9/28
 **/
public class Encrypt {

    private static final String[] keyTypes = new String[]{
            "1111111111111111",
            "2222222222222222",
            "3333333333333333",
            "4444444444444444",
            "5555555555555555",
            "6666666666666666",
            "7777777777777777",
            "8888888888888888",
            "9999999999999999",
            "AAAAAAAAAAAAAAAA",
            "BBBBBBBBBBBBBBBB",
            "CCCCCCCCCCCCCCCC",
    };


    public static byte[] encrypt_ECB(byte[] key, byte[] text) throws Exception {
        int ret = text.length % 16;
        byte[] data;
        if (ret != 0) {
            data = new byte[text.length + 16 - ret];
            System.arraycopy(text, 0, data, 0, text.length);
            data[text.length] = (byte) 0x80;
        } else {
            data = new byte[text.length];
            System.arraycopy(text, 0, data, 0, text.length);
        }

        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt_ECB(byte[] key, byte[] encrypted) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encrypted);
    }


    public static byte[] getKey(byte keyType, byte[] uuid) {
        String uuids = HexDump.decBytesToHex(uuid) + keyTypes[keyType];
        byte[] hsa256 = HSA256(uuids);

        byte[] key = new byte[16];
        System.arraycopy(hsa256, 0, key, 0, 16);
        return key;
    }

    public static byte[] encrypt_CBC(byte[] key, byte[] initVector, byte[] text, int length) throws Exception {

        int ret = length % 16;
        byte[] data;
        if (ret != 0) {
            data = new byte[length + 16 - ret];
            System.arraycopy(text, 0, data, 0, length);
            data[length] = (byte) 0x80;
        } else {
            data = new byte[length];
            System.arraycopy(text, 0, data, 0, length);
        }

        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        IvParameterSpec iv = new IvParameterSpec(initVector);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        return cipher.doFinal(data);
    }

    public static String decrypt_CBC(byte[] key, byte[] initVector, byte[] encrypted) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        IvParameterSpec iv = new IvParameterSpec(initVector);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public static byte[] HSA256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] xor(byte[] data, byte[] key) {
        try {
            byte[] result = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                result[i] = (byte) (data[i] ^ key[i]);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getRandomBytes(int length) {
        try {
            Random random = new Random();
            byte[] result = new byte[length];
            random.nextBytes(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
