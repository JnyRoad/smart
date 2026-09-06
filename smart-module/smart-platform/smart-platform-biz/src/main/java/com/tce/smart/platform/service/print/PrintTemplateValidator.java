package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.tce.smart.platform.api.dto.req.print.PrintTemplateRequest;
import com.tce.smart.platform.core.entity.print.PrintTemplateVersion;
import org.springframework.stereotype.Component;
import java.util.*;

/** 固定单页、字段白名单、组件边界和分类规则；渲染验证作为发布的第二道门禁。 */
@Component
public class PrintTemplateValidator {
    public static final Set<String> FIELD_KEYS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("staffName", "staffNo", "departmentName", "companyName", "cardNo", "employeeGradeName", "visitorName", "visitorCredentialPayload", "parkName", "validFrom", "validTo")));
    public static final Set<String> RESOURCE_KEYS = Collections.singleton("personPhoto");
    private final PrintAccessPolicy access;
    private final PrintFeatureProperties properties;
    public PrintTemplateValidator(PrintAccessPolicy access, PrintFeatureProperties properties) { this.access = access; this.properties = properties; }

    public void validate(String parkId, PrintTemplateRequest request) {
        name(request.getName()); key(request.getClassificationCode());
        List<String> classifications = properties.getClassificationCodes().get(request.getPrintItemType() + ":" + request.getPersonType());
        if (classifications == null || classifications.isEmpty()) throw new PrintApiException(503, "PRINT_CLASSIFICATION_NOT_CONFIGURED", "该人员类型的受控分类尚未配置");
        if (!classifications.contains(request.getClassificationCode())) invalid(request.getFaceRole(), "classificationCode", "CLASSIFICATION_NOT_ALLOWED");
        boolean staff = "STAFF_CARD".equals(request.getPrintItemType()) && Arrays.asList("EMPLOYEE", "OUTSOURCED", "DISPATCHED").contains(request.getPersonType());
        boolean visitor = "VISITOR_SLIP".equals(request.getPrintItemType()) && Arrays.asList("VISITOR", "SUPPLIER").contains(request.getPersonType());
        if ((!staff && !visitor) || !Arrays.asList("FRONT", "BACK").contains(request.getFaceRole()) || (visitor && !"FRONT".equals(request.getFaceRole())) || !Integer.valueOf(1).equals(request.getSideCount())) invalid(request.getFaceRole(), "sideCount", "SINGLE_FACE_REQUIRED");
        PrintJson.limit(request.getLayoutJson(), 2 * 1024 * 1024); PrintJson.limit(request.getFieldSchemaJson(), 256 * 1024); PrintJson.limit(request.getPageSpecJson(), 256 * 1024);
        PrintJson.limit(request.getResourceManifest(), 256 * 1024);
        JsonNode layout = request.getLayoutJson(), page = request.getPageSpecJson(), fields = request.getFieldSchemaJson().path("fields");
        if (!layout.isObject() || !page.isObject() || !fields.isArray()) invalid(request.getFaceRole(), "layoutJson", "INVALID_STRUCTURE");
        if (!request.getFaceRole().equals(layout.path("faceRole").asText()) || !layout.path("sideCount").isIntegralNumber() || layout.path("sideCount").asInt() != 1 || layout.path("schemaVersion").asInt() != 1 || !layout.path("schemas").isArray() || layout.path("schemas").size() != 1 || !layout.path("schemas").get(0).isArray()) invalid(request.getFaceRole(), "schemas", "SINGLE_PAGE_REQUIRED");
        double width = dimension(page.path("widthMm"), request.getFaceRole()), height = dimension(page.path("heightMm"), request.getFaceRole());
        if (!page.path("maxPageCount").isIntegralNumber() || page.path("maxPageCount").asInt() != 1 || !Arrays.asList("LANDSCAPE", "PORTRAIT").contains(page.path("orientation").asText())) invalid(request.getFaceRole(), "pageSpecJson", "INVALID_PAGE_SPEC");
        if (("LANDSCAPE".equals(page.path("orientation").asText()) && width < height) || ("PORTRAIT".equals(page.path("orientation").asText()) && width > height)) invalid(request.getFaceRole(), "orientation", "PAGE_ORIENTATION_MISMATCH");
        if (layout.has("pageSpecJson") && !layout.get("pageSpecJson").equals(page)) invalid(request.getFaceRole(), "pageSpecJson", "PAGE_SPEC_MISMATCH");
        if (layout.has("basePdf") && (!layout.path("basePdf").isObject() || Math.abs(layout.path("basePdf").path("width").asDouble(-1) - width) > 0.001 || Math.abs(layout.path("basePdf").path("height").asDouble(-1) - height) > 0.001)) invalid(request.getFaceRole(), "basePdf", "PAGE_SPEC_MISMATCH");
        access.rejectUnsafeJson(layout); access.rejectUnsafeJson(request.getFieldSchemaJson()); access.rejectUnsafeJson(page); access.rejectUnsafeJson(request.getResourceManifest());
        access.validateManifest(parkId, request.getResourceManifest());
        Map<String, JsonNode> resources = new HashMap<>(); for (JsonNode resource : request.getResourceManifest()) resources.put(resource.path("objectId").asText(), resource);
        if (layout.hasNonNull("basePdfRef")) checkReference(layout.get("basePdfRef"), resources, request.getFaceRole(), "basePdfRef");
        Map<String, JsonNode> components = new HashMap<>();
        for (JsonNode component : layout.get("schemas").get(0)) {
            String componentName = component.path("name").asText(); componentName(componentName);
            if (components.put(componentName, component) != null) invalid(request.getFaceRole(), componentName, "DUPLICATE_COMPONENT_NAME");
            String type = component.path("type").asText();
            if (!Arrays.asList("text", "image", "qrcode", "code128", "rectangle", "line", "ellipse").contains(type)) invalid(request.getFaceRole(), componentName, "COMPONENT_NOT_ALLOWED");
            JsonNode position = component.path("position"); double x = coordinate(position.path("x")), y = coordinate(position.path("y"));
            double w = dimension(component.path("width"), request.getFaceRole()), h = dimension(component.path("height"), request.getFaceRole());
            if (x < 0 || y < 0 || x + w > width + 0.001 || y + h > height + 0.001) invalid(request.getFaceRole(), componentName, "COMPONENT_OUT_OF_BOUNDS");
            if (component.has("rotate") && (!component.path("rotate").isNumber() || component.path("rotate").asDouble() != 0)) invalid(request.getFaceRole(), componentName, "ROTATION_NOT_SUPPORTED");
            if ("text".equals(type)) {
                String font = component.path("fontName").asText("NotoSansSC");
                if (!properties.getAllowedFonts().contains(font)) invalid(request.getFaceRole(), componentName, "FONT_NOT_REGISTERED");
                if (component.has("fontSize") && (!component.path("fontSize").isNumber() || component.path("fontSize").asDouble() <= 0 || component.path("fontSize").asDouble() > 200)) invalid(request.getFaceRole(), componentName, "INVALID_FONT_SIZE");
            }
            if (component.has("resourceRef")) checkReference(component.path("resourceRef"), resources, request.getFaceRole(), componentName);
        }
        Set<String> boundComponents = new HashSet<>(); boolean securityQr = false;
        for (JsonNode field : fields) {
            String fieldKey = field.path("key").asText(), componentName = field.path("schemaName").asText();
            if ((!FIELD_KEYS.contains(fieldKey) && !RESOURCE_KEYS.contains(fieldKey)) || !components.containsKey(componentName) || !field.path("required").isBoolean() || !boundComponents.add(componentName)) invalid(request.getFaceRole(), componentName, "FIELD_NOT_ALLOWED");
            JsonNode component = components.get(componentName);
            if (RESOURCE_KEYS.contains(fieldKey)) {
                if (!"image".equals(component.path("type").asText()) || !field.path("required").asBoolean() || component.has("resourceRef") || component.has("content") && (!component.path("content").isTextual() || !component.path("content").asText().isEmpty())) invalid(request.getFaceRole(), componentName, "PHOTO_BINDING_INVALID");
                continue;
            }
            if (!Arrays.asList("text", "qrcode", "code128").contains(component.path("type").asText())) invalid(request.getFaceRole(), componentName, "FIELD_NOT_ALLOWED");
            if ("visitorCredentialPayload".equals(fieldKey)) {
                if (!visitor || !"VISITOR_SECURITY".equals(request.getClassificationCode()) || !"qrcode".equals(components.get(componentName).path("type").asText()) || !field.path("required").asBoolean()) invalid(request.getFaceRole(), componentName, "VISITOR_CREDENTIAL_BINDING_REQUIRED");
                securityQr = true;
            }
            if (visitor && Arrays.asList("staffName", "staffNo", "departmentName", "cardNo", "employeeGradeName").contains(fieldKey)) invalid(request.getFaceRole(), componentName, "FIELD_NOT_ALLOWED");
            if (staff && "visitorName".equals(fieldKey)) invalid(request.getFaceRole(), componentName, "FIELD_NOT_ALLOWED");
        }
        // 动态照片仅存绑定元数据；固定图片继续引用受控清单，不允许把图像字节藏在草稿中。
        for (Map.Entry<String, JsonNode> entry : components.entrySet()) {
            JsonNode component = entry.getValue();
            if ("image".equals(component.path("type").asText())) {
                if (component.has("content") && (!component.path("content").isTextual() || !component.path("content").asText().isEmpty())) invalid(request.getFaceRole(), entry.getKey(), "IMAGE_CONTENT_NOT_ALLOWED");
                if (!boundComponents.contains(entry.getKey())) checkReference(component.path("resourceRef"), resources, request.getFaceRole(), entry.getKey());
            }
        }
        if ("VISITOR_SECURITY".equals(request.getClassificationCode()) && (!visitor || !securityQr)) invalid(request.getFaceRole(), "visitorCredentialPayload", "SECURITY_QR_REQUIRED");
    }
    private void checkReference(JsonNode reference, Map<String, JsonNode> resources, String face, String component) {
        JsonNode resource = resources.get(reference.path("objectId").asText());
        if (resource == null || !resource.path("contentHash").equals(reference.path("contentHash"))) invalid(face, component, "RESOURCE_NOT_IN_MANIFEST");
        if (resource.hasNonNull("bindingKey") || resource.hasNonNull("subjectId") || resource.hasNonNull("subjectType") || "PHOTO".equals(resource.path("purpose").asText())) invalid(face, component, "PHOTO_BINDING_REQUIRED");
    }
    private double coordinate(JsonNode node) { return node.isNumber() && Double.isFinite(node.asDouble()) ? node.asDouble() : -1; }
    private double dimension(JsonNode node, String face) {
        double value = coordinate(node); if (value <= 0 || value > 2000) invalid(face, "dimensions", "INVALID_DIMENSIONS"); return value;
    }
    public static void name(String name) { if (name == null || name.trim().isEmpty() || name.codePointCount(0, name.length()) > 100) invalid(null, "name", "INVALID_NAME"); }
    /** 组件名称用于后勤定位，允许中文；业务代码继续使用独立的ASCII键校验。 */
    private static void componentName(String name) {
        if (name == null || !name.matches("[\\p{L}\\p{N}_][\\p{L}\\p{N}_.: -]{0,63}")) invalid(null,"name","INVALID_COMPONENT_NAME");
    }
    public static void key(String key) { if (key == null || !key.matches("[A-Za-z0-9_][A-Za-z0-9_.:-]{0,63}")) invalid(null, "key", "INVALID_KEY"); }
    public static void invalid(String face, String component, String code) {
        Map<String, Object> violation = new LinkedHashMap<>(); violation.put("face", face); violation.put("component", component); violation.put("code", code);
        throw new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "模板校验失败", Collections.singletonMap("violations", Collections.singletonList(violation)));
    }
    public static String contentHash(PrintTemplateVersion version) {
        Map<String, Object> content = new LinkedHashMap<>(); content.put("layoutJson", PrintJson.read(version.getLayoutJson())); content.put("fieldSchemaJson", PrintJson.read(version.getFieldSchemaJson())); content.put("pageSpecJson", PrintJson.read(version.getPageSpecJson())); content.put("resourceManifest", PrintJson.read(version.getResourceManifestJson())); return PrintJson.hash(content);
    }
    public static void integrity(PrintTemplateVersion version) {
        if (version == null || !Objects.equals(version.getContentHash(), contentHash(version))) throw new PrintApiException(422, "TEMPLATE_CONTENT_HASH_MISMATCH", "模板内容哈希校验失败");
    }
}
