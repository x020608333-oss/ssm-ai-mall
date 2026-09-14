package com.shop.web;

import com.shop.ai.ChatService;
import com.shop.common.Result;
import com.shop.entity.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
 * AI 智能客服：页面 + 对话API
 * 会话ID通过Cookie维持，游客也能咨询；登录与否不影响对话
 */
@Controller
public class AiChatController {

    private static final String COOKIE_SID = "mall_chat_sid";

    @Autowired
    private ChatService chatService;

    @GetMapping("/chat")
    public String page(HttpServletRequest request, HttpServletResponse response) {
        ensureSid(request, response);
        return "front/chat";
    }

    @PostMapping("/api/chat/send")
    @ResponseBody
    public Result<ChatMessage> send(@RequestBody Map<String, String> body,
                                    HttpServletRequest request, HttpServletResponse response) {
        String sid = ensureSid(request, response);
        String message = body.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.error(400, "消息不能为空");
        }
        if (message.length() > 500) {
            return Result.error(400, "消息太长了，请精简一下");
        }
        return Result.ok(chatService.reply(sid, message.trim()));
    }

    @GetMapping("/api/chat/history")
    @ResponseBody
    public Result<java.util.List<ChatMessage>> history(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String sid = null;
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (COOKIE_SID.equals(c.getName())) {
                    sid = c.getValue();
                    break;
                }
            }
        }
        if (sid == null || sid.isEmpty()) {
            return Result.ok(java.util.Collections.emptyList());
        }
        return Result.ok(chatService.history(sid));
    }

    /** 会话ID：无则种Cookie */
    private String ensureSid(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (COOKIE_SID.equals(c.getName()) && c.getValue() != null && !c.getValue().isEmpty()) {
                    return c.getValue();
                }
            }
        }
        String sid = UUID.randomUUID().toString().replace("-", "");
        Cookie cookie = new Cookie(COOKIE_SID, sid);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 3600);
        response.addCookie(cookie);
        return sid;
    }
}
