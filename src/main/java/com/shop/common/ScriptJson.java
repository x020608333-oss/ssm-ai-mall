package com.shop.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 生成可直接嵌入 JSP {@code <script>} 块的 JSON。
 */
public final class ScriptJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ScriptJson() {
    }

    /** 额外转义 < > &，防止字符串值里出现 </script> 提前截断脚本块 */
    public static String of(Object value) {
        try {
            return MAPPER.writeValueAsString(value)
                    .replace("<", "\\u003c")
                    .replace(">", "\\u003e")
                    .replace("&", "\\u0026");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化脚本数据失败", e);
        }
    }
}