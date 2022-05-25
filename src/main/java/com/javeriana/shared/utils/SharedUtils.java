package com.javeriana.shared.utils;

import com.javeriana.publish_subscribe.models.MonitorDTO;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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

    public static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<MonitorDTO> initMonitors() {
        MonitorDTO ph = MonitorDTO.builder()
                .pos(0)
                .topic("PH")
                .port(4982)
                .mainIp("192.168.5.112")
                .secondaryIp("192.168.5.114")
                .build();

        MonitorDTO oxigeno = MonitorDTO.builder()
                .pos(1)
                .topic("Oxigeno")
                .port(4983)
                .mainIp("192.168.5.112")
                .secondaryIp("192.168.5.114")
                .build();

        MonitorDTO temperatura = MonitorDTO.builder()
                .pos(2)
                .topic("Temperatura")
                .port(4984)
                .mainIp("192.168.5.114")
                .secondaryIp("192.168.5.117")
                .build();

        return List.of(ph, oxigeno, temperatura);
    }

    public static boolean serverListening(String host, int port) {
        try (Socket serverSocket = new Socket()) {
            // serverSocket.setReuseAddress(false);
            serverSocket.connect(new InetSocketAddress(InetAddress.getByName(host), port), 1);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
