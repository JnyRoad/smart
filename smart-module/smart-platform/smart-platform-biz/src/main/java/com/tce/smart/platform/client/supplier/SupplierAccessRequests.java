package com.tce.smart.platform.client.supplier;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;

/** 严格字符串请求；默认对象描述避免Spring DEBUG打印扫码正文。 */
public final class SupplierAccessRequests {
    private SupplierAccessRequests() { }

    @JsonDeserialize(using = VerificationDeserializer.class)
    public static final class Verification {
        final String credentialCode;
        final String postId;
        Verification(String credentialCode, String postId) { this.credentialCode = credentialCode; this.postId = postId; }
    }

    @JsonDeserialize(using = EventDeserializer.class)
    public static final class Event {
        final String verificationId;
        final String postId;
        final String direction;
        Event(String verificationId, String postId, String direction) {
            this.verificationId = verificationId; this.postId = postId; this.direction = direction;
        }
    }

    public static final class VerificationDeserializer extends JsonDeserializer<Verification> {
        @Override public Verification deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            JsonNode body = read(parser, "credentialCode", "postId");
            return new Verification(body.get("credentialCode").textValue(), body.get("postId").textValue());
        }
    }
    public static final class EventDeserializer extends JsonDeserializer<Event> {
        @Override public Event deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            JsonNode body = read(parser, "verificationId", "postId", "direction");
            return new Event(body.get("verificationId").textValue(), body.get("postId").textValue(), body.get("direction").textValue());
        }
    }
    private static JsonNode read(JsonParser parser, String... expected) throws IOException {
        parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        JsonNode body;
        try { body = parser.readValueAsTree(); }
        catch (IOException malformed) { throw new SupplierAccessHttpException(400); }
        if (body == null || !body.isObject() || body.size() != expected.length) throw new SupplierAccessHttpException(400);
        for (String field : expected) {
            JsonNode value = body.get(field);
            if (value == null || !value.isTextual() || value.textValue().isEmpty() || value.textValue().length() > 128) throw new SupplierAccessHttpException(400);
        }
        return body;
    }
}
