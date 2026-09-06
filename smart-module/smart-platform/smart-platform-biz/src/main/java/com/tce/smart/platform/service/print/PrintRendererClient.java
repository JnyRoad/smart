package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.core.entity.print.PrintTemplate;
import com.tce.smart.platform.core.entity.print.PrintTemplateVersion;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;

/** 内部预览校验不产生打印任务；复核真实 PDF 后才允许保存发布指针。 */
@Component
public class PrintRendererClient implements PrintPublicationValidator {
    private static final int MAX_PDF = 32 * 1024 * 1024;
    private final PrintFeatureProperties properties;
    private final PrintAccessPolicy access;
    private final RestTemplate http;
    @Autowired public PrintRendererClient(PrintFeatureProperties properties, PrintAccessPolicy access) { this(properties, access, createHttp(properties)); }
    public PrintRendererClient(PrintFeatureProperties properties, PrintAccessPolicy access, RestTemplate http) { this.properties = properties; this.access = access; this.http = http; }
    private static RestTemplate createHttp(PrintFeatureProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
            @Override protected void prepareConnection(HttpURLConnection connection, String method) throws IOException { super.prepareConnection(connection, method); connection.setInstanceFollowRedirects(false); }
        };
        factory.setConnectTimeout(Math.max(100, Math.min(10000, properties.getRendererConnectTimeoutMs())));
        factory.setReadTimeout(Math.max(100, Math.min(60000, properties.getRendererReadTimeoutMs())));
        return new RestTemplate(factory);
    }
    @Override public Map<String, Object> validate(PrintTemplate template, PrintTemplateVersion version) {
        JsonNode result = renderPreview(UUID.randomUUID().toString(), template.getPrintItemType(), Collections.singletonList(version), PrintJson.object());
        Map<String, Object> report = new LinkedHashMap<>(); report.put("status", "READY"); report.put("validatorVersion", 1); report.put("renderRequestId", result.path("renderRequestId").asText()); report.put("validatedAt", java.time.Instant.now().toString()); report.put("artifactHash", result.path("artifacts").get(0).path("sha256").asText()); report.put("pageCount", 1); return report;
    }
    public JsonNode renderPreview(String previewId, String printItemType, List<PrintTemplateVersion> versions, JsonNode sampleData) {
        URI endpoint = endpoint(); String requestId = UUID.randomUUID().toString();
        if (versions.isEmpty() || versions.size() > 2) invalid();
        ObjectNode request = PrintJson.object().put("requestId", requestId).put("purpose", "PREVIEW").put("previewId", previewId).put("printItemType", printItemType).put("expectedFaceCount", versions.size()).put("printMode", "STAFF_CARD".equals(printItemType) ? "MANUAL_DUPLEX" : "SINGLE");
        com.fasterxml.jackson.databind.node.ArrayNode faces = request.putArray("faceSources");
        for (PrintTemplateVersion version : versions) {
            JsonNode page = PrintJson.read(version.getPageSpecJson()); ObjectNode layout = (ObjectNode) PrintJson.read(version.getLayoutJson()).deepCopy();
            if (layout.hasNonNull("basePdfRef")) throw new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "当前渲染器不支持受控底图转换");
            layout.remove("basePdfRef");
            ObjectNode base = PrintJson.object().put("width", page.path("widthMm").asDouble()).put("height", page.path("heightMm").asDouble()); base.putArray("padding").add(0).add(0).add(0).add(0);
            layout.set("basePdf", base); layout.set("pageSpecJson", page);
            JsonNode schema = PrintJson.read(version.getFieldSchemaJson()); layout.set("fieldSchemaJson", schema);
            ObjectNode fields = PrintJson.object();
            for (JsonNode binding : schema.path("fields")) {
                String key = binding.path("key").asText();
                if (PrintTemplateValidator.RESOURCE_KEYS.contains(key)) continue;
                if (!PrintTemplateValidator.FIELD_KEYS.contains(key)) PrintTemplateValidator.invalid(version.getFaceRole(), key, "FIELD_NOT_ALLOWED");
                String example = "visitorCredentialPayload".equals(key) ? "synthetic-preview-credential" : "validFrom".equals(key) ? "2026-01-01" : "validTo".equals(key) ? "2026-12-31" : "staffNo".equals(key) || "cardNo".equals(key) ? "S001" : "示例";
                fields.put(key, sampleData != null && sampleData.has(key) ? sampleData.path(key).asText() : example);
            }
            ObjectNode face = faces.addObject().put("face", version.getFaceRole());
            if ("PUBLISHED".equals(version.getVersionStatus())) face.put("templateVersionId", version.getTemplateVersionId());
            else face.put("templateId", version.getTemplateId()).put("draftRevision", version.getDraftRevision());
            face.set("template", layout); ObjectNode resolved = face.putObject("resolvedInput"); resolved.set("fields", fields);
            com.fasterxml.jackson.databind.node.ArrayNode resources = access.resolvedResources(version.getParkId(), PrintJson.read(version.getResourceManifestJson()));
            if (usesPersonPhoto(layout, schema, version.getFaceRole())) {
                resolved.put("subjectType", "SYNTHETIC").put("subjectId", "synthetic-preview").put("synthetic", true);
                resources.add(syntheticPhoto());
            }
            checkResourceBudget(resources); face.set("resourceManifest", resources);
        }
        return renderFrozen(request);
    }
    /** 创建时解析授权数据与资源；之后渲染只消费这个私有冻结请求。 */
    public ObjectNode freezeJobRequest(String jobId,String printMode,List<PrintTemplateVersion> versions,ObjectNode subject) {
        if(versions.isEmpty()||versions.size()>2) invalid();
        ObjectNode request=PrintJson.object().put("requestId",UUID.randomUUID().toString()).put("purpose","PRINT").put("jobId",jobId).put("printItemType",subject.path("printItemType").asText()).put("printMode",printMode).put("expectedFaceCount",versions.size());
        com.fasterxml.jackson.databind.node.ArrayNode faces=request.putArray("faceSources");
        JsonNode input=subject.path("fields");if(!input.isObject()) invalid();
        input.fieldNames().forEachRemaining(key->{if(!PrintTemplateValidator.FIELD_KEYS.contains(key))throw new PrintApiException(422,"FIELD_NOT_ALLOWED","业务字段未获准");});
        for(PrintTemplateVersion version:versions){
            PrintTemplateValidator.integrity(version);if(!"PUBLISHED".equals(version.getVersionStatus()))invalid();
            JsonNode page=PrintJson.read(version.getPageSpecJson());ObjectNode layout=(ObjectNode)PrintJson.read(version.getLayoutJson());
            if(layout.hasNonNull("basePdfRef"))throw new PrintApiException(422,"TEMPLATE_VALIDATION_FAILED","当前渲染器不支持受控底图转换");layout.remove("basePdfRef");
            ObjectNode base=PrintJson.object().put("width",page.path("widthMm").asDouble()).put("height",page.path("heightMm").asDouble());base.putArray("padding").add(0).add(0).add(0).add(0);layout.set("basePdf",base);layout.set("pageSpecJson",page);
            JsonNode schema=PrintJson.read(version.getFieldSchemaJson());layout.set("fieldSchemaJson",schema);ObjectNode fields=PrintJson.object();
            for(JsonNode binding:schema.path("fields")){String key=binding.path("key").asText();if(PrintTemplateValidator.RESOURCE_KEYS.contains(key))continue;if(!PrintTemplateValidator.FIELD_KEYS.contains(key))PrintTemplateValidator.invalid(version.getFaceRole(),key,"FIELD_NOT_ALLOWED");JsonNode value=input.path(key);if((!value.isTextual()||value.asText().trim().isEmpty())&&binding.path("required").asBoolean())throw new PrintApiException(422,"PRINT_SUBJECT_FIELD_REQUIRED","人员缺少模板必填字段");fields.put(key,value.isTextual()?value.asText():"");}
            ObjectNode face=faces.addObject().put("face",version.getFaceRole()).put("templateVersionId",version.getTemplateVersionId());face.set("template",layout);ObjectNode resolved=face.putObject("resolvedInput").put("subjectId",subject.path("subjectId").asText()).put("subjectType",subject.path("subjectType").asText());resolved.set("fields",fields);
            com.fasterxml.jackson.databind.node.ArrayNode resources=access.resolvedResources(version.getParkId(),PrintJson.read(version.getResourceManifestJson()));
            // 只冻结模板实际使用的照片；没有照片绑定时不复制无用人员资源。
            if (usesPersonPhoto(layout, schema, version.getFaceRole())) resources.add(freezePersonPhoto(subject));
            checkResourceBudget(resources); face.set("resourceManifest",resources);
        }
        PrintJson.limit(request,46*1024*1024);return request;
    }
    /** 冻结及合成预览都复核独立资源绑定，防历史或被篡改模板绕过草稿门禁。 */
    private boolean usesPersonPhoto(JsonNode layout, JsonNode schema, String face) {
        Map<String,JsonNode> components = new HashMap<>();
        for (JsonNode page : layout.path("schemas")) for (JsonNode component : page) components.put(component.path("name").asText(), component);
        Set<String> seen = new HashSet<>(); boolean uses = false;
        for (JsonNode binding : schema.path("fields")) {
            String name = binding.path("schemaName").asText(), key = binding.path("key").asText();
            JsonNode component = components.get(name);
            if (component == null || !seen.add(name) || !binding.path("required").isBoolean()) PrintTemplateValidator.invalid(face, name, "FIELD_NOT_ALLOWED");
            if (PrintTemplateValidator.RESOURCE_KEYS.contains(key)) {
                if (!"image".equals(component.path("type").asText()) || !binding.path("required").asBoolean() || component.has("resourceRef") || component.has("content") && (!component.path("content").isTextual() || !component.path("content").asText().isEmpty())) PrintTemplateValidator.invalid(face, name, "PHOTO_BINDING_INVALID");
                uses = true;
            } else if (!PrintTemplateValidator.FIELD_KEYS.contains(key) || "image".equals(component.path("type").asText())) PrintTemplateValidator.invalid(face, name, "FIELD_NOT_ALLOWED");
        }
        return uses;
    }

    /** 已授权业务来源的照片只接收规定字段；冻结后归属和字节全部自包含，不再读取照片源。 */
    private ObjectNode freezePersonPhoto(ObjectNode subject) {
        JsonNode resources = subject.path("resources");
        if (resources.isMissingNode() || resources.isArray() && resources.size() == 0) throw new PrintApiException(422, "PRINT_SUBJECT_PHOTO_REQUIRED", "人员缺少模板必填照片");
        if (!resources.isArray() || resources.size() != 1) throw invalidPhoto();
        JsonNode photo = resources.get(0);
        Set<String> keys = new HashSet<>(Arrays.asList("bindingKey", "mediaType", "sha256", "bytesBase64"));
        if (!photo.isObject() || photo.size() != keys.size()) throw invalidPhoto();
        photo.fieldNames().forEachRemaining(key -> { if (!keys.contains(key)) throw invalidPhoto(); });
        if (!"personPhoto".equals(photo.path("bindingKey").asText()) || !photo.path("mediaType").isTextual() || !photo.path("sha256").isTextual() || !photo.path("bytesBase64").isTextual()) throw invalidPhoto();
        String subjectId = subject.path("subjectId").asText(), subjectType = subject.path("subjectType").asText();
        if (!subject.path("subjectId").isTextual() || subjectId.trim().isEmpty() || subjectId.length() > 128 || !subject.path("subjectType").isTextual() || subjectType.trim().isEmpty() || "SYNTHETIC".equals(subjectType)) throw invalidPhoto();
        String encoded = photo.path("bytesBase64").asText(), mediaType = photo.path("mediaType").asText();
        if (encoded.length() > ((20 * 1024 * 1024 + 2) / 3) * 4) throw invalidPhoto();
        final byte[] bytes;
        try { bytes = Base64.getDecoder().decode(encoded); } catch (IllegalArgumentException error) { throw invalidPhoto(); }
        if (bytes.length == 0 || bytes.length > 20 * 1024 * 1024 || !Base64.getEncoder().encodeToString(bytes).equals(encoded) || !PrintJson.hashBytes(bytes).equals(photo.path("sha256").asText())) throw invalidPhoto();
        validatePhotoImage(bytes, mediaType);
        return photoResource(bytes, mediaType, subjectType, subjectId);
    }

    /** 在任何像素分配前检查真实解码器报告的类型与尺寸，防伪扩展名及尺寸炸弹。 */
    private void validatePhotoImage(byte[] bytes, String mediaType) {
        if (!Arrays.asList("image/png", "image/jpeg").contains(mediaType)) throw invalidPhoto();
        // ImageIO会容错接受缺少文件尾和部分损坏的扫描数据，不能仅用成功解码判断照片完整。
        if ("image/png".equals(mediaType)) validatePngContainer(bytes);
        else if (bytes.length < 4 || (bytes[0] & 255) != 255 || (bytes[1] & 255) != 216 || (bytes[bytes.length - 2] & 255) != 255 || (bytes[bytes.length - 1] & 255) != 217) throw invalidPhoto();
        ImageReader reader = null;
        try (ImageInputStream image = new javax.imageio.stream.MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(image);
            if (!readers.hasNext()) throw invalidPhoto();
            reader = readers.next(); reader.setInput(image, true, true);
            reader.addIIOReadWarningListener((source, warning) -> { throw invalidPhoto(); });
            String format = reader.getFormatName();
            if ("image/png".equals(mediaType) ? !"png".equalsIgnoreCase(format) : !("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format))) throw invalidPhoto();
            int width = reader.getWidth(0), height = reader.getHeight(0);
            if (width <= 0 || height <= 0 || width > 4096 || height > 4096 || (long) width * height > 16000000 || reader.read(0) == null) throw invalidPhoto();
        } catch (IOException | IllegalArgumentException error) { throw invalidPhoto(); }
        finally { if (reader != null) reader.dispose(); }
    }

    /** 检查PNG块边界、CRC和唯一完整文件尾；不解压图片，也不按不可信块长度分配内存。 */
    private void validatePngContainer(byte[] bytes) {
        byte[] signature = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
        if (bytes.length < 33 || !Arrays.equals(signature, Arrays.copyOf(bytes, 8))) throw invalidPhoto();
        boolean header = false, pixels = false;
        for (int offset = 8; offset < bytes.length;) {
            if (bytes.length - offset < 12) throw invalidPhoto();
            long length = unsignedInt(bytes, offset);
            if (length > bytes.length - offset - 12) throw invalidPhoto();
            int size = (int)length, end = offset + size + 12;
            String type = new String(bytes, offset + 4, 4, StandardCharsets.US_ASCII);
            java.util.zip.CRC32 crc = new java.util.zip.CRC32();
            crc.update(bytes, offset + 4, size + 4);
            if (crc.getValue() != unsignedInt(bytes, end - 4)) throw invalidPhoto();
            if ("IHDR".equals(type)) {
                if (header || offset != 8 || size != 13) throw invalidPhoto();
                header = true;
            } else if (!header) throw invalidPhoto();
            if ("IDAT".equals(type) && size > 0) pixels = true;
            if ("IEND".equals(type)) {
                if (size != 0 || !pixels || end != bytes.length) throw invalidPhoto();
                return;
            }
            offset = end;
        }
        throw invalidPhoto();
    }
    private long unsignedInt(byte[] bytes, int offset) {
        return ((long)(bytes[offset] & 255) << 24) | ((long)(bytes[offset + 1] & 255) << 16) | ((long)(bytes[offset + 2] & 255) << 8) | (bytes[offset + 3] & 255);
    }

    private ObjectNode photoResource(byte[] bytes, String mediaType, String subjectType, String subjectId) {
        String contentHash = PrintJson.hashBytes(bytes);
        return PrintJson.object().put("objectId", UUID.randomUUID().toString()).put("bindingKey", "personPhoto").put("subjectType", subjectType).put("subjectId", subjectId)
            .put("mediaType", mediaType).put("contentHash", contentHash).put("sizeBytes", bytes.length).put("contentBase64", Base64.getEncoder().encodeToString(bytes));
    }

    /** 发布/设计预览生成几何占位；业务预览则始终复用已冻结的真实人员照片。 */
    private ObjectNode syntheticPhoto() {
        BufferedImage image = new BufferedImage(48, 64, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 64; y++) for (int x = 0; x < 48; x++) {
            boolean silhouette = (x - 24) * (x - 24) + (y - 20) * (y - 20) <= 81 || y >= 34 && Math.pow((x - 24) / 17.0, 2) + Math.pow((y - 53) / 21.0, 2) <= 1;
            image.setRGB(x, y, silhouette ? 0x6b7a91 : 0xe8edf3);
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", out)) throw invalidPhoto();
            return photoResource(out.toByteArray(), "image/png", "SYNTHETIC", "synthetic-preview").put("synthetic", true);
        } catch (IOException error) { throw invalidPhoto(); }
    }
    private void checkResourceBudget(com.fasterxml.jackson.databind.node.ArrayNode resources) {
        long total = 0;
        for (JsonNode resource : resources) total += resource.path("sizeBytes").asLong();
        if (resources.size() > 32 || total > 32L * 1024 * 1024) throw new PrintApiException(422, "PAYLOAD_LIMIT_EXCEEDED", "资源总大小超出限制");
    }
    private PrintApiException invalidPhoto() { return new PrintApiException(422, "PRINT_SUBJECT_PHOTO_INVALID", "人员照片的格式、内容或可信来源校验失败"); }

    /** 独立工作线程无需平台登录态，且绝不重新查询人员、模板或资源。 */
    public JsonNode renderFrozen(JsonNode request) {
        URI endpoint=endpoint();String requestId=request.path("requestId").asText();String identity="PREVIEW".equals(request.path("purpose").asText())?"previewId":"jobId";
        final byte[] body=PrintJson.canonical(request).getBytes(StandardCharsets.UTF_8);
        try {
            JsonNode result=http.execute(endpoint,HttpMethod.POST,outbound->{outbound.getHeaders().setContentType(MediaType.APPLICATION_JSON);outbound.getHeaders().setBearerAuth(properties.getRendererToken());outbound.getHeaders().set("X-Request-Id",requestId);outbound.getBody().write(body);},inbound->{if(inbound.getRawStatusCode()!=200)invalid();return PrintJson.read(new String(readLimited(inbound.getBody(),46*1024*1024),StandardCharsets.UTF_8));});
            if(result==null||!"READY".equals(result.path("status").asText())||!requestId.equals(result.path("renderRequestId").asText())||!request.path(identity).asText().equals(result.path(identity).asText())||!result.path("artifacts").isArray()||result.path("artifacts").size()!=request.path("faceSources").size())invalid();
            long total=0;for(int i=0;i<request.path("faceSources").size();i++){JsonNode source=request.path("faceSources").get(i),artifact=result.path("artifacts").get(i);validateArtifact(artifact,source.path("face").asText(),source.at("/template/pageSpecJson"),1);total+=artifact.path("bytes").asLong();}
            if(request.path("faceSources").size()==1&&result.hasNonNull("combinedArtifact"))invalid();
            if(request.path("faceSources").size()==2){JsonNode combined=result.path("combinedArtifact");validateArtifact(combined,null,request.path("faceSources").get(0).at("/template/pageSpecJson"),2);total+=combined.path("bytes").asLong();}if(total>MAX_PDF)throw new PrintApiException(422,"PAYLOAD_LIMIT_EXCEEDED","渲染制品总大小超出限制");return result;
        }catch(PrintApiException ex){throw ex;}catch(org.springframework.web.client.HttpClientErrorException ex){if(ex.getRawStatusCode()==422){List<PrintTemplateVersion> versions=new ArrayList<>();for(JsonNode face:request.path("faceSources")){PrintTemplateVersion v=new PrintTemplateVersion();v.setFaceRole(face.path("face").asText());v.setLayoutJson(face.path("template").toString());versions.add(v);}throw renderError(ex,versions);}if(ex.getRawStatusCode()==413)throw new PrintApiException(422,"PAYLOAD_LIMIT_EXCEEDED","渲染请求超过服务限制");throw new PrintApiException(503,"PRINT_RENDERER_UNAVAILABLE","可信渲染服务身份或地址不可用");}catch(Exception ex){throw new PrintApiException(503,"PRINT_RENDERER_UNAVAILABLE","可信渲染服务不可用");}
    }
    private PrintApiException renderError(org.springframework.web.client.HttpClientErrorException error, List<PrintTemplateVersion> versions) {
        List<Map<String,Object>> violations = new ArrayList<>();
        Map<String,Set<String>> namesByFace=new HashMap<>();
        for(PrintTemplateVersion version:versions) {
            Set<String> names=new HashSet<>();
            for(JsonNode page:PrintJson.read(version.getLayoutJson()).path("schemas")) for(JsonNode component:page) names.add(component.path("name").asText());
            namesByFace.put(version.getFaceRole(),names);
        }
        if (error.getResponseBodyAsByteArray()!=null && error.getResponseBodyAsByteArray().length <= 256 * 1024) {
            try {
                JsonNode details = PrintJson.read(new String(error.getResponseBodyAsByteArray(),StandardCharsets.UTF_8)).path("error").path("details");
                if (details.isArray()) for (JsonNode detail : details) {
                    if (violations.size() >= 64) break;
                    Map<String,Object> violation = new LinkedHashMap<>();
                    String code = detail.path("code").asText();
                    if (code.matches("[A-Z0-9_]{1,64}")) violation.put("code",code);
                    String face = detail.path("face").asText();
                    if (!namesByFace.containsKey(face) && versions.size() == 1) face=versions.get(0).getFaceRole();
                    if (namesByFace.containsKey(face)) {
                        violation.put("face",face);
                        String schemaName=detail.path("schemaName").asText();
                        // 只回传本次授权模板中既有的名称，不把远端任意文字当作组件定位。
                        if(namesByFace.get(face).contains(schemaName)) violation.put("schemaName",schemaName);
                    }
                    if (!violation.isEmpty()) violations.add(violation);
                }
            } catch (PrintApiException ignored) { }
        }
        return new PrintApiException(422,"RENDER_VALIDATION_FAILED","可信渲染校验未通过",Collections.singletonMap("violations",violations));
    }

    private URI endpoint() {
        String url = properties.getRendererUrl(), token = properties.getRendererToken();
        if (url == null || url.trim().isEmpty() || token == null || token.trim().isEmpty() || token.indexOf('\r') >= 0 || token.indexOf('\n') >= 0) throw new PrintApiException(503, "PRINT_RENDERER_NOT_CONFIGURED", "可信渲染地址与服务身份尚未配置");
        try {
            URI uri = URI.create(url);
            if (!Arrays.asList("http", "https").contains(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null || !(uri.getPath().isEmpty() || "/".equals(uri.getPath()))) throw new IllegalArgumentException();
            for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                if (!address.isLoopbackAddress() && !address.isSiteLocalAddress()) throw new IllegalArgumentException();
                // 跨主机必须使用TLS，避免将服务令牌和模板资源明文发送到园区网络。
                if ("http".equals(uri.getScheme()) && !address.isLoopbackAddress()) throw new IllegalArgumentException();
            }
            return URI.create(url.replaceAll("/$", "") + "/internal/print-renderer/v1/render");
        } catch (Exception e) { throw new PrintApiException(503, "PRINT_RENDERER_NOT_CONFIGURED", "可信渲染地址必须指向内部服务，跨主机调用须使用HTTPS"); }
    }
    private void validateArtifact(JsonNode artifact, String face, JsonNode page, int pageCount) {
        try {
            double width = page.path("widthMm").asDouble(), height = page.path("heightMm").asDouble();
            if ((face != null && !face.equals(artifact.path("face").asText())) || !"application/pdf".equals(artifact.path("mediaType").asText()) || artifact.path("pageCount").asInt() != pageCount || (face != null && (Math.abs(artifact.path("widthMm").asDouble() - width) > 0.05 || Math.abs(artifact.path("heightMm").asDouble() - height) > 0.05)) || artifact.path("contentBase64").asText().length() > 44739244) invalid();
            byte[] bytes = Base64.getDecoder().decode(artifact.path("contentBase64").asText());
            if (bytes.length < 5 || bytes.length > MAX_PDF || !artifact.path("bytes").isIntegralNumber() || artifact.path("bytes").asLong() != bytes.length || !PrintJson.hashBytes(bytes).equals(artifact.path("sha256").asText())) invalid();
            try (PDDocument pdf = PDDocument.load(new ByteArrayInputStream(bytes), MemoryUsageSetting.setupMainMemoryOnly(64L * 1024 * 1024))) {
                if (pdf.isEncrypted() || pdf.getNumberOfPages() != pageCount) invalid();
                for (int i = 0; i < pageCount; i++) {
                    if (pdf.getPage(i).getRotation() != 0) invalid();
                    PDRectangle actual = pdf.getPage(i).getMediaBox(), crop = pdf.getPage(i).getCropBox();
                    if (Math.abs(actual.getWidth() * 25.4 / 72 - width) > 0.05 || Math.abs(actual.getHeight() * 25.4 / 72 - height) > 0.05 || Math.abs(actual.getWidth() - crop.getWidth()) > 0.01 || Math.abs(actual.getHeight() - crop.getHeight()) > 0.01) invalid();
                }
            }
        } catch (PrintApiException e) { throw e; }
        catch (Exception e) { invalid(); }
    }
    private static byte[] readLimited(InputStream input, int max) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] chunk = new byte[8192]; int read;
        while ((read = input.read(chunk)) != -1) { if (out.size() + read > max) throw new PrintApiException(422, "PAYLOAD_LIMIT_EXCEEDED", "渲染响应超出限制"); out.write(chunk, 0, read); } return out.toByteArray();
    }
    private static void invalid() { throw new PrintApiException(422, "RENDER_VALIDATION_FAILED", "渲染制品校验失败"); }
}
