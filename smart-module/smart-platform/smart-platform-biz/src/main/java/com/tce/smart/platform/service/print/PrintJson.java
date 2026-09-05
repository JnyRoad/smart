package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** 规范化完整 JSON 后计算内容哈希；字段顺序不影响幂等判断。 */
public final class PrintJson {
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION).enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private PrintJson() { }
    public static JsonNode read(String value) {
        try { return MAPPER.readTree(value); }
        catch (Exception e) { throw new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "JSON 内容无效"); }
    }
    public static JsonNode tree(Object value) { return MAPPER.valueToTree(value); }
    public static ObjectNode object() { return MAPPER.createObjectNode(); }
    public static ArrayNode array() { return MAPPER.createArrayNode(); }
    public static String canonical(Object value) { return sort(tree(value), 0).toString(); }
    private static JsonNode sort(JsonNode node, int depth) {
        if (depth > 32) throw new PrintApiException(422, "PAYLOAD_LIMIT_EXCEEDED", "JSON 嵌套过深");
        if (node.isObject()) {
            ObjectNode result = object(); List<String> keys = new ArrayList<>(); node.fieldNames().forEachRemaining(keys::add); Collections.sort(keys);
            for (String key : keys) result.set(key, sort(node.get(key), depth + 1)); return result;
        }
        if (node.isArray()) { ArrayNode result = array(); for (JsonNode child : node) result.add(sort(child, depth + 1)); return result; }
        return node;
    }
    @SuppressWarnings("unchecked") public static Map<String, Object> map(Object value) { return MAPPER.convertValue(value, LinkedHashMap.class); }
    public static String hash(Object value) { return hashBytes(canonical(value).getBytes(StandardCharsets.UTF_8)); }
    public static String hashBytes(byte[] bytes) {
        try { StringBuilder result = new StringBuilder("sha256:"); for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes)) result.append(String.format("%02x", b & 255)); return result.toString(); }
        catch (Exception e) { throw new IllegalStateException("SHA-256 不可用", e); }
    }
    public static void limit(JsonNode node, int bytes) {
        if (node == null || node.isNull()) throw new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "缺少模板内容");
        if (canonical(node).getBytes(StandardCharsets.UTF_8).length > bytes) throw new PrintApiException(422, "PAYLOAD_LIMIT_EXCEEDED", "模板内容超出限制");
    }
}
