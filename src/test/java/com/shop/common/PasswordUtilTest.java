package com.shop.common;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordUtilTest {

    @Test
    public void pbkdf2HashVerifiesAndRejectsWrongPassword() {
        String stored = PasswordUtil.hash("123456");
        assertTrue(stored.startsWith("pbkdf2$"));
        assertTrue(PasswordUtil.verify("123456", stored));
        assertFalse(PasswordUtil.verify("654321", stored));
        assertFalse(PasswordUtil.verify("123456", "not-a-hash"));
    }

    @Test
    public void legacyMd5StillVerifies() throws Exception {
        byte[] digest = MessageDigest.getInstance("MD5")
                .digest("123456".getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        assertTrue(PasswordUtil.verify("123456", sb.toString()));
        assertFalse(PasswordUtil.verify("wrong", sb.toString()));
    }
}
