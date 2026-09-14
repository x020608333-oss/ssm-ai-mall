package com.shop.dao;

import com.shop.entity.ChatMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMessageMapper {

    int insert(ChatMessage msg);

    List<ChatMessage> findRecent(@Param("sessionId") String sessionId, @Param("limit") int limit);
}
