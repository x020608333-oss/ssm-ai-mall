package com.shop.web;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 登录后的跳转目标只允许站内路径，防止开放重定向把用户带到攻击者站点。
 */
public class AuthControllerRedirectTest {

    @Test
    public void keepsSiteRelativePath() {
        assertEquals("/orders", AuthController.resolveRedirect("/orders", "user"));
        assertEquals("/cart?from=checkout", AuthController.resolveRedirect("/cart?from=checkout", "user"));
        assertEquals("/admin/users", AuthController.resolveRedirect("/admin/users", "admin"));
    }

    @Test
    public void fallsBackByRoleWhenAbsent() {
        assertEquals("/", AuthController.resolveRedirect(null, "user"));
        assertEquals("/", AuthController.resolveRedirect("", "user"));
        assertEquals("/admin", AuthController.resolveRedirect(null, "admin"));
        assertEquals("/admin", AuthController.resolveRedirect("", "admin"));
    }

    @Test
    public void rejectsExternalAndProtocolRelativeUrl() {
        assertEquals("/", AuthController.resolveRedirect("https://evil.com/phish", "user"));
        assertEquals("/", AuthController.resolveRedirect("http://evil.com", "user"));
        assertEquals("/", AuthController.resolveRedirect("//evil.com", "user"));
        assertEquals("/", AuthController.resolveRedirect("/\\evil.com", "user"));
        assertEquals("/", AuthController.resolveRedirect("evil.com", "user"));
        assertEquals("/admin", AuthController.resolveRedirect("//evil.com", "admin"));
    }

    @Test
    public void rejectsControlCharacterSmuggling() {
        assertEquals("/", AuthController.resolveRedirect("/\t/evil.com", "user"));
        assertEquals("/", AuthController.resolveRedirect("/\n//evil.com", "user"));
        assertEquals("/", AuthController.resolveRedirect("/\r/evil.com", "user"));
        assertEquals("/", AuthController.resolveRedirect("/orders\u0000", "user"));
        assertEquals("/", AuthController.resolveRedirect("/orders\u007f", "user"));
    }
}