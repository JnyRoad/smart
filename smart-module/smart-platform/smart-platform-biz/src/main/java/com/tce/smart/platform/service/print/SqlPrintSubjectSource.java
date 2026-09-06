package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.core.mapper.PrintSubjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

/** 从本系统已同步的可信业务表构造冻结资料，所有查询先经过园区及打印操作授权。 */
@Service
@Transactional(readOnly=true)
public class SqlPrintSubjectSource implements PrintSubjectSource {
    private static final Set<String> TYPES = new HashSet<>(Arrays.asList("STAFF","SUPPLIER_PERSON","VISITOR","VISITOR_COMPANION","ADMITTANCE","ADMITTANCE_COMPANION"));
    private final PrintSubjectMapper mapper;
    private final PrintAccessPolicy access;
    private final PrintResolutionProperties grades;
    private final PrintSubjectProperties properties;
    private final Clock clock;
    @Autowired public SqlPrintSubjectSource(PrintSubjectMapper mapper,PrintAccessPolicy access,PrintResolutionProperties grades,PrintSubjectProperties properties) {
        this(mapper,access,grades,properties,Clock.systemDefaultZone());
    }
    public SqlPrintSubjectSource(PrintSubjectMapper mapper,PrintAccessPolicy access,PrintResolutionProperties grades,PrintSubjectProperties properties,Clock clock) {
        this.mapper=mapper;this.access=access;this.grades=grades;this.properties=properties;this.clock=clock;
    }

    @Override public ObjectNode load(String parkId,String subjectType,String subjectId) {
        String park=authorize(parkId,subjectType);
        if(subjectId==null||!subjectId.matches("[1-9][0-9]{0,18}"))throw error("INVALID_REQUEST","人员标识无效");
        Map<String,Object> row=unique(mapper.subject(park,subjectType,subjectId));
        ObjectNode result=PrintJson.object().put("parkId",park).put("subjectType",subjectType).put("subjectId",subjectId);
        ObjectNode fields=result.putObject("fields");result.putArray("resources");
        if("STAFF".equals(subjectType)) staff(park,subjectId,row,result,fields);
        else if("SUPPLIER_PERSON".equals(subjectType)) supplier(row,result,fields);
        else visitor(park,subjectType,row,result,fields);
        photo(park,subjectType,text(row,"photoCode"),(ArrayNode)result.path("resources"));
        return result;
    }

    @Override public ObjectNode search(String parkId,String subjectType,String keyword,int current,int size) {
        String park=authorize(parkId,subjectType);
        if(current<1||size<1||size>50||keyword==null||keyword.length()>100||keyword.chars().anyMatch(Character::isISOControl))throw error("INVALID_REQUEST","搜索参数无效");
        String term=keyword.trim();
        String pattern=term.isEmpty()?null:"%"+term.replace("!","!!").replace("%","!%").replace("_","!_")+"%";
        long offset=((long)current-1)*size;
        ObjectNode result=PrintJson.object().put("total",mapper.count(park,subjectType,pattern)).put("current",current).put("size",size);
        ArrayNode records=result.putArray("records");
        for(Map<String,Object> row:mapper.search(park,subjectType,pattern,offset,offset+size)) {
            records.addObject().put("subjectId",text(row,"id")).put("subjectType",subjectType).put("displayName",text(row,"name"))
                    .put("staffNo",text(row,"staffNo")).put("employeeGradeName",text(row,"gradeName"));
        }
        return result;
    }

