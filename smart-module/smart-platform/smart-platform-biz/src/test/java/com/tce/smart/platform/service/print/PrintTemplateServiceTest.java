package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.smart.platform.api.dto.req.print.*;
import com.tce.smart.platform.core.mapper.PrintTemplateMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;
import static com.tce.smart.platform.service.print.PrintAccessPolicyTest.*;

public class PrintTemplateServiceTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private final ObjectMapper json = new ObjectMapper();
    DriverManagerDataSource source;
    JdbcTemplate jdbc;
    PrintTemplateService service;
    private volatile boolean rejectRender;

    @Before public void setup() throws Exception {
        source = new DriverManagerDataSource("jdbc:h2:file:" + temporary.newFolder().toPath().resolve("print") + ";MODE=Oracle;DB_CLOSE_ON_EXIT=FALSE", "sa", "");
        source.setDriverClassName("org.h2.Driver"); jdbc = new JdbcTemplate(source);
        createTestSchema();
        service = restartService(); loginManager();
    }
    @After public void close() { SecurityContextHolder.clearContext(); if (jdbc != null) shutdownDatabase(); }

    private void shutdownDatabase() { jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) connection -> { try (java.sql.Statement statement = connection.createStatement()) { statement.execute("SHUTDOWN"); } return null; }); }

    private PrintTemplateService restartService() throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean(); factory.setDataSource(source);
        factory.setMapperLocations(new org.springframework.core.io.Resource[]{new ClassPathResource("mapper/PrintTemplateMapper.xml")});
        SqlSessionFactory sessions = factory.getObject();
        PrintTemplateMapper mapper = new SqlSessionTemplate(sessions).getMapper(PrintTemplateMapper.class);
        PrintFeatureProperties props = properties();
        PrintAccessPolicy policy = new PrintAccessPolicy(props, null);
        PrintPublicationValidator renderer = (template, version) -> {
            if (rejectRender) throw new PrintApiException(422, "RENDER_VALIDATION_FAILED", "合成测试渲染失败");
            return Collections.singletonMap("status", "READY");
        };
        return new PrintTemplateService(mapper, policy, new PrintTemplateValidator(policy, props), renderer, new DataSourceTransactionManager(source));
    }

    PrintTemplateRequest draft(String face) throws Exception {
        return json.readValue("{\"parkId\":\"1\",\"name\":\"测试厂牌\",\"printItemType\":\"STAFF_CARD\",\"personType\":\"EMPLOYEE\",\"classificationCode\":\"STAFF_DEFAULT\",\"faceRole\":\"" + face + "\",\"sideCount\":1,\"layoutJson\":{\"schemaVersion\":1,\"faceRole\":\"" + face + "\",\"sideCount\":1,\"schemas\":[[]]},\"fieldSchemaJson\":{\"fields\":[]},\"pageSpecJson\":{\"widthMm\":85.6,\"heightMm\":53.98,\"orientation\":\"LANDSCAPE\",\"maxPageCount\":1},\"resourceManifest\":[]}", PrintTemplateRequest.class);
    }
    String create(String face) throws Exception { return (String) service.create(null, draft(face)).get("templateId"); }
    private String publish(String id, String key) {
        Map<String, Object> template = service.detail(id);
        PrintPublishRequest request = new PrintPublishRequest(); request.setDraftRevision(((Number) template.get("draftRevision")).longValue()); request.setDraftVersionId((String) template.get("currentDraftVersionId"));
        return (String) service.publish(id, request, key).getData().get("templateVersionId");
    }
    private PrintPairRequest pair(String front, String back) {
        PrintPairRequest pair = new PrintPairRequest(); pair.setName("测试组合"); pair.setPrintItemType("STAFF_CARD"); pair.setPersonType("EMPLOYEE"); pair.setClassificationCode("STAFF_DEFAULT"); pair.setFrontTemplateVersionId(front); pair.setBackTemplateVersionId(back); return pair;
    }

    @Test public void savesChineseComponentNameWithoutRelaxingBusinessKeys() throws Exception {
        PrintTemplateRequest request=draft("FRONT");
        com.fasterxml.jackson.databind.node.ArrayNode page=(com.fasterxml.jackson.databind.node.ArrayNode)request.getLayoutJson().path("schemas").get(0);
        com.fasterxml.jackson.databind.node.ObjectNode component=page.addObject().put("name","员工姓名").put("type","text").put("content","测试").put("readOnly",true).put("width",40).put("height",10);
        component.putObject("position").put("x",2).put("y",2);
        String id=(String)service.create(null,request).get("templateId");
        assertTrue(PrintJson.canonical(service.detail(id)).contains("员工姓名"));
        expectCode("TEMPLATE_VALIDATION_FAILED",()->PrintTemplateValidator.key("任意分类"));
    }

    @Test public void auditStoresTheHttpRequestIdReturnedToOperator() throws Exception {
        org.springframework.test.web.servlet.MockMvc mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(new com.tce.smart.platform.controller.print.PrintTemplateController(service))
            .setControllerAdvice(new com.tce.smart.platform.controller.print.PrintApiAdvice()).addFilters(new com.tce.smart.platform.controller.print.PrintRequestFilter()).build();
        org.springframework.mock.web.MockHttpServletResponse response=mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/print/v1/templates").servletPath("/print/v1/templates").contentType("application/json").content(PrintJson.canonical(draft("FRONT"))))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated()).andReturn().getResponse();
        String requestId=response.getHeader("X-Request-Id"); assertNotNull(requestId);
        String id=PrintJson.read(response.getContentAsString()).at("/data/templateId").asText();
        String details=jdbc.queryForObject("SELECT DETAILS_JSON FROM SMT_PRINT_AUDIT WHERE OBJECT_ID=?",String.class,id);
        assertEquals(requestId,PrintJson.read(details).path("requestId").asText());
    }

    @Test public void preservesPublishedContentWhileDraftChangesAndRejectsStaleRevision() throws Exception {
        String id = create("FRONT"); String published = publish(id, "publish-1");
        PrintTemplateRequest change = draft("FRONT"); change.setName("新草稿"); change.setDraftRevision(0L);
        service.save(id, change);
        expectCode("DRAFT_REVISION_CONFLICT", () -> service.save(id, change));
        assertEquals(1L, ((Number) service.detail(id).get("draftRevision")).longValue());
        assertEquals(published, service.detail(id).get("currentPublishedVersionId"));
        assertEquals(0L, jdbc.queryForObject("SELECT DRAFT_REVISION FROM SMT_PRINT_TEMPLATE_VER WHERE TEMPLATE_VERSION_ID = ?", Long.class, published).longValue());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_TEMPLATE_VER WHERE VERSION_STATUS='PUBLISHED'", Integer.class).intValue());
    }

    @Test public void pairRetainsExactVersionsAcrossServiceAndDatabaseRestart() throws Exception {
        String frontId = create("FRONT"), backId = create("BACK");
        String front1 = publish(frontId, "front-1"), back1 = publish(backId, "back-1");
        String pairId = (String) service.createPair(null, pair(front1, back1), "pair-1").getData().get("pairId");
        String front2 = publish(frontId, "front-2"); assertNotEquals(front1, front2);
        shutdownDatabase(); service = restartService();
        Map<String, Object> actual = service.pairDetail(pairId);
        assertEquals(front1, actual.get("frontTemplateVersionId")); assertEquals(back1, actual.get("backTemplateVersionId"));
        assertEquals(0L, ((Number) actual.get("revision")).longValue());
    }

    @Test public void idempotentPublicationReplaysOriginalResponseAndRejectsChangedCommand() throws Exception {
        String id = create("FRONT"); String version = publish(id, "once");
        assertEquals(version, publish(id, "once"));
        PrintPublishRequest changed = new PrintPublishRequest(); changed.setDraftRevision(99L); changed.setDraftVersionId((String) service.detail(id).get("currentDraftVersionId"));
        expectCode("IDEMPOTENCY_KEY_REUSED", () -> service.publish(id, changed, "once"));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_TEMPLATE_VER WHERE VERSION_STATUS='PUBLISHED'", Integer.class).intValue());
    }

    @Test public void renderFailureRollsBackVersionPointerAuditAndIdempotencyRecord() throws Exception {
        String id = create("FRONT"); rejectRender = true;
        expectCode("RENDER_VALIDATION_FAILED", () -> publish(id, "failed-render"));
        assertNull(service.detail(id).get("currentPublishedVersionId"));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_TEMPLATE_VER WHERE VERSION_STATUS='PUBLISHED'", Integer.class).intValue());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_OPERATION", Integer.class).intValue());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_AUDIT", Integer.class).intValue());
    }

    @Test public void rollbackChecksCurrentPointerAndDoesNotMutateVersionsOrPairs() throws Exception {
        String id = create("FRONT"); String first = publish(id, "first"), second = publish(id, "second");
        PrintRollbackRequest req = new PrintRollbackRequest(); req.setTargetVersionId(first); req.setExpectedPublishedVersionId(first); req.setReason("测试回滚");
        expectCode("PUBLISHED_POINTER_CONFLICT", () -> service.rollback(id, req, "rollback-conflict"));
        req.setExpectedPublishedVersionId(second); service.rollback(id, req, "rollback-ok");
        assertEquals(first, service.detail(id).get("currentPublishedVersionId"));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_TEMPLATE_VER WHERE VERSION_STATUS='PUBLISHED'", Integer.class).intValue());
    }

    @Test public void pairRequiresCompatiblePublishedRolesAndRevision() throws Exception {
        String front = publish(create("FRONT"), "front"), back = publish(create("BACK"), "back");
        expectCode("TEMPLATE_VALIDATION_FAILED", () -> service.createPair(null, pair(front, front), "wrong-face"));
        String id = (String) service.createPair(null, pair(front, back), "pair").getData().get("pairId");
        PrintPairRequest change = pair(front, back); change.setRevision(0L);
        service.savePair(id, change, "pair-save");
        expectCode("PAIR_REVISION_CONFLICT", () -> service.savePair(id, change, "pair-stale"));
        assertEquals(1L, ((Number) service.pairDetail(id).get("revision")).longValue());
    }

    @Test public void rejectsCrossParkDetailsAndInvalidSinglePageDimensionsFields() throws Exception {
        String id = create("FRONT"); login(2, "test:print:read");
        expectCode("PRINT_SCOPE_DENIED", () -> service.detail(id)); loginManager();
        PrintTemplateRequest bad = draft("FRONT"); bad.setSideCount(2);
        expectCode("TEMPLATE_VALIDATION_FAILED", () -> service.create(null, bad));
        bad.setSideCount(1); bad.setLayoutJson(json.readTree("{\"faceRole\":\"FRONT\",\"sideCount\":1,\"schemas\":[[],[]]}"));
        expectCode("TEMPLATE_VALIDATION_FAILED", () -> service.create(null, bad));
        PrintTemplateRequest size = draft("FRONT"); size.setPageSpecJson(json.readTree("{\"widthMm\":-1,\"heightMm\":53.98,\"orientation\":\"LANDSCAPE\",\"maxPageCount\":1}"));
        expectCode("TEMPLATE_VALIDATION_FAILED", () -> service.create(null, size));
        PrintTemplateRequest fields = draft("FRONT"); fields.setFieldSchemaJson(json.readTree("{\"fields\":[{\"name\":\"password\",\"required\":true}]}"));
        expectCode("TEMPLATE_VALIDATION_FAILED", () -> service.create(null, fields));
    }

    @Test public void twoConcurrentSavesAllowExactlyOneExpectedRevision() throws Exception {
        String id = create("FRONT"); ExecutorService pool = Executors.newFixedThreadPool(2); CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        Callable<String> save = () -> {
            loginManager(); PrintTemplateRequest change = draft("FRONT"); change.setDraftRevision(0L); ready.countDown(); start.await();
            try { service.save(id, change); return "saved"; } catch (PrintApiException e) { return e.getCode(); } finally { SecurityContextHolder.clearContext(); }
        };
        try {
            Future<String> a = pool.submit(save), b = pool.submit(save); assertTrue(ready.await(5, TimeUnit.SECONDS)); start.countDown();
            List<String> results = Arrays.asList(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS));
            assertTrue(results.contains("saved")); assertTrue(results.contains("DRAFT_REVISION_CONFLICT"));
        } finally { pool.shutdownNow(); }
    }

    @Test public void auditFailureRollsBackPublishedVersionAndPointer() throws Exception {
        String templateId=create("FRONT"); fixtureAuditFailure();
        try { publish(templateId,"audit-failure"); fail("审计失败应回滚发布"); } catch(org.springframework.dao.DataAccessException expected) { }
        assertNull(service.detail(templateId).get("currentPublishedVersionId"));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_TEMPLATE_VER WHERE VERSION_STATUS='PUBLISHED'",Integer.class).intValue());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_OPERATION",Integer.class).intValue());
    }
    private void fixtureAuditFailure() { jdbc.execute("ALTER TABLE SMT_PRINT_AUDIT ADD CONSTRAINT TEST_REJECT_PUBLISH CHECK (ACTION <> 'TEMPLATE_PUBLISHED')"); }

    @Test public void concurrentSameKeyPublishesOneImmutableVersion() throws Exception {
        String templateId=create("FRONT"); ExecutorService pool=Executors.newFixedThreadPool(2); CountDownLatch ready=new CountDownLatch(2),start=new CountDownLatch(1);
        Callable<String> action=()->{loginManager(); ready.countDown(); start.await(); try{return publish(templateId,"concurrent-once");}finally{SecurityContextHolder.clearContext();}};
        try {Future<String>a=pool.submit(action),b=pool.submit(action); assertTrue(ready.await(5,TimeUnit.SECONDS)); start.countDown(); assertEquals(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS)); assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_TEMPLATE_VER WHERE VERSION_STATUS='PUBLISHED'",Integer.class).intValue());}
        finally{pool.shutdownNow();}
    }

    @Test public void timestampsRemainStableWhenServiceTimezoneChanges() throws Exception {
        String templateId=create("FRONT"); Object createdAt=service.detail(templateId).get("createdAt"); TimeZone previous=TimeZone.getDefault();
        try {TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland")); shutdownDatabase(); service=restartService(); assertEquals(createdAt,service.detail(templateId).get("createdAt"));}
        finally {TimeZone.setDefault(previous);}
    }

    private void createTestSchema() {
        // 测试专用 H2 文件库，不是 Oracle 发布迁移或业务数据库初始化脚本。
        jdbc.execute("CREATE TABLE SMT_PRINT_TEMPLATE (TEMPLATE_ID VARCHAR(36) PRIMARY KEY, PARK_ID VARCHAR(64), TEMPLATE_KEY VARCHAR(64), NAME VARCHAR(100), PRINT_ITEM_TYPE VARCHAR(64), PERSON_TYPE VARCHAR(64), CLASSIFICATION_CODE VARCHAR(64), FACE_ROLE VARCHAR(10), LIFECYCLE_STATUS VARCHAR(16), CURRENT_DRAFT_VERSION_ID VARCHAR(36), CURRENT_PUBLISHED_VERSION_ID VARCHAR(36), DRAFT_REVISION BIGINT, CREATED_BY VARCHAR(64), CREATED_AT TIMESTAMP, UPDATED_BY VARCHAR(64), UPDATED_AT TIMESTAMP, ARCHIVED_AT TIMESTAMP, UNIQUE(PARK_ID,TEMPLATE_KEY))");
        jdbc.execute("CREATE TABLE SMT_PRINT_TEMPLATE_VER (TEMPLATE_VERSION_ID VARCHAR(36) PRIMARY KEY, TEMPLATE_ID VARCHAR(36), PARK_ID VARCHAR(64), VERSION_NO BIGINT, VERSION_STATUS VARCHAR(16), FACE_ROLE VARCHAR(10), SIDE_COUNT INTEGER, LAYOUT_JSON CLOB, FIELD_SCHEMA_JSON CLOB, RESOURCE_MANIFEST_JSON CLOB, PAGE_SPEC_JSON CLOB, VALIDATION_REPORT_JSON CLOB, CONTENT_HASH VARCHAR(71), DRAFT_REVISION BIGINT, PUBLISHED_AT TIMESTAMP, PUBLISHED_BY VARCHAR(64), CREATED_AT TIMESTAMP, CREATED_BY VARCHAR(64), UNIQUE(TEMPLATE_ID,VERSION_NO))");
        jdbc.execute("CREATE TABLE SMT_PRINT_TEMPLATE_PAIR (PAIR_ID VARCHAR(36) PRIMARY KEY, PARK_ID VARCHAR(64), NAME VARCHAR(100), PRINT_ITEM_TYPE VARCHAR(64), PERSON_TYPE VARCHAR(64), CLASSIFICATION_CODE VARCHAR(64), FRONT_TEMPLATE_VERSION_ID VARCHAR(36), BACK_TEMPLATE_VERSION_ID VARCHAR(36), REVISION BIGINT, STATUS VARCHAR(16), CREATED_BY VARCHAR(64), CREATED_AT TIMESTAMP, UPDATED_BY VARCHAR(64), UPDATED_AT TIMESTAMP, ARCHIVED_AT TIMESTAMP)");
        jdbc.execute("CREATE TABLE SMT_PRINT_OPERATION (OPERATION_ID VARCHAR(36) PRIMARY KEY, PRINCIPAL_ID VARCHAR(64), IDEMPOTENCY_KEY VARCHAR(128), BODY_HASH VARCHAR(71), RESPONSE_JSON CLOB, CREATED_AT TIMESTAMP, UNIQUE(PRINCIPAL_ID,IDEMPOTENCY_KEY))");
        jdbc.execute("CREATE TABLE SMT_PRINT_AUDIT (AUDIT_ID VARCHAR(36) PRIMARY KEY, PARK_ID VARCHAR(64), ACTOR_ID VARCHAR(64), ACTION VARCHAR(64), OBJECT_ID VARCHAR(36), DETAILS_JSON CLOB, CREATED_AT TIMESTAMP)");
    }
}
