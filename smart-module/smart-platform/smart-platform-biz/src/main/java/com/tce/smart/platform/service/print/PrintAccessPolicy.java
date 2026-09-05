package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.tce.smart.common.security.service.SmartUser;
import org.springframework.lang.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Pattern;

/** 每个对象分别检查登录身份、操作权限、园区与文件授权，不能只过滤列表。 */
@Component
public class PrintAccessPolicy {
    private static final Pattern UNSAFE = Pattern.compile("(?i)(https?://|file:|javascript:|data:|(?:^|[\\s\"'])(?:/[\\w.]|[a-z]:[\\\\/])|\\.\\.[\\\\/]|<\\s*(?:script|iframe|object)\\b)");
    private final PrintFeatureProperties properties;
    private final PrintResourceStore resources;
    @Autowired public PrintAccessPolicy(PrintFeatureProperties properties, @Nullable PrintResourceStore resources) { this.properties = properties; this.resources = resources; }

    public SmartUser user() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof SmartUser)) throw new PrintApiException(401, "PRINT_AUTHENTICATION_REQUIRED", "需要平台登录身份");
        SmartUser user = (SmartUser) auth.getPrincipal();
        if (!user.isEnabled() || !user.isAccountNonLocked() || !user.isAccountNonExpired() || !user.isCredentialsNonExpired() || user.getId() == null) throw new PrintApiException(401, "PRINT_AUTHENTICATION_REQUIRED", "登录身份不可用");
        if (!properties.isEnabled()) throw new PrintApiException(503, "PRINT_FEATURE_DISABLED", "打印模板功能尚未启用");
        return user;
    }
    public String actor() { return String.valueOf(user().getId()); }
    public String resolvePark(String parkId) {
        List<Integer> parks = user().getParkIdList();
        if (parkId == null || parkId.trim().isEmpty()) {
            if (parks == null || parks.isEmpty()) throw new PrintApiException(403, "PRINT_SCOPE_DENIED", "没有获准园区");
            Set<Integer> distinct = new HashSet<>(parks); distinct.remove(null);
            if (distinct.size() != 1) throw new PrintApiException(422, "PRINT_PARK_REQUIRED", "请选择园区");
            parkId = String.valueOf(distinct.iterator().next());
        }
        final String selected = parkId;
        if (parks == null || parks.stream().filter(Objects::nonNull).noneMatch(id -> String.valueOf(id).equals(selected))) throw new PrintApiException(403, "PRINT_SCOPE_DENIED", "对象不属于获准园区");
        return parkId;
    }
    public void require(String capability, String parkId) {
        SmartUser current = user(); resolvePark(parkId);
        String permission = properties.getPermissions().get(capability);
        if (permission == null || permission.trim().isEmpty() || permission.contains("*")) throw new PrintApiException(503, "PRINT_PERMISSION_NOT_CONFIGURED", "打印操作权限尚未配置");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream().noneMatch(a -> permission.equals(a.getAuthority()))) throw new PrintApiException(403, "PRINT_PERMISSION_DENIED", "没有此打印操作权限");
    }
    public void rejectUnsafeJson(JsonNode node) { rejectUnsafeJson(node, 0); }
    private void rejectUnsafeJson(JsonNode node, int depth) {
        if (node == null) return;
        if (depth > 32) throw new PrintApiException(422, "PAYLOAD_LIMIT_EXCEEDED", "JSON 嵌套过深");
        if (node.isTextual() && UNSAFE.matcher(node.asText()).find()) throw new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "模板不允许外部地址、路径或脚本");
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) { Map.Entry<String, JsonNode> entry = fields.next();
                if (entry.getKey().matches("(?i)(url|href|src|path|script|javascript|onclick|onload)")) throw new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "模板包含未获准属性");
                rejectUnsafeJson(entry.getValue(), depth + 1);
            }
        } else if (node.isArray()) for (JsonNode child : node) rejectUnsafeJson(child, depth + 1);
    }
    public void validateManifest(String parkId, JsonNode manifest) {
        resolvePark(parkId);
        if (manifest == null || !manifest.isArray()) throw new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "资源清单必须为数组");
        if (manifest.size() > 32) throw new PrintApiException(422, "PAYLOAD_LIMIT_EXCEEDED", "资源数量超出限制");
        Set<String> seen = new HashSet<>(); long total = 0;
        for (JsonNode reference : manifest) {
            PrintResourceStore.RegisteredResource stored = registered(parkId, reference.path("objectId").asText());
            if (!seen.add(stored.getObjectId())) throw new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "资源重复");
            JsonNode metadata = PrintJson.tree(stored);
            for (String key : Arrays.asList("contentHash", "mediaType", "parkId", "purpose", "accessScope")) if (!metadata.path(key).equals(reference.path(key))) throw new PrintApiException(403, "PRINT_SCOPE_DENIED", "资源元数据不匹配");
            if (!reference.path("sizeBytes").isIntegralNumber() || reference.path("sizeBytes").asLong() != stored.getSizeBytes()) throw new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "资源大小不匹配");
            if (stored.getSourceRevision() != null && !Objects.equals(stored.getSourceRevision(), reference.path("sourceRevision").asText(null))) throw new PrintApiException(403, "PRINT_SCOPE_DENIED", "资源修订不匹配");
            total += stored.getSizeBytes(); if (total > 32L * 1024 * 1024) throw new PrintApiException(422, "PAYLOAD_LIMIT_EXCEEDED", "资源总大小超出限制");
        }
    }
    private PrintResourceStore.RegisteredResource registered(String parkId, String objectId) {
        if (resources == null) throw new PrintApiException(503, "PRINT_RESOURCE_NOT_CONFIGURED", "受控文件服务尚未配置");
        uuid(objectId); PrintResourceStore.RegisteredResource r = resources.describe(objectId);
        if (r == null) throw new PrintApiException(404, "PRINT_RESOURCE_NOT_FOUND", "资源不存在");
        if (!objectId.equals(r.getObjectId()) || !parkId.equals(r.getParkId()) || !resources.canAccess(actor(), r)) throw new PrintApiException(403, "PRINT_SCOPE_DENIED", "无权使用该资源");
        if (r.getSizeBytes() == null || r.getSizeBytes() <= 0 || r.getSizeBytes() > 20L * 1024 * 1024) throw new PrintApiException(422, "PAYLOAD_LIMIT_EXCEEDED", "单个资源大小超出限制");
        boolean allowed = (Arrays.asList("PHOTO", "LOGO", "BACKGROUND").contains(r.getPurpose()) && Arrays.asList("image/png", "image/jpeg").contains(r.getMediaType()))
                || ("FONT".equals(r.getPurpose()) && Arrays.asList("font/ttf", "font/otf", "application/x-font-ttf").contains(r.getMediaType()))
                || ("BASE_PDF".equals(r.getPurpose()) && "application/pdf".equals(r.getMediaType()));
        if (!allowed || !Arrays.asList("TEMPLATE", "STAFF_RECORD", "PRINT_JOB").contains(r.getAccessScope()) || r.getContentHash() == null || !r.getContentHash().matches("sha256:[a-f0-9]{64}")) throw new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "资源用途或类型未获准");
        if ("PHOTO".equals(r.getPurpose()) && (!"STAFF_RECORD".equals(r.getAccessScope()) || r.getSubjectId() == null)) throw new PrintApiException(403, "PRINT_SCOPE_DENIED", "照片缺少人员归属");
        return r;
    }
    public byte[] readResource(String parkId, String objectId, String expectedHash) {
        require("resource", parkId); PrintResourceStore.RegisteredResource r = registered(parkId, objectId);
        if (!r.getContentHash().equals(expectedHash)) throw new PrintApiException(403, "PRINT_SCOPE_DENIED", "资源哈希不匹配");
        byte[] data = resources.read(objectId);
        if (data == null || data.length != r.getSizeBytes() || !PrintJson.hashBytes(data).equals(expectedHash)) throw new PrintApiException(422, "PRINT_RESOURCE_HASH_MISMATCH", "资源内容校验失败");
        return data;
    }
    public ArrayNode resolvedResources(String parkId, JsonNode manifest) {
        validateManifest(parkId, manifest); ArrayNode resolved = PrintJson.array();
        for (JsonNode r : manifest) { com.fasterxml.jackson.databind.node.ObjectNode copy = r.deepCopy(); copy.put("contentBase64", Base64.getEncoder().encodeToString(readResource(parkId, r.path("objectId").asText(), r.path("contentHash").asText()))); resolved.add(copy); }
        return resolved;
    }
    public static void uuid(String id) {
        if (id == null || !id.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")) throw new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "打印对象标识格式无效");
    }
}
