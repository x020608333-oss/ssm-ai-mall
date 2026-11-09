package com.shop.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScriptJsonTest {

    @Test
    public void neutralizesScriptBreakout() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("detail", "</script><script>alert(1)</script>");
        String json = ScriptJson.of(data);
        assertFalse(json.contains("<"));
        assertFalse(json.contains(">"));
        assertFalse(json.contains("&"));
        assertTrue(json.contains("\\u003c"));
    }

    @Test
    public void staysValidJsonAndRoundTrips() throws Exception {
        String payload = "</script><img src=x onerror=alert(1)>&'\"\\ 中文";
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("detail", payload);
        data.put("price", 12.50);
        String json = ScriptJson.of(data);
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
        Map<?, ?> parsed = new ObjectMapper().readValue(json, Map.class);
        assertEquals(payload, parsed.get("detail"));
        assertEquals(12.5, ((Number) parsed.get("price")).doubleValue(), 0.0001);
    }

    @Test
    public void emptyMapSerializesToEmptyObject() {
        assertEquals("{}", ScriptJson.of(new LinkedHashMap<String, Object>()));
    }
}