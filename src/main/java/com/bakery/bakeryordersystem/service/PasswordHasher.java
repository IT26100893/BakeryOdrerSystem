package com.bakery.bakeryordersystem.service;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HexFormat;

/** Information hiding: passwords are stored as "salt:SHA-256(salt+password)", never as plain text. */
public final class PasswordHasher {
    private static final SecureRandom RNG = new SecureRandom();
    private PasswordHasher() {}

    public static String hash(String raw) {
        byte[] salt = new byte[8]; RNG.nextBytes(salt);
        String s = HexFormat.of().formatHex(salt);
        return s + ":" + sha256(s + raw);
    }
    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null || !stored.contains(":")) return false;
        String[] p = stored.split(":", 2);
        return MessageDigest.isEqual(sha256(p[0] + raw).getBytes(StandardCharsets.UTF_8), p[1].getBytes(StandardCharsets.UTF_8));
    }
    private static String sha256(String s) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
