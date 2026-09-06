package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.JsonNode;
import com.tce.smart.platform.api.dto.req.print.*;
import com.tce.smart.platform.controller.print.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.tce.smart.platform.core.mapper.PrintPreviewMapper;
import com.tce.smart.platform.core.mapper.PrintObjectMapper;
import com.tce.smart.platform.core.mapper.PrintTemplateMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.*;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import java.io.ByteArrayOutputStream;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static com.tce.smart.platform.service.print.PrintAccessPolicyTest.*;

public class PrintPreviewServiceTest {
    private PrintTemplateServiceTest fixture;
    private PrintPreviewMapper previews;
    private PrintTemplateMapper templates;
    private PrintAccessPolicy policy;
    private PrintTemplateValidator validator;
    private PrintRendererClient renderer;
    private MemoryArtifacts artifacts;
    private byte[] pdf;
    @Before public void setup() throws Exception {
        fixture = new PrintTemplateServiceTest(); fixture.temporary.create(); fixture.setup();
        fixture.jdbc.execute("CREATE TABLE SMT_PRINT_PREVIEW (PREVIEW_ID VARCHAR(36) PRIMARY KEY, PARK_ID VARCHAR(64), CREATED_BY VARCHAR(64), CREATED_AT TIMESTAMP, STATUS VARCHAR(32), DETAILS_JSON CLOB)");
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean(); factory.setDataSource(fixture.source); factory.setMapperLocations(new Resource[]{new ClassPathResource("mapper/PrintTemplateMapper.xml"),new ClassPathResource("mapper/PrintPreviewMapper.xml")});
        SqlSessionTemplate sessions = new SqlSessionTemplate(factory.getObject()); previews = sessions.getMapper(PrintPreviewMapper.class); templates = sessions.getMapper(PrintTemplateMapper.class);
        PrintFeatureProperties properties = properties(); properties.getPermissions().put("preview", "test:print:preview");
        login(1, "test:print:read", "test:print:write", "test:print:publish", "test:print:preview", "test:print:resource");
        policy = new PrintAccessPolicy(properties, null); validator = new PrintTemplateValidator(policy, properties); renderer = mock(PrintRendererClient.class); artifacts = new MemoryArtifacts();
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) { document.addPage(new PDPage(new PDRectangle(242.64567f, 153.01418f))); document.save(out); pdf = out.toByteArray(); }
        when(renderer.renderPreview(anyString(), anyString(), anyList(), any())).thenAnswer(invocation -> {
            Map<String,Object> artifact = new LinkedHashMap<>(); artifact.put("artifactId", UUID.randomUUID().toString()); artifact.put("face", "FRONT"); artifact.put("mediaType", "application/pdf"); artifact.put("sha256", PrintJson.hashBytes(pdf)); artifact.put("bytes", pdf.length); artifact.put("contentBase64", Base64.getEncoder().encodeToString(pdf)); artifact.put("pageCount", 1); artifact.put("widthMm",85.6); artifact.put("heightMm",53.98);
            Map<String,Object> result = new LinkedHashMap<>(); result.put("status","READY"); result.put("previewId",invocation.getArgument(0)); result.put("artifacts",Collections.singletonList(artifact)); return PrintJson.tree(result);
        });
    }
    @After public void cleanup() { fixture.close(); fixture.temporary.delete(); }
    private PrintPreviewService service(PrintPreviewArtifactStore store) { return new PrintPreviewService(templates,previews,policy,validator,renderer,store,new DataSourceTransactionManager(fixture.source)); }
    private PrintPreviewRequest request(String templateId) { PrintPreviewRequest request = new PrintPreviewRequest(); request.setVersionId((String)fixture.service.detail(templateId).get("currentDraftVersionId")); request.setSampleData(PrintJson.object()); return request; }
    @Test public void missingArtifactStoreFailsBeforeCreatingPreviewMetadata() throws Exception {
        String id = fixture.create("FRONT"); PrintPreviewRequest request = request(id);
        expectCode("PRINT_ARTIFACT_STORE_NOT_CONFIGURED", () -> service(null).templatePreview(id,request));
        assertEquals(0,fixture.jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_PREVIEW",Integer.class).intValue());
    }
    @Test public void previewReadsFrozenArtifactAfterServiceRecreationAndRejectsOtherPark() throws Exception {
        String id = fixture.create("FRONT"); PrintPreviewService service = service(artifacts);
        Map<String,Object> created = service.templatePreview(id,request(id)); String previewId = (String)created.get("previewId");
        assertEquals("READY",created.get("status")); assertFalse(created.toString().contains("contentBase64"));
        Map<String,Object> loaded = service(artifacts).detail(previewId); List<?> faces = (List<?>)loaded.get("artifacts"); String artifactId = (String)((Map<?,?>)faces.get(0)).get("artifactId");
        assertArrayEquals(pdf,service(artifacts).readArtifact(previewId,artifactId));
        login(2,"test:print:preview","test:print:resource");
        expectCode("PRINT_SCOPE_DENIED", () -> service.detail(previewId));
    }
    @Test public void rejectsForeignVersionAndUncontrolledSubjectData() throws Exception {
        String a=fixture.create("FRONT"), b=fixture.create("FRONT"); PrintPreviewRequest foreign=request(b);
        expectCode("TEMPLATE_VALIDATION_FAILED",()->service(artifacts).templatePreview(a,foreign));
        PrintPreviewRequest body=request(a); body.setSampleData(PrintJson.object().put("subjectId","real-person-id"));
        expectCode("PRINT_SCOPE_DENIED",()->service(artifacts).templatePreview(a,body));
    }
    @Test public void objectBytesRollBackWhenPreviewMetadataCannotBeSaved() throws Exception {
        fixture.jdbc.execute("CREATE TABLE SMT_PRINT_OBJECT (OBJECT_ID VARCHAR(36) PRIMARY KEY,PARK_ID VARCHAR(36),CREATED_BY VARCHAR(64),PURPOSE VARCHAR(32),ACCESS_SCOPE VARCHAR(32),OWNER_ID VARCHAR(36),CONTENT_HASH VARCHAR(80),MEDIA_TYPE VARCHAR(80),SIZE_BYTES BIGINT,CREATED_AT TIMESTAMP,CONTENT_BYTES BLOB)");
        SqlSessionFactoryBean factory=new SqlSessionFactoryBean(); factory.setDataSource(fixture.source); factory.setMapperLocations(new Resource[]{new ClassPathResource("mapper/PrintObjectMapper.xml")});
        SqlPrintObjectStore sqlStore=new SqlPrintObjectStore(new SqlSessionTemplate(factory.getObject()).getMapper(PrintObjectMapper.class));
        String id=fixture.create("FRONT");
        fixture.jdbc.execute("ALTER TABLE SMT_PRINT_PREVIEW ADD CONSTRAINT REJECT_READY CHECK (STATUS <> 'READY')");
        try { service(sqlStore).templatePreview(id,request(id)); fail("应由数据库约束拒绝元数据保存"); }
        catch(org.springframework.dao.DataAccessException expected) { }
        assertEquals(0,fixture.jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_OBJECT",Integer.class).intValue());
        assertEquals(0,fixture.jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_PREVIEW",Integer.class).intValue());
    }
    @Test public void stagedExternalArtifactsAbortWhenPreviewMetadataCannotBeSaved() throws Exception {
        String id=fixture.create("FRONT");
        fixture.jdbc.execute("ALTER TABLE SMT_PRINT_PREVIEW ADD CONSTRAINT REJECT_READY CHECK (STATUS <> 'READY')");
        try { service(artifacts).templatePreview(id,request(id)); fail("应由数据库约束拒绝元数据保存"); }
        catch(org.springframework.dao.DataAccessException expected) { }
        assertTrue(artifacts.objects.isEmpty()); assertEquals(1,artifacts.aborts);
    }
    @Test public void pairPreviewFreezesBothPublishedVersionsAndRejectsStaleRevision() throws Exception {
        String front=publish(fixture.create("FRONT")),back=publish(fixture.create("BACK"));
        PrintPairRequest pair=new PrintPairRequest(); pair.setName("两面预览"); pair.setPrintItemType("STAFF_CARD"); pair.setPersonType("EMPLOYEE"); pair.setClassificationCode("STAFF_DEFAULT"); pair.setFrontTemplateVersionId(front); pair.setBackTemplateVersionId(back);
        String id=(String)fixture.service.createPair(null,pair,"preview-pair").getData().get("pairId");
        when(renderer.renderPreview(anyString(),anyString(),anyList(),any())).thenAnswer(invocation->{
            List<com.tce.smart.platform.core.entity.print.PrintTemplateVersion> versions=invocation.getArgument(2);
            assertEquals(Arrays.asList(front,back),Arrays.asList(versions.get(0).getTemplateVersionId(),versions.get(1).getTemplateVersionId()));
            Map<String,Object> result=new LinkedHashMap<>(); result.put("artifacts",Arrays.asList(artifact("FRONT",pdf,1),artifact("BACK",pdf,1)));
            try(PDDocument document=new PDDocument(); ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                document.addPage(new PDPage(new PDRectangle(242.64567f,153.01418f))); document.addPage(new PDPage(new PDRectangle(242.64567f,153.01418f))); document.save(out);
                result.put("combinedArtifact",artifact("COMBINED",out.toByteArray(),2));
            }
            return PrintJson.tree(result);
        });
        PrintPreviewRequest body=new PrintPreviewRequest(); body.setRevision(0L); body.setSampleData(PrintJson.object());
        Map<String,Object> created=service(artifacts).pairPreview(id,body);
        assertEquals(2,created.get("sideCount")); assertEquals(2,((List<?>)created.get("artifacts")).size());
        Map<?,?> combined=(Map<?,?>)created.get("combinedArtifact");
        try(PDDocument document=PDDocument.load(service(artifacts).readArtifact((String)created.get("previewId"),(String)combined.get("artifactId")))) { assertEquals(2,document.getNumberOfPages()); }
        pair.setRevision(0L); fixture.service.savePair(id,pair,"updated-pair");
        assertEquals(0,((Number)service(artifacts).detail((String)created.get("previewId")).get("pairRevision")).intValue());
        expectCode("PAIR_REVISION_CONFLICT",()->service(artifacts).pairPreview(id,body));
    }
    @Test public void pairPreviewAcceptsNumericallyEqualIntegerAndDecimalDimensions() throws Exception {
        PrintTemplateRequest frontBody=fixture.draft("FRONT"),backBody=fixture.draft("BACK");
        ((com.fasterxml.jackson.databind.node.ObjectNode)frontBody.getPageSpecJson()).put("heightMm",54);
        ((com.fasterxml.jackson.databind.node.ObjectNode)backBody.getPageSpecJson()).put("heightMm",54.0);
        String front=publish((String)fixture.service.create(null,frontBody).get("templateId"));
        String back=publish((String)fixture.service.create(null,backBody).get("templateId"));
        PrintPairRequest pair=new PrintPairRequest(); pair.setName("相同物理尺寸"); pair.setPrintItemType("STAFF_CARD"); pair.setPersonType("EMPLOYEE"); pair.setClassificationCode("STAFF_DEFAULT"); pair.setFrontTemplateVersionId(front); pair.setBackTemplateVersionId(back);
        String id=(String)fixture.service.createPair(null,pair,"same-dimensions").getData().get("pairId");
        when(renderer.renderPreview(anyString(),anyString(),anyList(),any())).thenThrow(new PrintApiException(422,"RENDER_VALIDATION_FAILED","尺寸通过后测试渲染诊断"));
        PrintPreviewRequest body=new PrintPreviewRequest(); body.setRevision(0L);
        Map<String,Object> result=service(artifacts).pairPreview(id,body);
        assertEquals("RENDER_VALIDATION_FAILED",result.get("errorCode"));
        verify(renderer).renderPreview(anyString(),anyString(),anyList(),any());
    }

    @Test public void previewHttpEndpointCarriesRequestIdAndRechecksDownloadAuthorization() throws Exception {
        String id=fixture.create("FRONT");
        MockMvc mvc=MockMvcBuilders.standaloneSetup(new PrintPreviewController(service(artifacts))).setControllerAdvice(new PrintApiAdvice()).addFilters(new PrintRequestFilter()).build();
        String result=mvc.perform(post("/print/v1/templates/"+id+"/preview").servletPath("/print/v1/templates/"+id+"/preview").contentType("application/json").content(PrintJson.canonical(request(id))))
            .andExpect(status().isAccepted()).andExpect(header().exists("X-Request-Id")).andExpect(jsonPath("$.data.status").value("READY")).andReturn().getResponse().getContentAsString();
        JsonNode data=PrintJson.read(result).path("data"); String path="/print/v1/previews/"+data.path("previewId").asText()+"/artifacts/"+data.at("/artifacts/0/artifactId").asText();
        mvc.perform(get(path).servletPath(path)).andExpect(status().isOk()).andExpect(content().contentType("application/pdf")).andExpect(content().bytes(pdf));
        login(1,"test:print:preview");
        mvc.perform(get(path).servletPath(path)).andExpect(status().isForbidden()).andExpect(jsonPath("$.error.code").value("PRINT_PERMISSION_DENIED"));
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        mvc.perform(get("/print/v1/previews/"+data.path("previewId").asText())).andExpect(status().isUnauthorized());
    }
    @Test public void rendererUnavailableDoesNotPersistMisleadingFailedPreview() throws Exception {
        String id=fixture.create("FRONT");
        when(renderer.renderPreview(anyString(),anyString(),anyList(),any())).thenThrow(new PrintApiException(503,"PRINT_RENDERER_UNAVAILABLE","测试依赖不可用"));
        expectCode("PRINT_RENDERER_UNAVAILABLE",()->service(artifacts).templatePreview(id,request(id)));
        assertEquals(0,fixture.jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_PREVIEW",Integer.class).intValue());
    }

    private String publish(String id) {
        Map<String,Object> detail=fixture.service.detail(id); PrintPublishRequest body=new PrintPublishRequest(); body.setDraftRevision(((Number)detail.get("draftRevision")).longValue()); body.setDraftVersionId((String)detail.get("currentDraftVersionId"));
        return (String)fixture.service.publish(id,body,UUID.randomUUID().toString()).getData().get("templateVersionId");
    }
    private Map<String,Object> artifact(String face,byte[] bytes,int pageCount) {
        Map<String,Object> value=new LinkedHashMap<>(); value.put("artifactId",UUID.randomUUID().toString()); value.put("face",face); value.put("mediaType","application/pdf"); value.put("sha256",PrintJson.hashBytes(bytes)); value.put("bytes",bytes.length); value.put("contentBase64",Base64.getEncoder().encodeToString(bytes)); value.put("pageCount",pageCount); value.put("widthMm",85.6); value.put("heightMm",53.98); return value;
    }
    private static class MemoryArtifacts implements PrintPreviewArtifactStore {
        private final Map<String,byte[]> objects=new HashMap<>();
        private int aborts;
        private static class MemoryBatch implements Batch { private final Map<String,byte[]> staged=new HashMap<>(); }
        public Batch stage(String previewId,String parkId,String actorId) { return new MemoryBatch(); }
        public String write(Batch raw,String artifactId,byte[] bytes,String hash) { ((MemoryBatch)raw).staged.put(artifactId,bytes.clone()); return artifactId; }
        public void commit(Batch raw) { objects.putAll(((MemoryBatch)raw).staged); }
        public void abort(Batch raw) { aborts++; for(String id:((MemoryBatch)raw).staged.keySet()) objects.remove(id); }
        public byte[] read(String objectId) { return objects.get(objectId); }
    }
}
