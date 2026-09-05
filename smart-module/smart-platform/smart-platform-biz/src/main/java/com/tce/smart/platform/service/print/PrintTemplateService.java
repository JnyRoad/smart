package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.tce.smart.platform.api.dto.req.print.*;
import com.tce.smart.platform.core.entity.print.*;
import com.tce.smart.platform.core.mapper.PrintTemplateMapper;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

/** 模板修改、发布指针、组合与幂等结果由同一数据库事务提交。 */
@Service
public class PrintTemplateService {
    private final PrintTemplateMapper mapper;
    private final PrintAccessPolicy access;
    private final PrintTemplateValidator validator;
    private final PrintPublicationValidator renderer;
    private final TransactionTemplate transactions;
    public PrintTemplateService(PrintTemplateMapper mapper, PrintAccessPolicy access, PrintTemplateValidator validator, PrintPublicationValidator renderer, PlatformTransactionManager transactionManager) {
        this.mapper = mapper; this.access = access; this.validator = validator; this.renderer = renderer; this.transactions = new TransactionTemplate(transactionManager);
        this.transactions.setTimeout(90);
    }
    public Map<String, Object> create(String requestedPark, PrintTemplateRequest request) {
        String park = park(requestedPark, request.getParkId()); access.require("write", park); validator.validate(park, request);
        if (request.getTemplateKey() != null) PrintTemplateValidator.key(request.getTemplateKey());
        try {
            return transactions.execute(status -> {
                Timestamp now = now(); String actor = access.actor(); PrintTemplate template = new PrintTemplate();
                template.setTemplateId(id()); template.setParkId(park); template.setTemplateKey(request.getTemplateKey() == null ? template.getTemplateId() : request.getTemplateKey());
                template.setName(request.getName()); template.setPrintItemType(request.getPrintItemType()); template.setPersonType(request.getPersonType()); template.setClassificationCode(request.getClassificationCode()); template.setFaceRole(request.getFaceRole()); template.setLifecycleStatus("ACTIVE"); template.setDraftRevision(0L);
                template.setCreatedBy(actor); template.setUpdatedBy(actor); template.setCreatedAt(now); template.setUpdatedAt(now);
                PrintTemplateVersion draft = new PrintTemplateVersion(); draft.setTemplateVersionId(id()); draft.setTemplateId(template.getTemplateId()); draft.setParkId(park); draft.setVersionNo(0L); draft.setVersionStatus("DRAFT"); draft.setFaceRole(template.getFaceRole()); draft.setSideCount(1); draft.setCreatedBy(actor); draft.setCreatedAt(now); applyContent(draft, request, 0L);
                template.setCurrentDraftVersionId(draft.getTemplateVersionId()); mapper.insertTemplate(template); mapper.insertTemplateVersion(draft); audit(park, "TEMPLATE_CREATED", template.getTemplateId(), Collections.singletonMap("draftRevision", 0)); return templateView(template, true);
            });
        } catch (DuplicateKeyException duplicate) { throw new PrintApiException(409, "TEMPLATE_KEY_CONFLICT", "本园区已存在相同模板键"); }
    }
    public Map<String, Object> save(String templateId, PrintTemplateRequest request) {
        access.user(); PrintAccessPolicy.uuid(templateId);
        return transactions.execute(status -> {
            PrintTemplate template = template(templateId, true, "write"); active(template); revision(request.getDraftRevision(), template.getDraftRevision(), "DRAFT_REVISION_CONFLICT", "currentDraftRevision");
            if (request.getParkId() != null && !template.getParkId().equals(request.getParkId())) throw scope();
            fixed(request.getTemplateKey(), template.getTemplateKey()); fixed(request.getFaceRole(), template.getFaceRole()); fixed(request.getPrintItemType(), template.getPrintItemType()); fixed(request.getPersonType(), template.getPersonType()); fixed(request.getClassificationCode(), template.getClassificationCode());
            PrintTemplateRequest merged = requestFor(template, request); validator.validate(template.getParkId(), merged);
            PrintTemplateVersion draft = version(template.getCurrentDraftVersionId());
            if (!"DRAFT".equals(draft.getVersionStatus())) throw new PrintApiException(409, "DRAFT_REVISION_CONFLICT", "当前草稿不可修改");
            long next = template.getDraftRevision() + 1; applyContent(draft, merged, next);
            if (mapper.updateTemplateVersion(draft) != 1) throw new PrintApiException(409, "DRAFT_REVISION_CONFLICT", "当前草稿不可修改");
            template.setName(merged.getName()); template.setDraftRevision(next); touch(template); mapper.updateTemplate(template);
            audit(template.getParkId(), "DRAFT_SAVED", templateId, Collections.singletonMap("draftRevision", next)); return templateView(template, true);
        });
    }
    public Map<String, Object> detail(String templateId) { return templateView(template(templateId, false, "read"), true); }
    public List<Map<String, Object>> versions(String templateId) {
        PrintTemplate template = template(templateId, false, "read"); List<Map<String, Object>> result = new ArrayList<>();
        for (PrintTemplateVersion version : mapper.findVersions(template.getTemplateId())) result.add(versionView(version)); return result;
    }
    public Map<String, Object> list(PrintListQuery query) {
        query.setParkId(access.resolvePark(query.getParkId())); access.require("read", query.getParkId()); RowBounds bounds = bounds(query);
        List<Map<String, Object>> records = new ArrayList<>(); for (PrintTemplate template : mapper.listTemplates(query, bounds)) records.add(templateView(template, false));
        return page(records, mapper.countTemplates(query), query);
    }
    public PrintMutationResult publish(String templateId, PrintPublishRequest request, String key) {
        PrintTemplate initial = template(templateId, false, "publish");
        return command(key, "publish:" + templateId, request, () -> {
            PrintTemplate template = template(templateId, true, "publish"); active(template); revision(request.getDraftRevision(), template.getDraftRevision(), "DRAFT_REVISION_CONFLICT", "currentDraftRevision");
            if (!Objects.equals(request.getDraftVersionId(), template.getCurrentDraftVersionId())) throw new PrintApiException(409, "DRAFT_REVISION_CONFLICT", "草稿指针已变化", Collections.singletonMap("currentDraftRevision", template.getDraftRevision()));
            PrintTemplateVersion draft = version(request.getDraftVersionId());
            if (!"DRAFT".equals(draft.getVersionStatus()) || !templateId.equals(draft.getTemplateId())) throw new PrintApiException(422, "TEMPLATE_VALIDATION_FAILED", "发布目标必须是当前草稿");
            validator.validate(template.getParkId(), requestFor(template, draft));
            Map<String, Object> report = renderer.validate(template, draft);
            if (report == null || !"READY".equals(report.get("status"))) throw new PrintApiException(422, "RENDER_VALIDATION_FAILED", "可信渲染校验未通过");
            PrintTemplateVersion published = new PrintTemplateVersion(); BeanUtils.copyProperties(draft, published); published.setTemplateVersionId(id()); published.setVersionNo(mapper.nextVersionNo(templateId)); published.setVersionStatus("PUBLISHED"); published.setPublishedBy(access.actor()); published.setPublishedAt(now()); published.setCreatedBy(access.actor()); published.setCreatedAt(published.getPublishedAt()); published.setValidationReportJson(PrintJson.canonical(report));
            mapper.insertTemplateVersion(published); template.setCurrentPublishedVersionId(published.getTemplateVersionId()); touch(template); mapper.updateTemplate(template);
            audit(initial.getParkId(), "TEMPLATE_PUBLISHED", templateId, Collections.singletonMap("templateVersionId", published.getTemplateVersionId())); return versionView(published);
        });
    }
    public PrintMutationResult rollback(String templateId, PrintRollbackRequest request, String key) {
        template(templateId, false, "publish");
        return command(key, "rollback:" + templateId, request, () -> {
            PrintTemplate template = template(templateId, true, "publish"); active(template);
            if (request.getExpectedPublishedVersionId() == null || !request.getExpectedPublishedVersionId().equals(template.getCurrentPublishedVersionId())) throw new PrintApiException(409, "PUBLISHED_POINTER_CONFLICT", "当前发布版本已变化", Collections.singletonMap("currentPublishedVersionId", template.getCurrentPublishedVersionId()));
            if (request.getReason() == null || request.getReason().trim().isEmpty() || request.getReason().length() > 1000) PrintTemplateValidator.invalid(template.getFaceRole(), "reason", "ROLLBACK_REASON_REQUIRED");
            PrintTemplateVersion target = version(request.getTargetVersionId()); access.require("publish", target.getParkId());
            if (!templateId.equals(target.getTemplateId()) || !"PUBLISHED".equals(target.getVersionStatus())) PrintTemplateValidator.invalid(template.getFaceRole(), "targetVersionId", "PUBLISHED_VERSION_REQUIRED");
            validator.validate(template.getParkId(), requestFor(template, target)); template.setCurrentPublishedVersionId(target.getTemplateVersionId()); touch(template); mapper.updateTemplate(template);
            Map<String, Object> details = new LinkedHashMap<>(); details.put("targetVersionId", target.getTemplateVersionId()); details.put("reason", request.getReason()); audit(template.getParkId(), "TEMPLATE_ROLLED_BACK", templateId, details); return versionView(target);
        });
    }
    public PrintMutationResult createPair(String requestedPark, PrintPairRequest request, String key) {
        access.user(); PrintTemplateVersion front = version(request.getFrontTemplateVersionId());
        String park = park(requestedPark == null ? front.getParkId() : requestedPark, request.getParkId()); access.require("publish", park);
        return command(key, "pair-create:" + park, request, () -> {
            validatePair(park, request); PrintTemplatePair pair = new PrintTemplatePair(); BeanUtils.copyProperties(request, pair); pair.setPairId(id()); pair.setParkId(park); pair.setRevision(0L); pair.setStatus("ACTIVE"); pair.setCreatedAt(now()); pair.setUpdatedAt(pair.getCreatedAt()); pair.setCreatedBy(access.actor()); pair.setUpdatedBy(pair.getCreatedBy()); mapper.insertTemplatePair(pair);
            audit(park, "PAIR_CREATED", pair.getPairId(), pairAudit(pair)); return pairView(pair);
        });
    }
    public PrintMutationResult savePair(String pairId, PrintPairRequest request, String key) {
        pair(pairId, false, "publish");
        return command(key, "pair-save:" + pairId, request, () -> {
            PrintTemplatePair pair = pair(pairId, true, "publish");
            if (!"ACTIVE".equals(pair.getStatus())) throw new PrintApiException(409, "PAIR_ARCHIVED", "组合已归档");
            revision(request.getRevision(), pair.getRevision(), "PAIR_REVISION_CONFLICT", "currentRevision");
            if (request.getParkId() != null && !pair.getParkId().equals(request.getParkId())) throw scope();
            fixed(request.getPrintItemType(), pair.getPrintItemType()); fixed(request.getPersonType(), pair.getPersonType()); fixed(request.getClassificationCode(), pair.getClassificationCode());
            PrintPairRequest merged = new PrintPairRequest(); BeanUtils.copyProperties(request, merged); merged.setPrintItemType(pair.getPrintItemType()); merged.setPersonType(pair.getPersonType()); merged.setClassificationCode(pair.getClassificationCode()); validatePair(pair.getParkId(), merged);
            pair.setName(merged.getName()); pair.setFrontTemplateVersionId(merged.getFrontTemplateVersionId()); pair.setBackTemplateVersionId(merged.getBackTemplateVersionId()); pair.setRevision(pair.getRevision() + 1); pair.setUpdatedBy(access.actor()); pair.setUpdatedAt(now()); mapper.updateTemplatePair(pair); audit(pair.getParkId(), "PAIR_REBOUND", pairId, pairAudit(pair)); return pairView(pair);
        });
    }
    public PrintMutationResult archivePair(String pairId, Long expectedRevision, String key) {
        pair(pairId, false, "publish");
        return command(key, "pair-archive:" + pairId, Collections.singletonMap("revision", expectedRevision), () -> {
            PrintTemplatePair pair = pair(pairId, true, "publish"); revision(expectedRevision, pair.getRevision(), "PAIR_REVISION_CONFLICT", "currentRevision");
            if (!"ACTIVE".equals(pair.getStatus())) throw new PrintApiException(409, "PAIR_ARCHIVED", "组合已归档");
            pair.setStatus("ARCHIVED"); pair.setRevision(pair.getRevision() + 1); pair.setArchivedAt(now()); pair.setUpdatedAt(pair.getArchivedAt()); pair.setUpdatedBy(access.actor()); mapper.updateTemplatePair(pair); audit(pair.getParkId(), "PAIR_ARCHIVED", pairId, pairAudit(pair)); return pairView(pair);
        });
    }
    public Map<String, Object> pairDetail(String pairId) { return pairView(pair(pairId, false, "read")); }
    public Map<String, Object> listPairs(PrintListQuery query) {
        query.setParkId(access.resolvePark(query.getParkId())); access.require("read", query.getParkId()); RowBounds bounds = bounds(query); List<Map<String, Object>> records = new ArrayList<>(); for (PrintTemplatePair pair : mapper.listPairs(query, bounds)) records.add(pairView(pair)); return page(records, mapper.countPairs(query), query);
    }
    public Map<String, Object> resource(String versionId, String objectId) {
        access.user(); PrintTemplateVersion version = version(versionId); access.require("resource", version.getParkId()); template(version.getTemplateId(), false, "read");
        JsonNode manifest = PrintJson.read(version.getResourceManifestJson()); access.validateManifest(version.getParkId(), manifest);
        for (JsonNode reference : manifest) if (objectId.equals(reference.path("objectId").asText())) {
            Map<String, Object> result = new HashMap<>(); result.put("bytes", access.readResource(version.getParkId(), objectId, reference.path("contentHash").asText())); result.put("mediaType", reference.path("mediaType").asText()); result.put("hash", reference.path("contentHash").asText()); return result;
        }
        throw new PrintApiException(403, "PRINT_SCOPE_DENIED", "资源不属于该模板版本");
    }
    private void validatePair(String park, PrintPairRequest request) {
        PrintTemplateValidator.name(request.getName()); PrintTemplateValidator.key(request.getClassificationCode());
        if (!"STAFF_CARD".equals(request.getPrintItemType())) PrintTemplateValidator.invalid(null, "printItemType", "STAFF_PAIR_REQUIRED");
        PrintTemplateVersion front = version(request.getFrontTemplateVersionId()), back = version(request.getBackTemplateVersionId());
        validatePairFace(park, request, front, "FRONT"); validatePairFace(park, request, back, "BACK");
        JsonNode a = PrintJson.read(front.getPageSpecJson()), b = PrintJson.read(back.getPageSpecJson());
        if (Math.abs(a.path("widthMm").asDouble() - b.path("widthMm").asDouble()) > 0.000001 || Math.abs(a.path("heightMm").asDouble() - b.path("heightMm").asDouble()) > 0.000001) PrintTemplateValidator.invalid(null, "pageSpecJson", "PAIR_DIMENSIONS_MISMATCH");
        for (String field : Arrays.asList("orientation", "mediaType", "mediaSpec")) if (!a.path(field).equals(b.path(field))) PrintTemplateValidator.invalid(null, field, "PAIR_MEDIA_MISMATCH");
    }
    private void validatePairFace(String park, PrintPairRequest request, PrintTemplateVersion version, String role) {
        access.require("publish", version.getParkId()); if (!park.equals(version.getParkId())) throw scope();
        PrintTemplate template = template(version.getTemplateId(), false, "publish"); active(template);
        if (!"PUBLISHED".equals(version.getVersionStatus()) || !role.equals(version.getFaceRole()) || !Integer.valueOf(1).equals(version.getSideCount()) || !Objects.equals(request.getPrintItemType(), template.getPrintItemType()) || !Objects.equals(request.getPersonType(), template.getPersonType()) || !Objects.equals(request.getClassificationCode(), template.getClassificationCode())) PrintTemplateValidator.invalid(role, "templateVersionId", "PAIR_VERSION_MISMATCH");
        validator.validate(park, requestFor(template, version));
    }
    private PrintMutationResult command(String key, String action, Object body, Supplier<Map<String, Object>> operation) {
        if (key == null || !key.matches("[\\x20-\\x7e]{1,128}")) throw new PrintApiException(422, "IDEMPOTENCY_KEY_REQUIRED", "需要有效的 Idempotency-Key");
        String actor = access.actor(); Map<String, Object> envelope = new LinkedHashMap<>(); envelope.put("action", action); envelope.put("body", body); String hash = PrintJson.hash(envelope);
        PrintOperation existing = mapper.findOperation(actor, key); if (existing != null) return replay(existing, hash);
        try {
            return transactions.execute(status -> {
                PrintOperation record = new PrintOperation(); record.setOperationId(id()); record.setPrincipalId(actor); record.setIdempotencyKey(key); record.setBodyHash(hash); record.setCreatedAt(now()); mapper.insertOperation(record);
                Map<String, Object> data = operation.get(); record.setResponseJson(PrintJson.canonical(data)); mapper.completeOperation(record); return new PrintMutationResult(data, false);
            });
        } catch (DuplicateKeyException collision) {
            // 唯一约束等待并发事务提交；失败事务回滚后再读取首次完整响应。
            existing = mapper.findOperation(actor, key); if (existing != null) return replay(existing, hash); throw new PrintApiException(409, "PRINT_CONCURRENT_MODIFICATION", "对象被并发修改");
        }
    }
    private PrintMutationResult replay(PrintOperation record, String hash) {
        if (!hash.equals(record.getBodyHash())) throw new PrintApiException(409, "IDEMPOTENCY_KEY_REUSED", "幂等键已用于不同请求");
        if (record.getResponseJson() == null) throw new PrintApiException(409, "PRINT_OPERATION_IN_PROGRESS", "请求正在处理");
        return new PrintMutationResult(PrintJson.map(PrintJson.read(record.getResponseJson())), true);
    }
    private PrintTemplate template(String id, boolean lock, String permission) {
        access.user(); PrintAccessPolicy.uuid(id); PrintTemplate value = lock ? mapper.lockTemplate(id) : mapper.findTemplate(id);
        if (value == null) throw new PrintApiException(404, "TEMPLATE_NOT_FOUND", "模板不存在"); access.require(permission, value.getParkId()); return value;
    }
    private PrintTemplateVersion version(String id) {
        PrintAccessPolicy.uuid(id); PrintTemplateVersion value = mapper.findTemplateVersion(id);
        if (value == null) throw new PrintApiException(404, "TEMPLATE_VERSION_NOT_FOUND", "模板版本不存在"); access.resolvePark(value.getParkId()); PrintTemplateValidator.integrity(value); return value;
    }
    private PrintTemplatePair pair(String id, boolean lock, String permission) {
        access.user(); PrintAccessPolicy.uuid(id); PrintTemplatePair value = lock ? mapper.lockTemplatePair(id) : mapper.findTemplatePair(id);
        if (value == null) throw new PrintApiException(404, "TEMPLATE_PAIR_NOT_FOUND", "组合不存在"); access.require(permission, value.getParkId()); return value;
    }
    private Map<String, Object> templateView(PrintTemplate template, boolean content) {
        Map<String, Object> data = PrintJson.map(template); data.put("sideCount", 1); data.put("status", template.getLifecycleStatus()); timestamps(data);
        if (content) {
            data.put("draft", template.getCurrentDraftVersionId() == null ? null : versionView(version(template.getCurrentDraftVersionId())));
            List<Map<String, Object>> published = new ArrayList<>(); for (PrintTemplateVersion version : mapper.findVersions(template.getTemplateId())) if ("PUBLISHED".equals(version.getVersionStatus())) published.add(versionView(version)); data.put("versions", published);
        }
        return data;
    }
    private Map<String, Object> versionView(PrintTemplateVersion version) {
        PrintTemplateValidator.integrity(version); access.resolvePark(version.getParkId()); access.validateManifest(version.getParkId(), PrintJson.read(version.getResourceManifestJson()));
        Map<String, Object> data = PrintJson.map(version); data.put("layoutJson", PrintJson.read(version.getLayoutJson())); data.put("fieldSchemaJson", PrintJson.read(version.getFieldSchemaJson())); data.put("pageSpecJson", PrintJson.read(version.getPageSpecJson())); data.put("resourceManifest", PrintJson.read(version.getResourceManifestJson())); data.remove("resourceManifestJson"); data.put("validationReport", PrintJson.read(version.getValidationReportJson())); data.remove("validationReportJson"); timestamps(data); return data;
    }
    private Map<String, Object> pairView(PrintTemplatePair pair) {
        Map<String, Object> data = PrintJson.map(pair); data.put("frontVersionNo", version(pair.getFrontTemplateVersionId()).getVersionNo()); data.put("backVersionNo", version(pair.getBackTemplateVersionId()).getVersionNo()); timestamps(data); return data;
    }
    private void applyContent(PrintTemplateVersion version, PrintTemplateRequest request, long revision) {
        version.setLayoutJson(PrintJson.canonical(request.getLayoutJson())); version.setFieldSchemaJson(PrintJson.canonical(request.getFieldSchemaJson())); version.setPageSpecJson(PrintJson.canonical(request.getPageSpecJson())); version.setResourceManifestJson(PrintJson.canonical(request.getResourceManifest())); version.setValidationReportJson("{\"status\":\"DRAFT\"}"); version.setDraftRevision(revision); version.setContentHash(PrintTemplateValidator.contentHash(version));
    }
    private PrintTemplateRequest requestFor(PrintTemplate template, PrintTemplateRequest request) {
        PrintTemplateRequest result = new PrintTemplateRequest(); BeanUtils.copyProperties(request, result); result.setPrintItemType(template.getPrintItemType()); result.setPersonType(template.getPersonType()); result.setClassificationCode(template.getClassificationCode()); result.setFaceRole(template.getFaceRole()); return result;
    }
    private PrintTemplateRequest requestFor(PrintTemplate template, PrintTemplateVersion version) {
        PrintTemplateRequest request = new PrintTemplateRequest(); request.setName(template.getName()); request.setPrintItemType(template.getPrintItemType()); request.setPersonType(template.getPersonType()); request.setClassificationCode(template.getClassificationCode()); request.setFaceRole(version.getFaceRole()); request.setSideCount(version.getSideCount()); request.setLayoutJson(PrintJson.read(version.getLayoutJson())); request.setFieldSchemaJson(PrintJson.read(version.getFieldSchemaJson())); request.setPageSpecJson(PrintJson.read(version.getPageSpecJson())); request.setResourceManifest(PrintJson.read(version.getResourceManifestJson())); return request;
    }
    private String park(String query, String body) { if (query != null && body != null && !query.equals(body)) throw scope(); return access.resolvePark(query != null ? query : body); }
    private void touch(PrintTemplate template) { template.setUpdatedAt(now()); template.setUpdatedBy(access.actor()); }
    private void active(PrintTemplate template) { if (!"ACTIVE".equals(template.getLifecycleStatus())) throw new PrintApiException(409, "TEMPLATE_ARCHIVED", "模板已归档"); }
    private static void fixed(String supplied, String stored) { if (supplied != null && !supplied.equals(stored)) PrintTemplateValidator.invalid(null, "immutable", "IMMUTABLE_TEMPLATE_PROPERTY"); }
    private static void revision(Long expected, Long current, String code, String field) { if (expected == null || !expected.equals(current)) throw new PrintApiException(409, code, "修订已变化，请重新加载", Collections.singletonMap(field, current)); }
    private void audit(String park, String action, String objectId, Map<String, Object> details) {
        Map<String,Object> tracedDetails=new LinkedHashMap<>(details);
        org.springframework.web.context.request.RequestAttributes attributes=org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        Object requestId=attributes==null ? null : attributes.getAttribute("print.requestId",org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST);
        // HTTP入口使用过滤器生成的同一追踪ID；离线调用生成独立ID，不采信外部请求头。
        tracedDetails.put("requestId",requestId instanceof String ? requestId : id());
        PrintAudit audit = new PrintAudit(); audit.setAuditId(id()); audit.setParkId(park); audit.setActorId(access.actor()); audit.setAction(action); audit.setObjectId(objectId); audit.setDetailsJson(PrintJson.canonical(tracedDetails)); audit.setCreatedAt(now()); mapper.insertAudit(audit);
    }
    private Map<String, Object> pairAudit(PrintTemplatePair pair) { Map<String, Object> data = new LinkedHashMap<>(); data.put("revision", pair.getRevision()); data.put("frontTemplateVersionId", pair.getFrontTemplateVersionId()); data.put("backTemplateVersionId", pair.getBackTemplateVersionId()); return data; }
    private static Timestamp now() { return Timestamp.from(Instant.now()); }
    private static String id() { return UUID.randomUUID().toString(); }
    private static PrintApiException scope() { return new PrintApiException(403, "PRINT_SCOPE_DENIED", "对象不属于同一授权园区"); }
    private static RowBounds bounds(PrintListQuery query) {
        if (query.getCurrent() == null || query.getCurrent() < 1 || query.getCurrent() > 10000 || query.getSize() == null || query.getSize() < 1 || query.getSize() > 100) throw new PrintApiException(422, "PAYLOAD_LIMIT_EXCEEDED", "分页范围无效");
        return new RowBounds((query.getCurrent() - 1) * query.getSize(), query.getSize());
    }
    private static Map<String, Object> page(List<Map<String, Object>> records, long total, PrintListQuery query) { Map<String, Object> page = new LinkedHashMap<>(); page.put("records", records); page.put("total", total); page.put("current", query.getCurrent()); page.put("size", query.getSize()); return page; }
    private static void timestamps(Map<String, Object> data) { for (String key : Arrays.asList("createdAt", "updatedAt", "publishedAt", "archivedAt")) if (data.get(key) instanceof Number) data.put(key, Instant.ofEpochMilli(((Number) data.get(key)).longValue()).toString()); }
}
