package com.vbank.vidyovideoview.helper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class HAMCA_Encription {

    private byte[] deriveKey(String p, byte[] s, int i, int l) throws Exception {
        PBEKeySpec ks = new PBEKeySpec(p.toCharArray(), s, i, l);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return skf.generateSecret(ks).getEncoded();
    }

    public String encrypt(String s, String p) throws Exception {
        SecureRandom r = SecureRandom.getInstance("SHA1PRNG");
        byte[] esalt = new byte[20];
        r.nextBytes(esalt);
        byte[] secret = deriveKey(p, esalt, 100000, 128);
        // Perform Encryption
        SecretKeySpec eks = new SecretKeySpec(secret, "AES");
        Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, eks, new IvParameterSpec(new byte[16]));
        byte[] es = c.doFinal(s.getBytes(StandardCharsets.UTF_8));
        // Generate 160 bit Salt for HMAC Key
        byte[] hsalt = new byte[20];
        r.nextBytes(hsalt);
        // Generate 160 bit HMAC Key
        byte[] dhk = deriveKey(p, hsalt, 100000, 160);
        // Perform HMAC using SHA-256
        SecretKeySpec hks = new SecretKeySpec(dhk, "HmacSHA256");
        Mac m = Mac.getInstance("HmacSHA256");
        m.init(hks);
        byte[] hmac = m.doFinal(es);
        // Construct Output as "ESALT + HSALT + CIPHERTEXT + HMAC"
        byte[] os = new byte[40 + es.length + 32];
        System.arraycopy(esalt, 0, os, 0, 20);
        System.arraycopy(hsalt, 0, os, 20, 20);
        System.arraycopy(es, 0, os, 40, es.length);
        System.arraycopy(hmac, 0, os, 40 + es.length, 32);

        byte[] bytes = null;
        // Return a Base64 Encoded String
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            bytes = Base64.getEncoder().encode(os);
        } else {
            bytes = android.util.Base64.encode(os, android.util.Base64.DEFAULT);
        }

        assert bytes != null;
        return new String(bytes);
    }


    public String decrypt(String eos, String p) throws Exception {
        // Recover our Byte Array by Base64 Decoding
        byte[] os = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            os = (Base64.getDecoder().decode(eos));
        }else {
            os = android.util.Base64.decode(eos, android.util.Base64.DEFAULT);
        }

        // Check Minimum Length (ESALT (20) + HSALT (20) + HMAC (32))
        if (os.length > 72) {
            // Recover Elements from String
            byte[] esalt = Arrays.copyOfRange(os, 0, 20);
            byte[] hsalt = Arrays.copyOfRange(os, 20, 40);
            byte[] es = Arrays.copyOfRange(os, 40, os.length - 32);
            byte[] hmac = Arrays.copyOfRange(os, os.length - 32, os.length);

            // Regenerate HMAC key using Recovered Salt (hsalt)
            byte[] dhk = deriveKey(p, hsalt, 100000, 160);

            // Perform HMAC using SHA-256
            SecretKeySpec hks = new SecretKeySpec(dhk, "HmacSHA256");
            Mac m = Mac.getInstance("HmacSHA256");
            m.init(hks);
            byte[] chmac = m.doFinal(es);

            // Compare Computed HMAC vs Recovered HMAC
            if (MessageDigest.isEqual(hmac, chmac)) {
                // HMAC Verification Passed
                // Regenerate Encryption Key using Recovered Salt (esalt)
                byte[] dek = deriveKey(p, esalt, 100000, 128);

                // Perform Decryption
                SecretKeySpec eks = new SecretKeySpec(dek, "AES");
                Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
                c.init(Cipher.DECRYPT_MODE, eks, new IvParameterSpec(new byte[16]));
                byte[] s = c.doFinal(es);

                // Return our Decrypted String
                return new String(s, StandardCharsets.UTF_8);
            }
        }
        throw new Exception();
    }
}
