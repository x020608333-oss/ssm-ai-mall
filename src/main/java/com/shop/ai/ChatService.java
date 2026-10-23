package com.shop.ai;

import com.shop.dao.ChatMessageMapper;
import com.shop.dao.ProductMapper;
import com.shop.entity.ChatMessage;
import com.shop.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 客服编排器，回复优先级：
 * 1. 商品库检索（用户消息命中商品名 → 直接返回真实商品数据，相当于简易RAG/Tool Calling）
 * 2. 大模型（app.ai.enabled=true 且配置了Key时，走 OpenAI 兼容接口，上下文注入热门商品）
 * 3. 规则引擎（关键词意图识别，永远可用）
 * 4. 通用兜底话术
 * 全部对话入库 chat_message，便于复盘与效果分析
 */
@Slf4j
@Service
public class ChatService {

    private static final Pattern PRICE_ASK = Pattern.compile(".*?(\\D{0,12})?(多少钱|价格|报价|售价).*");
    private static final int HISTORY_LIMIT = 6;

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    @Autowired
    private OpenAiCompatClient llmClient;
    @Autowired
    private RuleChatClient ruleClient;

    public ChatMessage reply(String sessionId, String userMsg) {
        // 用户消息入库
        save(sessionId, "user", userMsg, "user");

        String answer;
        String source;
        // 1. 商品库检索优先（数据实时、最可信）
        String dbAnswer = searchProductAnswer(userMsg);
        if (dbAnswer != null) {
            answer = dbAnswer;
            source = "db";
        } else {
            // 2. 大模型（带历史上下文与商品库提示词）
            List<ChatMessage> history = chatMessageMapper.findRecent(sessionId, HISTORY_LIMIT);
            Collections.reverse(history);
            // findRecent 已经包含刚入库的本轮 user 消息，传给 LLM 前去掉，避免同一句话出现两次
            if (!history.isEmpty()) {
                ChatMessage last = history.get(history.size() - 1);
                if ("user".equals(last.getRole()) && userMsg.equals(last.getContent())) {
                    history.remove(history.size() - 1);
                }
            }
            String llm = llmClient.chat(systemPrompt(), history, userMsg);
            if (llm != null) {
                answer = llm;
                source = "llm";
            } else {
                // 3. 规则引擎
                String rule = ruleClient.chat(null, history, userMsg);
                if (rule != null) {
                    answer = rule;
                    source = "rule";
                } else {
                    // 4. 通用兜底
                    answer = "抱歉，我还在学习中 🤖\n您可以问我：\"有什么手机推荐\"、\"什么时候发货\"、\"怎么退款\"等问题，\n也可以拨打人工客服电话 400-800-1234。";
                    source = "fallback";
                }
            }
        }

        ChatMessage reply = save(sessionId, "assistant", answer, source);
        log.debug("AI客服回复 session={} source={}", sessionId, source);
        return reply;
    }

    public List<ChatMessage> history(String sessionId) {
        List<ChatMessage> list = chatMessageMapper.findRecent(sessionId, 30);
        Collections.reverse(list);
        return list;
    }

    /**
     * 简易RAG：把当前在售热门商品注入提示词，让大模型的回答贴近真实商品
     */
    private String systemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「智选商城」的AI客服，亲切专业，回答简洁（不超过200字）。")
          .append("只回答与购物相关的问题，无关话题礼貌拒绝。")
          .append("以下是商城在售热门商品（名称|价格|库存）：\n");
        List<Product> products = productMapper.searchByName("", 15);
        for (Product p : products) {
            sb.append(p.getName()).append("|").append(p.getPrice()).append("元|库存").append(p.getStock()).append("\n");
        }
        sb.append("物流：15点前当日发货，2~4天送达。退换：7天无理由。支付：支持模拟支付。");
        return sb.toString();
    }

    /**
     * 用户消息命中商品名（取消息中较长的词与商品名匹配）→ 返回真实商品信息
     */
    private String searchProductAnswer(String userMsg) {
        if (userMsg == null || userMsg.length() < 2) {
            return null;
        }
        // 询问价格类问题：尝试把消息中的非疑问词与商品名匹配
        List<Product> matched = null;
        // 先整句模糊搜
        String kw = userMsg.replaceAll("[吗呢吧？?！!。，,\\s]", "");
        if (kw.length() >= 2) {
            matched = productMapper.searchByName(kw, 3);
        }
        if (matched == null || matched.isEmpty()) {
            // 退而求其次：取消息中最长的2~6字连续中文片段逐个尝试
            Matcher m = Pattern.compile("[\\u4e00-\\u9fa5A-Za-z0-9]{2,8}").matcher(userMsg);
            int tries = 0;
            while (m.find() && tries < 6) {
                String piece = m.group();
                if (piece.length() >= 2) {
                    matched = productMapper.searchByName(piece, 3);
                    if (!matched.isEmpty()) {
                        break;
                    }
                    tries++;
                }
            }
        }
        if (matched == null || matched.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder("为您找到相关商品：\n");
        int i = 1;
        for (Product p : matched) {
            sb.append(i++).append(". ").append(p.getName())
              .append("，售价 ").append(p.getPrice()).append(" 元")
              .append("（库存").append(p.getStock()).append("件，已售").append(p.getSales()).append("）\n");
        }
        sb.append("\n可以点击首页搜索框查看商品详情，需要我帮您推荐同类商品吗？");
        return sb.toString();
    }

    private ChatMessage save(String sessionId, String role, String content, String source) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setSource(source);
        chatMessageMapper.insert(msg);
        return msg;
    }
}
