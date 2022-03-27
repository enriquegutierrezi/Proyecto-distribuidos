package com.javeriana.shared.utils;

import com.javeriana.publish_subscribe.models.MonitorDTO;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

public class SharedUtils {

    // Definición del tipo de algoritmo a utilizar (AES, DES, RSA)
    private final static String ALG = "AES";
    // Definición del modo de cifrado a utilizar
    private final static String CI = "AES/CBC/PKCS5Padding";

    public static String getHash(String txt, String hashType) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance(hashType);
            byte[] array = md.digest(txt.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static String encrypt(String key, String iv, String cleartext) throws Exception {
        Cipher cipher = Cipher.getInstance(CI);
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), ALG);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(cleartext.getBytes());
        return new String(encodeBase64(encrypted));
    }

    public static String decrypt(String key, String iv, String encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance(CI);
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), ALG);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        byte[] enc = decodeBase64(encrypted);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
        byte[] decrypted = cipher.doFinal(enc);
        return new String(decrypted);
    }

    public static List<MonitorDTO> initMonitors() {
        MonitorDTO ph = MonitorDTO.builder()
                .pos(0)
                .topic("PH")
                .port(4982)
                .mainIp(new byte[]{127, 0, 0, 1})
                .secondaryIp(new byte[]{127, 0, 0, 1})
                .build();

        MonitorDTO oxigeno = MonitorDTO.builder()
                .pos(1)
                .topic("Oxigeno")
                .port(4983)
                .mainIp(new byte[]{127, 0, 0, 1})
                .secondaryIp(new byte[]{127, 0, 0, 1})
                .build();

        MonitorDTO temperatura = MonitorDTO.builder()
                .pos(2)
                .topic("Temperatura")
                .port(4984)
                .mainIp(new byte[]{127, 0, 0, 1})
                .secondaryIp(new byte[]{127, 0, 0, 1})
                .build();

        return List.of(ph, oxigeno, temperatura);
    }
}