    private String authorize(String parkId,String type) {
        String park=access.resolvePark(parkId);access.require("execute",park);
        if(!TYPES.contains(type))throw error("INVALID_REQUEST","请选择明确的人员来源");
        return park;
    }
    private void staff(String park,String id,Map<String,Object> row,ObjectNode result,ObjectNode fields) {
        if(!Arrays.asList("1","2","3","4").contains(text(row,"status")))throw invalidSubject();
        List<Map<String,Object>> organizations=mapper.staffOrganizations(park,text(row,"companyId"));
        if(organizations.size()!=1)throw error("PRINT_SUBJECT_AMBIGUOUS","人员公司归属不唯一");
        String person=text(organizations.get(0),"personType");
        if(!Arrays.asList("EMPLOYEE","OUTSOURCED","DISPATCHED").contains(person))throw invalidSubject();
        result.put("printItemType","STAFF_CARD").put("personType",person).put("companyId",text(row,"companyId"));
        result.put("classificationCode","EMPLOYEE".equals(person)?"STAFF_DEFAULT":"OUTSOURCED".equals(person)?"OUTSOURCE_DEFAULT":"DISPATCH_DEFAULT");
        if("EMPLOYEE".equals(person)) {
            String code=text(row,"gradeCode"),name=text(row,"gradeName");Map<String,String> dictionary=grades.confirmedGrades(park);
            if(code==null||name==null||!name.equals(dictionary.get(code)))throw error("EMPLOYEE_GRADE_UNMAPPED","人员职级与已确认 DHR 字典不一致");
            result.put("employeeGradeCode",code).put("employeeGradeName",name).put("employeeGradeSource","DHR");fields.put("employeeGradeName",name);
        }
        List<Map<String,Object>> validCards=new ArrayList<>();
        for(Map<String,Object> card:mapper.cards(park,id)) {
            String number=text(card,"cardNo");
            if(number!=null&&number.matches("[A-Z0-9]{8,20}")&&!number.startsWith("999")
                    &&(park+":"+number).equals(text(card,"activeKey"))&&text(row,"staffNo")!=null&&text(row,"staffNo").equals(text(card,"badge")))validCards.add(card);
        }
        if(validCards.size()!=1)throw error("STAFF_CARD_NOT_REGISTERED","人员必须有唯一且同园区的已登记实体卡");
        result.put("cardRegistrationVerified",true);
        fields.put("staffName",required(row,"name")).put("staffNo",required(row,"staffNo")).put("companyName",value(row,"companyName"))
                .put("departmentName",value(row,"departmentName")).put("cardNo",text(validCards.get(0),"cardNo"));
    }

    private void visitor(String park,String type,Map<String,Object> row,ObjectNode result,ObjectNode fields) {
        if(!Arrays.asList("0","3").contains(text(row,"status")))throw invalidSubject();
        Instant from=instant(row.get("startTime")),to=instant(row.get("endTime"));
        Long early=properties.getEarlyPrintSeconds().get(park);long earlySeconds=early==null?0:early;
        if(earlySeconds<0||earlySeconds>86400*30L)throw error("PRINT_SUBJECT_CONFIG_INVALID","预印窗口配置无效");
        if(from==null||to==null||!from.isBefore(to)||from.isAfter(clock.instant().plusSeconds(earlySeconds))||!clock.instant().isBefore(to))throw invalidSubject();
        boolean modern=type.startsWith("ADMITTANCE");boolean vip;
        if(modern) {
            if(!Arrays.asList("2","3").contains(text(row,"visitorType")))throw invalidSubject();
            vip="2".equals(text(row,"visitorType"));
            if("ADMITTANCE".equals(type)&&(!Objects.equals(text(row,"name"),text(row,"parentName"))
                    ||!Objects.equals(text(row,"photoCode"),text(row,"parentPhotoCode"))))throw error("PRINT_SUBJECT_AMBIGUOUS","新申请主人员与父申请资料不一致");
        } else vip=text(row,"promoterBadge")!=null;
        boolean security=classification(park,modern,row);
        result.put("printItemType","VISITOR_SLIP").put("personType","VISITOR").put("vip",vip)
                .put("classificationCode",security?"VISITOR_SECURITY":"VISITOR_NORMAL");
        fields.put("visitorName",required(row,"name")).put("companyName",value(row,"companyName"))
                .put("validFrom",from.toString()).put("validTo",to.toString());
        String credential=text(row,"credential");
        if(security&&credential==null)throw error("VISITOR_CREDENTIAL_REQUIRED","保密访客缺少可信预约凭证");
        if(credential!=null)fields.put("visitorCredentialPayload",credential);
    }
    /** 供应商人员只打印访客式单面凭条，不复用员工实体卡或 HiTi 厂牌资料。 */
    private void supplier(Map<String,Object> row,ObjectNode result,ObjectNode fields) {
        result.put("printItemType","VISITOR_SLIP").put("personType","SUPPLIER")
                .put("classificationCode","SUPPLIER_DEFAULT").put("supplierId",required(row,"supplierId"));
        fields.put("visitorName",required(row,"name")).put("companyName",required(row,"companyName"));
    }
    private boolean classification(String park,boolean modern,Map<String,Object> row) {
        if(!modern) {
            String strategy=properties.getLegacyClassification().get(park);
            if(!Arrays.asList("NORMAL","SECURITY").contains(strategy))throw classificationError();
            return "SECURITY".equals(strategy);
        }
        if(!"AUTHORITY_MAPPING".equals(properties.getAdmittanceClassification().get(park)))throw classificationError();
        String areaTypes=text(row,"areaType");if(areaTypes==null||areaTypes.length()>2000)throw classificationError();
        boolean security=false;
        for(String area:new LinkedHashSet<>(Arrays.asList(areaTypes.split(",",-1)))) {
            if(!area.matches("[0-9]{1,10}"))throw classificationError();
            List<Map<String,Object>> mappings=mapper.areaAuthorities(park,area);if(mappings.isEmpty())throw classificationError();
            for(Map<String,Object> mapping:mappings) {
                String nature=text(mapping,"nature");
                if(text(mapping,"authorityId")==null||!Arrays.asList("0","1").contains(nature))throw classificationError();
                security|="1".equals(nature);
            }
        }
        return security;
    }

