package com.shop.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ChatMessage {
    private Long id;
    private String sessionId;
    /** user / assistant */
    private String role;
    private String content;
    /** 回复来源：llm大模型 / rule规则 / db商品检索 */
    private String source;
    private Date createdAt;
}
