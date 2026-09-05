package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.tce.smart.platform.api.dto.req.print.*;
import com.tce.smart.platform.core.entity.print.*;
import com.tce.smart.platform.core.mapper.*;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

/** 绑定持久化与自动/手选解析；手选只冻结本次选择，不修改长期关联。 */
@Service
public class PrintTemplateResolver {
    private final PrintBindingMapper bindings;
    private final PrintTemplateMapper templates;
    private final PrintAccessPolicy access;
    private final PrintTemplateValidator validator;
    private final PrintResolutionProperties config;
    private final TransactionTemplate transactions;
    public PrintTemplateResolver(PrintBindingMapper bindings,PrintTemplateMapper templates,PrintAccessPolicy access,PrintTemplateValidator validator,PrintResolutionProperties config,PlatformTransactionManager manager) {
        this.bindings=bindings;this.templates=templates;this.access=access;this.validator=validator;this.config=config;transactions=new TransactionTemplate(manager);transactions.setTimeout(90);
    }
    public ObjectNode employeeGrades(String requestedPark) {
        String park=access.resolvePark(requestedPark);access.require("publish",park);ObjectNode result=PrintJson.object().put("confirmed",true).put("source","DHR");
        ArrayNode records=result.putArray("records");config.confirmedGrades(park).forEach((code,name)->records.addObject().put("code",code).put("name",name).put("source","DHR"));result.putArray("sourceFields").add("jcheId").add("jcheName");return result;
    }
    public Map<String,Object> list(PrintBindingQuery query) {
        query.setParkId(access.resolvePark(query.getParkId()));access.require("publish",query.getParkId());
        if(query.getCurrent()==null||query.getSize()==null||query.getCurrent()<1||query.getCurrent()>10000||query.getSize()<1||query.getSize()>100)throw error(422,"PAYLOAD_LIMIT_EXCEEDED","分页范围无效");
        List<Map<String,Object>> rows=new ArrayList<>();for(PrintBindingRule rule:bindings.list(query,new RowBounds((query.getCurrent()-1)*query.getSize(),query.getSize())))rows.add(view(rule));
        Map<String,Object> result=new LinkedHashMap<>();result.put("records",rows);result.put("total",bindings.count(query));result.put("current",query.getCurrent());result.put("size",query.getSize());return result;
    }
    public Map<String,Object> detail(String id){return view(binding(id));}
    public PrintMutationResult create(String requestedPark,PrintBindingRequest request,String key) {
        String park=park(requestedPark,request.getParkId());access.require("publish",park);
        return command(key,"binding:create:"+park,request,()->{
            lock(park);PrintBindingRule rule=new PrintBindingRule();BeanUtils.copyProperties(request,rule);rule.setBindingRuleId(id());rule.setParkId(park);rule.setStatus("ACTIVE");rule.setRevision(0L);rule.setCreatedAt(now());rule.setCreatedBy(access.actor());apply(rule,request);validateRule(rule);conflicts(rule);bindings.insert(rule);audit(rule,"BINDING_CREATED");return view(rule);
        });
    }
    public PrintMutationResult save(String id,PrintBindingRequest request,String key) {
        PrintBindingRule initial=binding(id);return command(key,"binding:save:"+id,request,()->{
            lock(initial.getParkId());PrintBindingRule rule=binding(id);revision(request.getRevision(),rule.getRevision(),"BINDING_REVISION_CONFLICT");if(request.getParkId()!=null&&!request.getParkId().equals(rule.getParkId()))throw scope();
            // PATCH 省略的属性保持原值；validTo=null 明确取消结束时间。
            ObjectNode merged=(ObjectNode)PrintJson.tree(view(rule));ObjectNode incoming=(ObjectNode)PrintJson.tree(request);incoming.fields().forEachRemaining(e->{if(!e.getValue().isNull())merged.set(e.getKey(),e.getValue());});
            if(request.getPairId()!=null&&request.getTemplateId()!=null)throw invalid();
            rule.setPrintItemType(merged.path("printItemType").asText());rule.setPersonType(merged.path("personType").asText());rule.setClassificationCode(merged.path("classificationCode").asText());rule.setScopeType(merged.path("scopeType").asText());rule.setScopeId("EXPLICIT_DEFAULT".equals(rule.getScopeType())?null:merged.path("scopeId").asText(null));
            if(("STAFF_CARD".equals(rule.getPrintItemType())&&request.getTemplateId()!=null)||("VISITOR_SLIP".equals(rule.getPrintItemType())&&request.getPairId()!=null))throw invalid();
            if(!employee(rule.getPrintItemType(),rule.getPersonType())&&request.getEmployeeGradeCodes()!=null)throw invalid();
            rule.setPairId("STAFF_CARD".equals(rule.getPrintItemType())?merged.path("pairId").asText(null):null);rule.setTemplateId("VISITOR_SLIP".equals(rule.getPrintItemType())?merged.path("templateId").asText(null):null);
            rule.setPriority(merged.path("priority").asInt(100));if(request.getValidFrom()!=null)rule.setValidFrom(time(request.getValidFrom(),true));if(request.isValidToSpecified())rule.setValidTo(time(request.getValidTo(),false));
            if(request.getEmployeeGradeCodes()!=null)rule.setEmployeeGradeCodesClob(PrintJson.canonical(request.getEmployeeGradeCodes()));if(!employee(rule.getPrintItemType(),rule.getPersonType()))rule.setEmployeeGradeCodesClob(null);
            rule.setRevision(rule.getRevision()+1);rule.setUpdatedBy(access.actor());rule.setUpdatedAt(now());validateRule(rule);conflicts(rule);if(bindings.update(rule)!=1)throw error(409,"BINDING_REVISION_CONFLICT","规则修订已变化");audit(rule,"BINDING_UPDATED");return view(rule);
        });
    }
    public PrintMutationResult disable(String id,Long expected,String key) {
        PrintBindingRule initial=binding(id);return command(key,"binding:disable:"+id,Collections.singletonMap("revision",expected),()->{lock(initial.getParkId());PrintBindingRule rule=binding(id);revision(expected,rule.getRevision(),"BINDING_REVISION_CONFLICT");rule.setStatus("DISABLED");rule.setRevision(rule.getRevision()+1);rule.setUpdatedAt(now());rule.setUpdatedBy(access.actor());if(bindings.update(rule)!=1)throw error(409,"BINDING_REVISION_CONFLICT","规则修订已变化");audit(rule,"BINDING_DISABLED");return view(rule);});
    }
    /** 输入必须来自可信人员源；权限、资料与分类错误不降级为推荐提示。 */
    public ObjectNode resolve(String requestedPark,ObjectNode subject) {
        String park=validateSubject(requestedPark,subject);return resolveValidated(park,subject);
    }
    private ObjectNode resolveValidated(String park,ObjectNode subject) {
        List<PrintBindingRule> exact=new ArrayList<>(),defaults=new ArrayList<>();Instant now=Instant.now();
        for(PrintBindingRule rule:bindings.candidates(park,text(subject,"printItemType"),text(subject,"personType"),text(subject,"classificationCode"))) {
            if(rule.getValidFrom()==null||rule.getValidFrom().toInstant().isAfter(now)||(rule.getValidTo()!=null&&!now.isBefore(rule.getValidTo().toInstant())))continue;
            if(employee(rule.getPrintItemType(),rule.getPersonType())&&!grades(rule).contains(text(subject,"employeeGradeCode")))continue;
            if("EXPLICIT_DEFAULT".equals(rule.getScopeType()))defaults.add(rule);
            else if(("COMPANY".equals(rule.getScopeType())&&Objects.equals(rule.getScopeId(),text(subject,"companyId")))||("SUPPLIER".equals(rule.getScopeType())&&Objects.equals(rule.getScopeId(),text(subject,"supplierId"))))exact.add(rule);
        }
        List<PrintBindingRule> candidates=exact.isEmpty()?defaults:exact;
        if(candidates.isEmpty())throw error(404,employee(text(subject,"printItemType"),text(subject,"personType"))?"EMPLOYEE_GRADE_TEMPLATE_NOT_FOUND":"TEMPLATE_NOT_FOUND","未找到适用模板绑定");
        int priority=candidates.stream().mapToInt(r->r.getPriority()==null?100:r.getPriority()).max().getAsInt();List<PrintBindingRule> winners=new ArrayList<>();for(PrintBindingRule r:candidates)if((r.getPriority()==null?100:r.getPriority())==priority)winners.add(r);
        if(winners.size()!=1)throw error(409,"BINDING_AMBIGUOUS","存在同优先级适用规则，请消除歧义或手动核对选择");
        PrintBindingRule rule=winners.get(0);ObjectNode selection=PrintJson.object().put("kind","BOUND");if(rule.getPairId()!=null)selection.put("pairId",rule.getPairId());else selection.put("templateId",rule.getTemplateId());
        ObjectNode result=target(park,subject,selection,"execute",true);result.put("bindingRuleId",rule.getBindingRuleId()).put("bindingRevision",rule.getRevision()).put("matchLevel",exact.isEmpty()?"EXPLICIT_DEFAULT":"COMPANY".equals(rule.getScopeType())?"SPECIFIC_COMPANY":"SPECIFIC_SUPPLIER");result.set("bindingSnapshot",PrintJson.tree(view(rule)));copyGrade(subject,result);return result;
    }
    public ObjectNode select(String requestedPark,ObjectNode subject,JsonNode selection) {
        String park=validateSubject(requestedPark,subject);if(selection==null||!selection.isObject())throw invalid();String kind=text(selection,"kind");
        if("BOUND".equals(kind))return resolveValidated(park,subject);
        if(!Arrays.asList("PAIR","EXPLICIT").contains(kind))throw invalid();
        if(!selection.path("manualSelectionConfirmed").isBoolean()||!selection.path("manualSelectionConfirmed").asBoolean())throw error(422,"MANUAL_SELECTION_CONFIRMATION_REQUIRED","手动模板选择必须核对确认");
        ObjectNode result=target(park,subject,(ObjectNode)selection,"execute",true);ObjectNode automatic=PrintJson.object();
        try{ObjectNode recommended=resolveValidated(park,subject);automatic.put("status","MATCHED");automatic.set("recommendation",recommended);result.put("differsFromAutomatic",!Objects.equals(text(result,"frontTemplateVersionId"),text(recommended,"frontTemplateVersionId"))||!Objects.equals(text(result,"backTemplateVersionId"),text(recommended,"backTemplateVersionId")));}
        catch(PrintApiException e){if("BINDING_AMBIGUOUS".equals(e.getCode()))automatic.put("status","AMBIGUOUS");else if(Arrays.asList("TEMPLATE_NOT_FOUND","EMPLOYEE_GRADE_TEMPLATE_NOT_FOUND").contains(e.getCode()))automatic.put("status","NOT_FOUND");else throw e;automatic.put("code",e.getCode());}
        result.set("automaticResolution",automatic);result.putNull("bindingRuleId");result.put("manualSelectionConfirmed",true).put("confirmedBy",access.actor()).put("confirmedAt",Instant.now().toString());copyGrade(subject,result);return result;
    }
    private String validateSubject(String requestedPark,ObjectNode subject) {
        String park=access.resolvePark(requestedPark);access.require("execute",park);if(subject==null||!park.equals(text(subject,"parkId")))throw scope();
        String item=text(subject,"printItemType"),person=text(subject,"personType");if(!(("STAFF_CARD".equals(item)&&Arrays.asList("EMPLOYEE","OUTSOURCED","DISPATCHED","SUPPLIER").contains(person))||("VISITOR_SLIP".equals(item)&&"VISITOR".equals(person))))throw invalid();
        if(text(subject,"subjectId")==null||text(subject,"classificationCode")==null||!subject.path("fields").isObject()||!subject.path("resources").isArray())throw error(422,"PRINT_SUBJECT_INVALID","人员资料不完整");
        access.validateManifest(park,subject.path("resources"));
        if(employee(item,person)){String code=text(subject,"employeeGradeCode");if(code==null)throw error(422,"EMPLOYEE_GRADE_REQUIRED","人员缺少厂牌职级");Map<String,String> known=config.confirmedGrades(park);if(!"DHR".equals(text(subject,"employeeGradeSource"))||!known.containsKey(code)||!Objects.equals(known.get(code),text(subject,"employeeGradeName")))throw error(422,"EMPLOYEE_GRADE_UNMAPPED","人员职级与已确认 DHR 字典不一致");}
        return park;
    }
    private ObjectNode target(String park,JsonNode subject,ObjectNode input,String permission,boolean validateFields) {
        String kind=text(input,"kind"),item=text(subject,"printItemType");boolean staff="STAFF_CARD".equals(item);String pairId=text(input,"pairId"),front=text(input,"frontTemplateVersionId"),back=text(input,"backTemplateVersionId");PrintTemplatePair pair=null;
        if("PAIR".equals(kind)&&!staff)throw invalid();
        if(!staff&&(pairId!=null||back!=null))throw invalid();
        if((staff&&"BOUND".equals(kind))||"PAIR".equals(kind)||(staff&&pairId!=null)){
            PrintAccessPolicy.uuid(pairId);pair=templates.findTemplatePair(pairId);if(pair==null)throw error(404,"TEMPLATE_PAIR_NOT_FOUND","模板组合不存在");requirePark(park,pair.getParkId(),permission);if(!"ACTIVE".equals(pair.getStatus()))throw error(409,"TEMPLATE_PAIR_ARCHIVED","模板组合不可用");classification(subject,PrintJson.tree(pair));
            if(!"BOUND".equals(kind)){if(!input.path("pairRevision").isIntegralNumber())throw error(409,"PAIR_REVISION_CONFLICT","需要预览时确认的组合修订");revision(input.path("pairRevision").asLong(),pair.getRevision(),"PAIR_REVISION_CONFLICT");}
            if("EXPLICIT".equals(kind)){if(!Objects.equals(front,pair.getFrontTemplateVersionId())||!Objects.equals(back,pair.getBackTemplateVersionId()))throw error(409,"PAIR_REVISION_CONFLICT","显式版本与组合当前两面不一致");}else{front=pair.getFrontTemplateVersionId();back=pair.getBackTemplateVersionId();}
        }else if(!staff&&"BOUND".equals(kind)){
            PrintAccessPolicy.uuid(text(input,"templateId"));PrintTemplate template=templates.findTemplate(text(input,"templateId"));if(template==null)throw error(404,"BINDING_TARGET_NOT_FOUND","绑定目标不存在");requirePark(park,template.getParkId(),permission);classification(subject,PrintJson.tree(template));front=template.getCurrentPublishedVersionId();
        }
        if(staff&&back==null)throw invalid();PrintTemplateVersion f=face(park,subject,front,"FRONT",permission,validateFields);PrintTemplateVersion b=staff?face(park,subject,back,"BACK",permission,validateFields):null;
        if(!staff&&"BOUND".equals(kind)&&!Objects.equals(text(input,"templateId"),f.getTemplateId()))throw error(422,"TEMPLATE_VALIDATION_FAILED","发布指针必须属于绑定的访客模板");
        if(b!=null){JsonNode aPage=PrintJson.read(f.getPageSpecJson()),bPage=PrintJson.read(b.getPageSpecJson());for(String key:Arrays.asList("widthMm","heightMm","orientation","mediaType","mediaSpec"))if(!aPage.path(key).equals(bPage.path(key)))throw error(422,"TEMPLATE_VALIDATION_FAILED","模板两面的尺寸或介质不兼容");}
        ObjectNode selected=PrintJson.object().put("kind",kind).put("frontTemplateVersionId",front);ObjectNode result=PrintJson.object().put("frontTemplateVersionId",front).put("frontContentHash",f.getContentHash()).put("securityQrRequired","VISITOR_SECURITY".equals(text(subject,"classificationCode")));
        if(b!=null){selected.put("backTemplateVersionId",back);result.put("backTemplateVersionId",back).put("backContentHash",b.getContentHash());}if(pair!=null){selected.put("pairId",pairId);result.put("pairRevision",pair.getRevision());selected.put("pairRevision",pair.getRevision());}if(!staff){selected.put("templateId",f.getTemplateId());}result.set("selection",selected);return result;
    }
    private PrintTemplateVersion face(String park,JsonNode subject,String id,String role,String permission,boolean validateFields) {
        PrintAccessPolicy.uuid(id);PrintTemplateVersion version=templates.findTemplateVersion(id);if(version==null)throw error(404,"TEMPLATE_VERSION_NOT_FOUND","模板版本不存在");requirePark(park,version.getParkId(),permission);PrintTemplate template=templates.findTemplate(version.getTemplateId());if(template==null)throw error(404,"BINDING_TARGET_NOT_FOUND","版本所属模板不存在");requirePark(park,template.getParkId(),permission);classification(subject,PrintJson.tree(template));
        if(!"ACTIVE".equals(template.getLifecycleStatus())||!"PUBLISHED".equals(version.getVersionStatus())||!role.equals(version.getFaceRole())||!role.equals(template.getFaceRole())||!Integer.valueOf(1).equals(version.getSideCount()))throw error(422,"TEMPLATE_VALIDATION_FAILED","必须选择有效的已发布单面版本");PrintTemplateValidator.integrity(version);
        PrintTemplateRequest request=new PrintTemplateRequest();request.setName(template.getName());request.setPrintItemType(template.getPrintItemType());request.setPersonType(template.getPersonType());request.setClassificationCode(template.getClassificationCode());request.setFaceRole(role);request.setSideCount(1);request.setLayoutJson(PrintJson.read(version.getLayoutJson()));request.setFieldSchemaJson(PrintJson.read(version.getFieldSchemaJson()));request.setPageSpecJson(PrintJson.read(version.getPageSpecJson()));request.setResourceManifest(PrintJson.read(version.getResourceManifestJson()));
        if("VISITOR_SECURITY".equals(template.getClassificationCode())) {
            boolean bound=false;for(JsonNode field:request.getFieldSchemaJson().path("fields"))if("visitorCredentialPayload".equals(text(field,"key"))&&field.path("required").asBoolean()){for(JsonNode c:request.getLayoutJson().path("schemas").path(0))if(Objects.equals(text(c,"name"),text(field,"schemaName"))&&"qrcode".equals(text(c,"type")))bound=true;}
            if(!bound||(validateFields&&text(subject.path("fields"),"visitorCredentialPayload")==null))throw error(422,"SECURITY_QR_TEMPLATE_REQUIRED","保密访客必须使用受控带码模板和有效凭证");
        }
        validator.validate(park,request);
        if(validateFields)for(JsonNode field:request.getFieldSchemaJson().path("fields"))if(field.path("required").asBoolean()&&text(subject.path("fields"),text(field,"key"))==null)throw error(422,"PRINT_SUBJECT_INVALID","人员缺少模板要求的字段");return version;
    }
    private void validateRule(PrintBindingRule rule) {
        PrintTemplateValidator.key(rule.getClassificationCode());if(!Arrays.asList("COMPANY","SUPPLIER","EXPLICIT_DEFAULT").contains(rule.getScopeType()))throw invalid();if("EXPLICIT_DEFAULT".equals(rule.getScopeType())?rule.getScopeId()!=null:rule.getScopeId()==null||rule.getScopeId().trim().isEmpty()||rule.getScopeId().length()>64)throw invalid();
        validateScope(rule);
        if(rule.getValidFrom()==null||(rule.getValidTo()!=null&&!rule.getValidTo().after(rule.getValidFrom())))throw invalid();if(rule.getPriority()==null)rule.setPriority(100);
        if(employee(rule.getPrintItemType(),rule.getPersonType())){Set<String> codes=grades(rule);Map<String,String> known=config.confirmedGrades(rule.getParkId());if(codes.isEmpty()||codes.size()>100)throw error(422,"EMPLOYEE_GRADE_REQUIRED","正式员工绑定必须填写适用职级");for(String code:codes)if(!known.containsKey(code))throw error(422,"EMPLOYEE_GRADE_UNMAPPED","绑定包含未知职级");rule.setEmployeeGradeCodesClob(PrintJson.canonical(codes));}else if(rule.getEmployeeGradeCodesClob()!=null)throw invalid();
        boolean staff="STAFF_CARD".equals(rule.getPrintItemType());if(staff?(rule.getPairId()==null||rule.getTemplateId()!=null):(!"VISITOR_SLIP".equals(rule.getPrintItemType())||rule.getTemplateId()==null||rule.getPairId()!=null))throw invalid();
        ObjectNode selection=PrintJson.object().put("kind","BOUND");if(staff)selection.put("pairId",rule.getPairId());else selection.put("templateId",rule.getTemplateId());target(rule.getParkId(),PrintJson.tree(rule),selection,"publish",false);
    }
    private void validateScope(PrintBindingRule rule) {
        if("EXPLICIT_DEFAULT".equals(rule.getScopeType()))return;
        List<String> parks;
        if("COMPANY".equals(rule.getScopeType())) {
            if(!Arrays.asList("EMPLOYEE","OUTSOURCED","DISPATCHED").contains(rule.getPersonType()))throw invalid();
            parks=bindings.companyParks(rule.getScopeId(),rule.getPersonType());
        } else parks=bindings.supplierParks(rule.getScopeId());
        if(parks.isEmpty())throw error(404,"BINDING_SCOPE_NOT_FOUND","绑定单位不存在或已停用");
        if(parks.size()!=1||!rule.getParkId().equals(parks.get(0)))throw scope();
    }
    private void conflicts(PrintBindingRule candidate) {
        if(!"ACTIVE".equals(candidate.getStatus()))return;
        for(PrintBindingRule existing:bindings.candidates(candidate.getParkId(),candidate.getPrintItemType(),candidate.getPersonType(),candidate.getClassificationCode())){
            if(Objects.equals(existing.getBindingRuleId(),candidate.getBindingRuleId())||!Objects.equals(existing.getScopeType(),candidate.getScopeType())||!Objects.equals(existing.getScopeId(),candidate.getScopeId())||!Objects.equals(existing.getPriority(),candidate.getPriority()))continue;
            boolean overlap=(existing.getValidTo()==null||candidate.getValidFrom().before(existing.getValidTo()))&&(candidate.getValidTo()==null||existing.getValidFrom().before(candidate.getValidTo()));
            if(overlap&&(!employee(candidate.getPrintItemType(),candidate.getPersonType())||!Collections.disjoint(grades(existing),grades(candidate))))throw error(409,"BINDING_AMBIGUOUS","同范围、优先级、生效区间和职级存在重叠规则");
        }
    }
    private Set<String> grades(PrintBindingRule rule){Set<String> result=new LinkedHashSet<>();if(rule.getEmployeeGradeCodesClob()==null)return result;JsonNode json=PrintJson.read(rule.getEmployeeGradeCodesClob());if(!json.isArray()||json.size()>100)throw invalid();for(JsonNode code:json)if(!code.isTextual()||!result.add(code.asText()))throw invalid();return result;}
    private void apply(PrintBindingRule rule,PrintBindingRequest request){rule.setPriority(request.getPriority()==null?100:request.getPriority());rule.setValidFrom(time(request.getValidFrom(),true));rule.setValidTo(time(request.getValidTo(),false));rule.setEmployeeGradeCodesClob(request.getEmployeeGradeCodes()==null?null:PrintJson.canonical(request.getEmployeeGradeCodes()));rule.setUpdatedBy(access.actor());rule.setUpdatedAt(now());}
    private PrintBindingRule binding(String id){access.user();PrintAccessPolicy.uuid(id);PrintBindingRule result=bindings.find(id);if(result==null)throw error(404,"BINDING_NOT_FOUND","绑定不存在");access.require("publish",result.getParkId());return result;}
    private void lock(String park){if(bindings.lockPark(park)==null)throw error(404,"PRINT_PARK_NOT_FOUND","绑定园区不存在");}
    private Map<String,Object> view(PrintBindingRule rule){Map<String,Object> result=PrintJson.map(rule);result.remove("employeeGradeCodesClob");result.put("employeeGradeCodes",rule.getEmployeeGradeCodesClob()==null?null:PrintJson.read(rule.getEmployeeGradeCodesClob()));for(String key:Arrays.asList("validFrom","validTo","createdAt","updatedAt"))if(result.get(key) instanceof Number)result.put(key,Instant.ofEpochMilli(((Number)result.get(key)).longValue()).toString());return result;}
    private PrintMutationResult command(String key,String action,Object body,Supplier<Map<String,Object>> operation){
        if(key==null||!key.matches("[\\x20-\\x7e]{1,128}"))throw error(422,"IDEMPOTENCY_KEY_REQUIRED","需要有效的 Idempotency-Key");String actor=access.actor();Map<String,Object> envelope=new LinkedHashMap<>();envelope.put("action",action);envelope.put("body",body);if(body instanceof PrintBindingRequest)envelope.put("validToSpecified",((PrintBindingRequest)body).isValidToSpecified());String hash=PrintJson.hash(envelope);PrintOperation prior=templates.findOperation(actor,key);if(prior!=null)return replay(prior,hash);
        try{return transactions.execute(status->{PrintOperation record=new PrintOperation();record.setOperationId(id());record.setPrincipalId(actor);record.setIdempotencyKey(key);record.setBodyHash(hash);record.setCreatedAt(now());templates.insertOperation(record);Map<String,Object> data=operation.get();record.setResponseJson(PrintJson.canonical(data));templates.completeOperation(record);return new PrintMutationResult(data,false);});}
        catch(DuplicateKeyException e){prior=templates.findOperation(actor,key);if(prior!=null)return replay(prior,hash);throw error(409,"PRINT_CONCURRENT_MODIFICATION","绑定被并发修改");}
    }
    private PrintMutationResult replay(PrintOperation prior,String hash){if(!hash.equals(prior.getBodyHash()))throw error(409,"IDEMPOTENCY_KEY_REUSED","幂等键已用于不同请求");if(prior.getResponseJson()==null)throw error(409,"PRINT_OPERATION_IN_PROGRESS","请求正在处理");return new PrintMutationResult(PrintJson.map(PrintJson.read(prior.getResponseJson())),true);}
    private void audit(PrintBindingRule rule,String action){Map<String,Object> details=view(rule);RequestAttributes attrs=RequestContextHolder.getRequestAttributes();Object trace=attrs==null?null:attrs.getAttribute("print.requestId",RequestAttributes.SCOPE_REQUEST);details.put("requestId",trace instanceof String?trace:id());PrintAudit audit=new PrintAudit();audit.setAuditId(id());audit.setParkId(rule.getParkId());audit.setActorId(access.actor());audit.setAction(action);audit.setObjectId(rule.getBindingRuleId());audit.setDetailsJson(PrintJson.canonical(details));audit.setCreatedAt(now());templates.insertAudit(audit);}
    private void requirePark(String park,String actual,String permission){access.require(permission,actual);if(!Objects.equals(park,actual))throw scope();}
    private String park(String query,String body){if(query!=null&&body!=null&&!query.equals(body))throw scope();return access.resolvePark(query!=null?query:body);}
    private static void classification(JsonNode subject,JsonNode target){for(String key:Arrays.asList("printItemType","personType","classificationCode"))if(!Objects.equals(text(subject,key),text(target,key)))throw error(422,"TEMPLATE_VALIDATION_FAILED","模板与人员打印分类不一致");}
    private static void copyGrade(JsonNode subject,ObjectNode result){if(employee(text(subject,"printItemType"),text(subject,"personType")))for(String key:Arrays.asList("employeeGradeCode","employeeGradeName","employeeGradeSource"))result.set(key,subject.path(key));}
    private static boolean employee(String item,String person){return "STAFF_CARD".equals(item)&&"EMPLOYEE".equals(person);}
    private static String text(JsonNode node,String key){if(node==null||key==null||!node.path(key).isTextual())return null;String value=node.path(key).asText().trim();return value.isEmpty()?null:value;}
    private static Timestamp time(String value,boolean required){if(value==null){if(required)throw invalid();return null;}try{return Timestamp.from(Instant.parse(value));}catch(Exception e){throw invalid();}}
    private static void revision(Long expected,Long actual,String code){if(expected==null||!expected.equals(actual))throw error(409,code,"修订已变化，请重新加载");}
    private static Timestamp now(){return Timestamp.from(Instant.now());}
    private static String id(){return UUID.randomUUID().toString();}
    private static PrintApiException invalid(){return error(400,"INVALID_REQUEST","绑定或选择参数不合法");}
    private static PrintApiException scope(){return error(403,"PRINT_SCOPE_DENIED","对象不属于同一授权园区");}
    private static PrintApiException error(int status,String code,String message){return new PrintApiException(status,code,message);}
}
