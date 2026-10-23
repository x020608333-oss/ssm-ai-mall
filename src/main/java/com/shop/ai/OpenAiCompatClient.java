package com.shop.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容接口的大模型客户端（httpclient 实现，无SDK依赖）
 * 支持 DeepSeek / Kimi / 智谱GLM / 通义千问 等所有 /chat/completions 兼容服务
 * 未配置 api-key 或调用失败时返回 null，由规则引擎兜底
 */
@Slf4j
@Component
public class OpenAiCompatClient implements AiEngine {

    @Value("${app.ai.enabled}")
    private boolean enabled;

    @Value("${app.ai.base-url}")
    private String baseUrl;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model}")
    private String model;

    private final ObjectMapper mapper = new ObjectMapper();

    /** 单例 HttpClient：连接池复用 + 超时，避免每次请求新建客户端、无限等待 */
    private final CloseableHttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .setSocketTimeout(30000)
                    .build())
            .build();

    @Override
    public String chat(String systemPrompt, List<ChatMessage> history, String userMsg) {
        if (!available()) {
            return null;
        }
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(msg("system", systemPrompt));
            if (history != null) {
                for (ChatMessage h : history) {
                    messages.add(msg(h.getRole(), h.getContent()));
                }
            }
            messages.add(msg("user", userMsg));

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("stream", false);
            body.put("temperature", 0.6);
            body.put("max_tokens", 500);

            HttpPost post = new HttpPost(baseUrl + "/chat/completions");
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setEntity(new StringEntity(
                    mapper.writeValueAsString(body), StandardCharsets.UTF_8));

            try (CloseableHttpResponse resp = httpClient.execute(post)) {
                String text = EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8);
                JsonNode root = mapper.readTree(text);
                JsonNode content = root.path("choices").path(0).path("message").path("content");
                if (content.isMissingNode() || content.asText().isEmpty()) {
                    log.warn("LLM返回异常: {}", text);
                    return null;
                }
                return content.asText().trim();
            }
        } catch (IOException e) {
            log.warn("LLM调用失败，将使用规则引擎兜底: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean available() {
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public String name() {
        return "llm";
    }

    private Map<String, String> msg(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }
}
