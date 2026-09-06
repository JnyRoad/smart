package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.smart.platform.core.mapper.PrintSubjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.*;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.core.context.SecurityContextHolder;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.time.*;
import java.util.*;
import static org.junit.Assert.*;
import static com.tce.smart.platform.service.print.PrintAccessPolicyTest.*;

/** 隔离的本系统表夹具；不访问 DHR 或真实人员库。 */
public class PrintSubjectSourceTest {
 private JdbcTemplate jdbc;
 private PrintSubjectSource source;
 private PrintSubjectMapper mapper;
 private PrintAccessPolicy policy;
 private PrintResolutionProperties grades;
 private static final Instant NOW=Instant.parse("2026-09-05T08:00:00Z");
 @Before public void setup() throws Exception {
  DriverManagerDataSource db=new DriverManagerDataSource("jdbc:h2:mem:printsubject"+UUID.randomUUID()+";MODE=Oracle;DB_CLOSE_DELAY=-1","sa","");
  db.setDriverClassName("org.h2.Driver");jdbc=new JdbcTemplate(db);
  for(String ddl:SCHEMA)jdbc.execute(ddl);
  SqlSessionFactoryBean factory=new SqlSessionFactoryBean();factory.setDataSource(db);factory.setMapperLocations(new org.springframework.core.io.Resource[]{new ClassPathResource("mapper/PrintSubjectMapper.xml")});
  mapper=new SqlSessionTemplate(factory.getObject()).getMapper(PrintSubjectMapper.class);
  PrintFeatureProperties feature=properties();feature.getPermissions().put("execute","test:print:execute");policy=new PrintAccessPolicy(feature,null);
  grades=new PrintResolutionProperties();PrintResolutionProperties.GradeDictionary dictionary=new PrintResolutionProperties.GradeDictionary();dictionary.setConfirmed(true);dictionary.getCodes().put("9","员工级");grades.getEmployeeGrades().put("1",dictionary);
  configure("{}");
  login(1,"test:print:execute");
  jdbc.update("INSERT INTO SMT_PARK_BU VALUES (1,'C1',1)");
  jdbc.update("INSERT INTO SMT_STAFF VALUES (10,'张三','B10','C1','合成公司','制造部','9','员工级',NULL,1)");
  jdbc.update("INSERT INTO SMT_ISC_STAFF_CARD VALUES (1,10,'B10',1,0,'1:A1234567','A1234567')");
  jdbc.update("INSERT INTO SMT_VISITOR VALUES (20,1,'旧主',NULL,0,0,?,?,NULL,'old-code','旧公司')",java.sql.Timestamp.from(NOW.minusSeconds(60)),java.sql.Timestamp.from(NOW.plusSeconds(3600)));
  jdbc.update("INSERT INTO SMT_FELLOW_VISITOR VALUES (21,20,'旧随行',NULL)");
  jdbc.update("INSERT INTO SMT_ADMITTANCE_APPLY VALUES (20,1,'新主',NULL,0,?,?,2,'new-code','新公司','101')",java.sql.Timestamp.from(NOW.minusSeconds(60)),java.sql.Timestamp.from(NOW.plusSeconds(3600)));
  jdbc.update("INSERT INTO SMT_ADMITTANCE_FELLOW VALUES (20,20,1,'新主',NULL)");
  jdbc.update("INSERT INTO SMT_ADMITTANCE_FELLOW VALUES (21,20,0,'新随行',NULL)");
 }
 private void configure(String json)throws Exception {
  PrintSubjectProperties config=new ObjectMapper().readValue(json,PrintSubjectProperties.class);
  source=new SqlPrintSubjectSource(mapper,policy,grades,config,Clock.fixed(NOW,ZoneOffset.UTC));
 }
 private void visitors()throws Exception {configure("{\"legacyClassification\":{\"1\":\"NORMAL\"},\"admittanceClassification\":{\"1\":\"AUTHORITY_MAPPING\"}}");jdbc.update("INSERT INTO SMT_ADMITTANCE_AREA_TYPE_AUTH VALUES (1,101,1,100,1)");jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY VALUES (100,1,1,0)");}
 @After public void close(){SecurityContextHolder.clearContext();if(jdbc!=null)jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) connection -> {try(java.sql.Statement statement=connection.createStatement()){statement.execute("SHUTDOWN");}return null;});}
 @Test public void staffIdentityGradeAndRegisteredCardComeOnlyFromScopedTables(){
  ObjectNode s=source.load("1","STAFF","10");assertEquals("10",s.path("subjectId").asText());assertEquals("EMPLOYEE",s.path("personType").asText());assertEquals("DHR",s.path("employeeGradeSource").asText());assertEquals("员工级",s.path("employeeGradeName").asText());assertTrue(s.path("cardRegistrationVerified").asBoolean());assertEquals("A1234567",s.at("/fields/cardNo").asText());assertEquals("B10",s.at("/fields/staffNo").asText());assertFalse(s.toString().contains("certNo"));
  expectCode("PRINT_SCOPE_DENIED",()->source.load("2","STAFF","10"));login(1,"test:print:read");expectCode("PRINT_PERMISSION_DENIED",()->source.load("1","STAFF","10"));
 }
 @Test public void rejectsOrphanConflictAndUnconfirmedDhr(){
  jdbc.update("UPDATE SMT_STAFF SET JCHE_NAME='猜测职级'");expectCode("EMPLOYEE_GRADE_UNMAPPED",()->source.load("1","STAFF","10"));jdbc.update("UPDATE SMT_STAFF SET JCHE_NAME='员工级',COMP_ID='42'");
  expectCode("PRINT_SUBJECT_NOT_FOUND",()->source.load("1","STAFF","10"));jdbc.update("INSERT INTO SMT_ORGANIZE_RELATION VALUES (42,1,1)");assertEquals("OUTSOURCED",source.load("1","STAFF","10").path("personType").asText());
  jdbc.update("INSERT INTO SMT_PARK_BU VALUES (2,'42',1)");expectCode("PRINT_SUBJECT_AMBIGUOUS",()->source.load("1","STAFF","10"));
 }
 @Test public void cardsRequireOneMatchingPhysicalRegistration(){
  for(String card:Arrays.asList("99912345","a1234567","123","ABCDEFGH!")){jdbc.update("UPDATE SMT_ISC_STAFF_CARD SET CARD_NO=?,ACTIVE_KEY=?",card,"1:"+card);expectCode("STAFF_CARD_NOT_REGISTERED",()->source.load("1","STAFF","10"));}
  jdbc.update("UPDATE SMT_ISC_STAFF_CARD SET CARD_NO='A1234567',ACTIVE_KEY='2:A1234567'");expectCode("STAFF_CARD_NOT_REGISTERED",()->source.load("1","STAFF","10"));jdbc.update("UPDATE SMT_ISC_STAFF_CARD SET ACTIVE_KEY='1:A1234567',PARK_ID=2");expectCode("STAFF_CARD_NOT_REGISTERED",()->source.load("1","STAFF","10"));
  jdbc.update("UPDATE SMT_ISC_STAFF_CARD SET PARK_ID=1");jdbc.update("INSERT INTO SMT_ISC_STAFF_CARD VALUES (2,10,'B10',1,0,'1:B1234567','B1234567')");expectCode("STAFF_CARD_NOT_REGISTERED",()->source.load("1","STAFF","10"));jdbc.update("DELETE FROM SMT_ISC_STAFF_CARD");expectCode("STAFF_CARD_NOT_REGISTERED",()->source.load("1","STAFF","10"));
 }
 @Test public void separatesLegacyNewMainAndCompanionIdsAndUsesParentCredential()throws Exception{
  visitors();String[] types={"VISITOR","VISITOR_COMPANION","ADMITTANCE","ADMITTANCE_COMPANION"};String[] names={"旧主","旧随行","新主","新随行"};
  for(int i=0;i<4;i++){ObjectNode s=source.load("1",types[i],i%2==0?"20":"21");assertEquals(names[i],s.at("/fields/visitorName").asText());assertEquals(i<2?"old-code":"new-code",s.at("/fields/visitorCredentialPayload").asText());assertEquals(types[i],s.path("subjectType").asText());assertEquals(i>=2,s.path("vip").asBoolean());}
  expectCode("PRINT_SUBJECT_NOT_FOUND",()->source.load("1","ADMITTANCE_COMPANION","20"));jdbc.update("INSERT INTO SMT_ADMITTANCE_FELLOW VALUES (22,20,1,'另一主',NULL)");expectCode("PRINT_SUBJECT_AMBIGUOUS",()->source.load("1","ADMITTANCE","20"));
 }
 @Test public void visitorStatusValidityAndParentParkAreMandatory()throws Exception{
  visitors();jdbc.update("UPDATE SMT_ADMITTANCE_APPLY SET STATUS=7");expectCode("PRINT_SUBJECT_INVALID",()->source.load("1","ADMITTANCE_COMPANION","21"));jdbc.update("UPDATE SMT_ADMITTANCE_APPLY SET STATUS=0,START_TIME=?",java.sql.Timestamp.from(NOW.plusSeconds(60)));expectCode("PRINT_SUBJECT_INVALID",()->source.load("1","ADMITTANCE","20"));jdbc.update("UPDATE SMT_ADMITTANCE_APPLY SET PARK_ID=2");expectCode("PRINT_SUBJECT_NOT_FOUND",()->source.load("1","ADMITTANCE_COMPANION","21"));jdbc.update("UPDATE SMT_VISITOR SET DEL_FLAG=1");expectCode("PRINT_SUBJECT_NOT_FOUND",()->source.load("1","VISITOR_COMPANION","21"));
 }
 @Test public void classificationRequiresCompleteSameParkAuthorityAndKeepsVipIndependent()throws Exception{
  expectCode("VISITOR_CLASSIFICATION_UNMAPPED",()->source.load("1","VISITOR","20"));visitors();assertEquals("VISITOR_NORMAL",source.load("1","ADMITTANCE","20").path("classificationCode").asText());jdbc.update("UPDATE SMT_DEVICE_AUTHORITY SET AREA_TYPE=1");assertEquals("VISITOR_SECURITY",source.load("1","ADMITTANCE","20").path("classificationCode").asText());jdbc.update("UPDATE SMT_DEVICE_AUTHORITY SET PARK_ID=2");expectCode("VISITOR_CLASSIFICATION_UNMAPPED",()->source.load("1","ADMITTANCE","20"));jdbc.update("UPDATE SMT_DEVICE_AUTHORITY SET PARK_ID=1");jdbc.update("UPDATE SMT_ADMITTANCE_APPLY SET AREA_TYPE='101,102'");expectCode("VISITOR_CLASSIFICATION_UNMAPPED",()->source.load("1","ADMITTANCE","20"));
 }
 @Test public void photosUseExplicitNullDomainCodeAndActualDecodedImage()throws Exception{
  visitors();jdbc.update("UPDATE SMT_VISITOR SET VISITOR_PHOTO_ID='face-code'");byte[] png=png();jdbc.update("INSERT INTO SMT_IMAGE VALUES (1,1,'face-code',12,?)",png);
  configure("{\"legacyClassification\":{\"1\":\"NORMAL\"},\"photos\":{\"VISITOR\":{\"storageDomain\":\"NULL\",\"allowedTypes\":[12]}}}");expectCode("PRINT_PHOTO_INVALID",()->source.load("1","VISITOR","20"));jdbc.update("UPDATE SMT_IMAGE SET PARK_ID=NULL");ObjectNode s=source.load("1","VISITOR","20");assertEquals("personPhoto",s.at("/resources/0/bindingKey").asText());assertEquals("image/png",s.at("/resources/0/mediaType").asText());assertArrayEquals(png,Base64.getDecoder().decode(s.at("/resources/0/bytesBase64").asText()));assertTrue(s.at("/resources/0/sha256").asText().matches("sha256:[a-f0-9]{64}"));assertFalse(s.toString().contains("face-code"));jdbc.update("UPDATE SMT_IMAGE SET IMAGE=?",new byte[]{1,2,3});expectCode("PRINT_PHOTO_INVALID",()->source.load("1","VISITOR","20"));
 }
 @Test public void searchPaginatesAfterParkFilterAndNeverIncludesPrivateFields()throws Exception{
  jdbc.update("INSERT INTO SMT_STAFF VALUES (11,'张四','B11','C1','合成公司','制造部','9','员工级',NULL,0)");jdbc.update("INSERT INTO SMT_STAFF VALUES (9,'张外','B9','foreign','异园公司','制造部','9','员工级',NULL,1)");
  ObjectNode result=search("STAFF","张",2,1);assertEquals(2,result.path("total").asInt());assertEquals("11",result.at("/records/0/subjectId").asText());assertEquals(1,result.path("records").size());Set<String> keys=new HashSet<>();result.at("/records/0").fieldNames().forEachRemaining(keys::add);assertEquals(new HashSet<>(Arrays.asList("subjectId","subjectType","displayName","staffNo","employeeGradeName")),keys);assertEquals(0,search("STAFF","%' OR 1=1 --",1,10).path("total").asInt());expectCode("INVALID_REQUEST",()->search("STAFF","",1,51));
 }
 @Test public void supplierPrintsVisitorSlipWithoutBorrowingStaffCard()throws Exception{
  jdbc.update("INSERT INTO SMT_SECURITYAREA_SUPPLIER VALUES (5,1,0,'供应商')");jdbc.update("INSERT INTO SMT_SUPPLIER_PERSON VALUES (10,5,0,'长期人员')");assertEquals(1,search("SUPPLIER_PERSON","长期",1,20).path("total").asInt());ObjectNode supplier=source.load("1","SUPPLIER_PERSON","10");assertEquals("VISITOR_SLIP",supplier.path("printItemType").asText());assertEquals("SUPPLIER",supplier.path("personType").asText());assertEquals("SUPPLIER_DEFAULT",supplier.path("classificationCode").asText());assertEquals("5",supplier.path("supplierId").asText());assertEquals("长期人员",supplier.at("/fields/visitorName").asText());assertFalse(supplier.has("cardRegistrationVerified"));jdbc.update("UPDATE SMT_SECURITYAREA_SUPPLIER SET PARK_ID=2");expectCode("PRINT_SUBJECT_NOT_FOUND",()->source.load("1","SUPPLIER_PERSON","10"));
 }
 @Test public void preprintWindowNeverAllowsExpiredOrInvalidVisitorPeriods()throws Exception {
  visitors();jdbc.update("UPDATE SMT_VISITOR SET START_TIME=?",java.sql.Timestamp.from(NOW.plusSeconds(59)));
  expectCode("PRINT_SUBJECT_INVALID",()->source.load("1","VISITOR","20"));
  configure("{\"legacyClassification\":{\"1\":\"NORMAL\"},\"earlyPrintSeconds\":{\"1\":60}}");
  assertEquals("旧主",source.load("1","VISITOR","20").at("/fields/visitorName").asText());
  jdbc.update("UPDATE SMT_VISITOR SET START_TIME=?,END_TIME=?",java.sql.Timestamp.from(NOW.minusSeconds(60)),java.sql.Timestamp.from(NOW));
  expectCode("PRINT_SUBJECT_INVALID",()->source.load("1","VISITOR","20"));
  jdbc.update("UPDATE SMT_VISITOR SET END_TIME=START_TIME");expectCode("PRINT_SUBJECT_INVALID",()->source.load("1","VISITOR","20"));
 }
 @Test public void photoTypeUniquenessSizePixelsAndStaffSharedDomainAreEnforced()throws Exception {
  jdbc.update("UPDATE SMT_STAFF SET FACE_PIC_ID='staff-face'");
  expectCode("PRINT_PHOTO_SOURCE_NOT_CONFIGURED",()->source.load("1","STAFF","10"));
  String domain="\"photos\":{\"STAFF\":{\"storageDomain\":\"0\",\"allowedTypes\":[11]}}";
  configure("{"+domain+"}");jdbc.update("INSERT INTO SMT_IMAGE VALUES (1,0,'staff-face',12,?)",png());
  expectCode("PRINT_PHOTO_INVALID",()->source.load("1","STAFF","10"));jdbc.update("UPDATE SMT_IMAGE SET IMAGE_TYPE=11");
  assertEquals("image/png",source.load("1","STAFF","10").at("/resources/0/mediaType").asText());
  configure("{"+domain+",\"maxPhotoPixels\":3}");expectCode("PRINT_PHOTO_INVALID",()->source.load("1","STAFF","10"));
  configure("{"+domain+",\"maxPhotoBytes\":4}");expectCode("PRINT_PHOTO_INVALID",()->source.load("1","STAFF","10"));
  configure("{"+domain+"}");jdbc.update("INSERT INTO SMT_IMAGE VALUES (2,0,'staff-face',11,?)",png());expectCode("PRINT_PHOTO_INVALID",()->source.load("1","STAFF","10"));
 }
 @Test public void modernSupplierAndConflictingMainAreRejectedWithoutChangingLegacyVip()throws Exception {
  visitors();jdbc.update("UPDATE SMT_ADMITTANCE_APPLY SET PERSON_TYPE=1");expectCode("PRINT_SUBJECT_INVALID",()->source.load("1","ADMITTANCE","20"));
  jdbc.update("UPDATE SMT_ADMITTANCE_APPLY SET PERSON_TYPE=3,VISITOR_NAME='错误主人员'");expectCode("PRINT_SUBJECT_AMBIGUOUS",()->source.load("1","ADMITTANCE","20"));
  jdbc.update("UPDATE SMT_VISITOR SET PROMOTER_BADGE='host'");ObjectNode legacy=source.load("1","VISITOR","20");assertTrue(legacy.path("vip").asBoolean());assertEquals("VISITOR_NORMAL",legacy.path("classificationCode").asText());
  configure("{\"legacyClassification\":{\"1\":\"SECURITY\"}}");jdbc.update("UPDATE SMT_VISITOR SET SMS_CODE=NULL");expectCode("VISITOR_CREDENTIAL_REQUIRED",()->source.load("1","VISITOR_COMPANION","21"));
 }
 @Test public void httpSearchWrapsSafeFieldsAndRejectsUnauthorizedPark()throws Exception {
  org.springframework.test.web.servlet.MockMvc mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(new com.tce.smart.platform.controller.print.PrintSubjectController(source)).setControllerAdvice(new com.tce.smart.platform.controller.print.PrintApiAdvice()).build();
  org.springframework.mock.web.MockHttpServletResponse response=mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/print/v1/print-subjects").param("parkId","1").param("subjectType","STAFF").param("keyword","B10")).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andReturn().getResponse();
  assertEquals("10",PrintJson.read(response.getContentAsString()).at("/data/records/0/subjectId").asText());assertFalse(response.getContentAsString().contains("A1234567"));
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/print/v1/print-subjects").param("parkId","2").param("subjectType","STAFF")).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
 }
 private ObjectNode search(String type,String keyword,int current,int size){return source.search("1",type,keyword,current,size);}

 private byte[] png()throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(2,2,BufferedImage.TYPE_INT_RGB),"png",out);return out.toByteArray();}
 private static final String[] SCHEMA={
  "CREATE TABLE SMT_PARK_BU (ID BIGINT,COMP_ID VARCHAR(64),PARK_ID INT)",
  "CREATE TABLE SMT_ORGANIZE_RELATION (ID BIGINT,PARK_ID INT,COMP_TYPE INT)",
  "CREATE TABLE SMT_STAFF (ID BIGINT,NAME VARCHAR(100),BADGE VARCHAR(64),COMP_ID VARCHAR(64),COMP_NAME VARCHAR(100),DEP_NAME VARCHAR(100),JCHE_ID VARCHAR(64),JCHE_NAME VARCHAR(100),FACE_PIC_ID VARCHAR(100),STATUS INT)",
  "CREATE TABLE SMT_ISC_STAFF_CARD (ID BIGINT,STAFF_ID BIGINT,BADGE VARCHAR(64),PARK_ID INT,DEL_FLAG INT,ACTIVE_KEY VARCHAR(100),CARD_NO VARCHAR(64))",
  "CREATE TABLE SMT_VISITOR (ID BIGINT,PARK_ID INT,VISITOR_NAME VARCHAR(100),VISITOR_PHOTO_ID VARCHAR(100),STATUS INT,DEL_FLAG INT,START_TIME TIMESTAMP,END_TIME TIMESTAMP,PROMOTER_BADGE VARCHAR(64),SMS_CODE VARCHAR(64),COMPANY VARCHAR(100))",
  "CREATE TABLE SMT_FELLOW_VISITOR (ID BIGINT,VISITOR_ID BIGINT,FELLOW_NAME VARCHAR(100),FELLOW_PHOTO_ID VARCHAR(100))",
  "CREATE TABLE SMT_ADMITTANCE_APPLY (ID BIGINT,PARK_ID INT,VISITOR_NAME VARCHAR(100),VISITOR_PHOTO_ID VARCHAR(100),STATUS INT,START_TIME TIMESTAMP,END_TIME TIMESTAMP,PERSON_TYPE INT,SMS_CODE VARCHAR(64),COMPANY VARCHAR(100),AREA_TYPE VARCHAR(200))",
  "CREATE TABLE SMT_ADMITTANCE_FELLOW (ID BIGINT,VISITOR_ID BIGINT,IS_MAIN INT,FELLOW_NAME VARCHAR(100),FELLOW_PHOTO_ID VARCHAR(100))",
  "CREATE TABLE SMT_ADMITTANCE_AREA_TYPE_AUTH (ID BIGINT,AREA_TYPE_ID INT,PARK_ID INT,AUTH_ID INT,AUTH_TYPE INT)",
  "CREATE TABLE SMT_DEVICE_AUTHORITY (ID INT,PARK_ID INT,TYPE INT,AREA_TYPE INT)",
  "CREATE TABLE SMT_IMAGE (ID BIGINT,PARK_ID INT,IMAGE_CODE VARCHAR(100),IMAGE_TYPE INT,IMAGE BLOB)",
  "CREATE TABLE SMT_SECURITYAREA_SUPPLIER (ID BIGINT,PARK_ID INT,DEL_FLAG INT,COMPANY_NAME VARCHAR(100))",
  "CREATE TABLE SMT_SUPPLIER_PERSON (ID BIGINT,SUPPLIER_ID BIGINT,DEL_FLAG INT,PERSON_NAME VARCHAR(100))"
 };
}
