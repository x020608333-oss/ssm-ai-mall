package com.shop.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.common.Result;
import com.shop.common.WebUtil;
import com.shop.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器：页面请求跳登录页，API请求返回401 JSON
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        User user = WebUtil.getLoginUser(request);
        if (user != null) {
            return true;
        }
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(MAPPER.writeValueAsString(Result.error(401, "请先登录")));
            return false;
        }
        String redirect = uri + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
        response.sendRedirect(request.getContextPath() + "/login?redirect=" +
                java.net.URLEncoder.encode(redirect, "UTF-8"));
        return false;
    }
}
