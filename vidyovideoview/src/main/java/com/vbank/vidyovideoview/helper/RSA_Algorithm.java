package com.vbank.vidyovideoview.helper;

import android.os.Build;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSA_Algorithm {

    private final static String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu44BSBzSIxBgtLS/iJmSnBHjuhz5LhDh7DPHRVovB2NzaKW8Vqi3RsB8hS1kkKuiPzvqqmdCRA45qS6B89K91cJdeR6Bc33vTB6db8aCPLZuGKC2ULrhf9FzdVCHPscC/rNyOxuhwuAdwGpLrZPFF02Zw4cbTYbYGDyQpAkaFo+y6DJrstLX1ntJ4iwnRuCaNwXmn9E0QeA6YsVQY0VuPjAGwNHlXQaLgi5rw7zCRe4nac6SHn6BobhrwHT5ZqbC5CCLOYowNDzu4uVQyfO65tGCgBYQpN6ITez1sX4KY2+bE37laQioV/Cy5DDiDDVUIxJdRjF30i1z7ERdOTDGaQIDAQAB";
    public String encodeRawData(String rawData) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
            byte[] encryptedData = cipher.doFinal(rawData.getBytes());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(encryptedData);
            } else {
                return android.util.Base64.encodeToString(encryptedData, android.util.Base64.NO_WRAP);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Key getPublicKey() {
        try {
            byte[] decoded;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                decoded = Base64.getDecoder().decode(RSA_Algorithm.publicKey.getBytes("UTF-8"));
            } else {
                decoded = android.util.Base64.decode(RSA_Algorithm.publicKey.getBytes("UTF-8"), android.util.Base64.DEFAULT);
            }
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    String decodeData(String data, String privateKey) {
        try {
            byte[] rawData;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                rawData = Base64.getDecoder().decode(data);
            }else {
                rawData = android.util.Base64.decode(data,android.util.Base64.NO_WRAP);
            }
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKey));
            byte[] decryptedData = cipher.doFinal(rawData);
            return new String(decryptedData, "UTF-8");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Key getPrivateKey(String publicKey) {
        try {
            byte[] decoded;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                decoded = Base64.getDecoder().decode(publicKey.getBytes("UTF-8"));
            } else {
                decoded = android.util.Base64.decode(publicKey.getBytes("UTF-8"), android.util.Base64.NO_WRAP);
            }
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
