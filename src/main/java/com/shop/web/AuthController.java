package com.shop.web;

import com.shop.common.Result;
import com.shop.common.WebUtil;
import com.shop.entity.User;
import com.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 注册 / 登录 / 登出
 */
@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, HttpSession session) {
        if (WebUtil.getLoginUser(request) != null) {
            return "redirect:/";
        }
        return "front/login";
    }

    @GetMapping("/register")
    public String registerPage(HttpServletRequest request) {
        if (WebUtil.getLoginUser(request) != null) {
            return "redirect:/";
        }
        return "front/register";
    }

    @PostMapping("/api/auth/register")
    @ResponseBody
    public Result<Void> register(@RequestBody Map<String, String> body) {
        userService.register(body.get("username"), body.get("password"), body.get("nickname"));
        return Result.ok();
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        User user = userService.login(body.get("username"), body.get("password"));
        HttpSession session = request.getSession();
        session.setAttribute(WebUtil.SESSION_USER, user);
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("nickname", user.getNickname());
        data.put("role", user.getRole());
        String redirect = body.get("redirect");
        if (redirect != null && !redirect.isEmpty() && redirect.startsWith("/")) {
            data.put("redirect", redirect);
        } else {
            data.put("redirect", "admin".equals(user.getRole()) ? "/admin" : "/");
        }
        return Result.ok(data);
    }

    @PostMapping("/api/auth/logout")
    @ResponseBody
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.ok();
    }

    @GetMapping("/api/user/me")
    @ResponseBody
    public Result<User> me(HttpServletRequest request) {
        return Result.ok(WebUtil.getLoginUser(request));
    }
}
