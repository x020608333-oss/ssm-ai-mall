package com.shop.common;

import com.shop.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 会话与分页参数小工具
 */
public final class WebUtil {

    public static final String SESSION_USER = "loginUser";

    private WebUtil() {
    }

    /** 当前登录用户（拦截器已保证登录，此处只做读取） */
    public static User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute(SESSION_USER);
    }

    public static int page(HttpServletRequest request) {
        return Math.max(parseInt(request.getParameter("page"), 1), 1);
    }

    public static int size(HttpServletRequest request) {
        int size = parseInt(request.getParameter("size"), 10);
        return Math.min(Math.max(size, 1), 100);
    }

    public static int parseInt(String s, int def) {
        if (s == null || s.isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
