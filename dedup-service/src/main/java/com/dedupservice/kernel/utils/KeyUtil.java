package com.dedupservice.kernel.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class KeyUtil {
    private KeyUtil(){}


    public static String sha256Hex(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length*2);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
