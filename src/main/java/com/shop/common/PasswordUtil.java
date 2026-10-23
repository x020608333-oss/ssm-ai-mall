package com.shop.common;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;

/**
 * 密码散列：新密码使用 PBKDF2-HMAC-SHA256 + 随机盐，兼容历史 MD5 并自动升级。
 *
 * <p>存储格式：{@code pbkdf2$iterations$saltHex$hashHex}；旧数据为 32 位 MD5 hex。</p>
 */
public final class PasswordUtil {

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(String raw) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] derived = pbkdf2(raw.toCharArray(), salt, ITERATIONS);
        return "pbkdf2$" + ITERATIONS + "$" + hex(salt) + "$" + hex(derived);
    }

    public static boolean isModern(String stored) {
        return stored != null && stored.startsWith("pbkdf2$");
    }

    /** 校验密码；历史 MD5 继续可用，用户下次登录时会由业务层升级为 PBKDF2。 */
    public static boolean verify(String raw, String stored) {
        if (raw == null || stored == null) {
            return false;
        }
        if (isModern(stored)) {
            String[] parts = stored.split("\\$");
            if (parts.length != 4) {
                return false;
            }
            try {
                int iterations = Integer.parseInt(parts[1]);
                byte[] salt = unhex(parts[2]);
                byte[] expected = unhex(parts[3]);
                byte[] actual = pbkdf2(raw.toCharArray(), salt, iterations);
                return MessageDigest.isEqual(expected, actual);
            } catch (RuntimeException e) {
                return false;
            }
        }
        // 兼容旧 MD5 数据
        try {
            byte[] digest = MessageDigest.getInstance("MD5")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(hex(digest).getBytes(StandardCharsets.US_ASCII),
                    stored.toLowerCase().getBytes(StandardCharsets.US_ASCII));
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("当前 JDK 不支持 PBKDF2WithHmacSHA256", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static byte[] unhex(String value) {
        if (value == null || (value.length() & 1) != 0) {
            throw new IllegalArgumentException("invalid hex");
        }
        byte[] out = new byte[value.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int hi = Character.digit(value.charAt(i * 2), 16);
            int lo = Character.digit(value.charAt(i * 2 + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("invalid hex");
            }
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }
}
