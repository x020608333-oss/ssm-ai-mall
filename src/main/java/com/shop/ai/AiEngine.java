package com.shop.ai;

import com.shop.entity.ChatMessage;

import java.util.List;

/**
 * AI 客服引擎统一接口：方便替换/对比不同实现（大模型 vs 规则引擎）
 */
public interface AiEngine {

    /**
     * 生成回复
     *
     * @param systemPrompt 系统提示词（包含商品库上下文，即简易RAG）
     * @param history      历史对话（最近N条）
     * @param userMsg      用户当前消息
     * @return 回复内容；返回 null 表示该引擎无法处理，交给下一个兜底引擎
     */
    String chat(String systemPrompt, List<ChatMessage> history, String userMsg);

    /** 该引擎当前是否可用（如大模型是否已配置Key） */
    boolean available();

    String name();
}
