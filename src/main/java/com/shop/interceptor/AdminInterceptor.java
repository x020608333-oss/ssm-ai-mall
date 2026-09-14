package com.shop.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.common.Result;
import com.shop.common.WebUtil;
import com.shop.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理端权限拦截器：仅 admin 角色可访问 /admin/** 与 /api/admin/**
 */
public class AdminInterceptor implements HandlerInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        User user = WebUtil.getLoginUser(request);
        if (user != null && "admin".equals(user.getRole())) {
            return true;
        }
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/")) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(MAPPER.writeValueAsString(Result.error(403, "无权限操作")));
            return false;
        }
        response.sendRedirect(request.getContextPath() + "/login?redirect=" +
                java.net.URLEncoder.encode("/admin", "UTF-8"));
        return false;
    }
}