    private void photo(String park,String type,String code,ArrayNode resources) {
        if(code==null)return;
        PrintSubjectProperties.PhotoDomain domain=properties.getPhotos().get(type);
        if(domain==null||domain.getStorageDomain()==null||domain.getAllowedTypes()==null||domain.getAllowedTypes().isEmpty()
                ||domain.getAllowedTypes().stream().anyMatch(t->t==null||t<0))throw error("PRINT_PHOTO_SOURCE_NOT_CONFIGURED","照片存储域或图片类型尚未确认");
        String storage=domain.getStorageDomain();
        if("NULL".equals(storage))storage=null;else if("PARK".equals(storage))storage=park;
        else if(!storage.matches("0|[1-9][0-9]{0,9}"))throw error("PRINT_PHOTO_SOURCE_NOT_CONFIGURED","照片存储域无效");
        if(properties.getMaxPhotoBytes()<1||properties.getMaxPhotoBytes()>20*1024*1024||properties.getMaxPhotoPixels()<1||properties.getMaxPhotoPixels()>40000000)throw error("PRINT_SUBJECT_CONFIG_INVALID","图片资源上限配置无效");
        List<byte[]> matches=mapper.photos(code,storage,domain.getAllowedTypes());
        if(matches.size()!=1||matches.get(0)==null)throw photoError();
        byte[] bytes=matches.get(0);if(bytes.length==0||bytes.length>properties.getMaxPhotoBytes())throw photoError();
        try(MemoryCacheImageInputStream input=new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw photoError();
            ImageReader reader=readers.next();
            try {
                String format=reader.getFormatName().toLowerCase(Locale.ROOT);
                if(!Arrays.asList("png","jpeg","jpg").contains(format))throw photoError();
                reader.setInput(input,true,true);int width=reader.getWidth(0),height=reader.getHeight(0);
                if(width<1||height<1||(long)width*height>properties.getMaxPhotoPixels()||reader.read(0)==null)throw photoError();
                StringBuilder hash=new StringBuilder("sha256:");for(byte octet:MessageDigest.getInstance("SHA-256").digest(bytes))hash.append(String.format("%02x",octet&255));
                resources.addObject().put("bindingKey","personPhoto").put("mediaType","png".equals(format)?"image/png":"image/jpeg")
                        .put("sha256",hash.toString()).put("bytesBase64",Base64.getEncoder().encodeToString(bytes));
            } finally { reader.dispose(); }
        } catch(PrintApiException ex) {throw ex;} catch(Exception ex) {throw photoError();}
    }
    private static Map<String,Object> unique(List<Map<String,Object>> rows) {
        if(rows.isEmpty())throw new PrintApiException(404,"PRINT_SUBJECT_NOT_FOUND","当前园区没有该人员记录");
        if(rows.size()!=1)throw error("PRINT_SUBJECT_AMBIGUOUS","人员关联记录不唯一");
        return rows.get(0);
    }
    private Instant instant(Object value) {
        if(value instanceof java.util.Date)return ((java.util.Date)value).toInstant();
        if(value instanceof LocalDateTime)return ((LocalDateTime)value).atZone(clock.getZone()).toInstant();
        return null;
    }
    private static String text(Map<String,Object> row,String key){Object v=row.get(key);if(v==null)return null;String s=String.valueOf(v);return s.trim().isEmpty()?null:s;}
    private static String value(Map<String,Object> row,String key){String s=text(row,key);return s==null?"":s;}
    private static String required(Map<String,Object> row,String key){String s=text(row,key);if(s==null)throw invalidSubject();return s;}
    private static PrintApiException invalidSubject(){return error("PRINT_SUBJECT_INVALID","人员状态、身份或有效期不满足打印要求");}
    private static PrintApiException classificationError(){return error("VISITOR_CLASSIFICATION_UNMAPPED","访客保密分类策略尚未确认或区域映射不完整");}
    private static PrintApiException photoError(){return error("PRINT_PHOTO_INVALID","照片缺失、不唯一、格式不合法或超过资源上限");}
    private static PrintApiException error(String code,String message){return new PrintApiException(422,code,message);}
}
