package com.testing_service.domain.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


/**
 * Key Util is a class providing service to hash meaningful fields into a code
 */
public class KeyUtil {
    public static String canonicalForDedup(double amount, int userId, String phone, String ip) {
        String amt = String.format(java.util.Locale.ROOT, "%.2f", amount);
        return "amount="+amt+"|userID="+userId+"|phone="+(phone==null?"":phone)+"|IP="+(ip==null?"":ip);
    }

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
