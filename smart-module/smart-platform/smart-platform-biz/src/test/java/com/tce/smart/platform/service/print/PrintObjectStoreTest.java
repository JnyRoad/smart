package com.tce.smart.platform.service.print;

import com.tce.smart.platform.core.mapper.PrintObjectMapper;
import com.tce.smart.platform.controller.print.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.*;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.ByteArrayInputStream;
import java.util.*;
import static org.junit.Assert.*;
import static com.tce.smart.platform.service.print.PrintAccessPolicyTest.*;

/** 隔离文件库验证真实BLOB及元数据；不是Oracle或现场文件服务验收。 */
public class PrintObjectStoreTest {
    private PrintTemplateServiceTest fixture;
    private PrintObjectMapper mapper;
    private SqlPrintObjectStore store;
    private PrintResourceService service;
    private final byte[] png = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/mWQAAAAASUVORK5CYII=");
    @Before public void setup() throws Exception {
        fixture=new PrintTemplateServiceTest(); fixture.temporary.create(); fixture.setup();
        fixture.jdbc.execute("CREATE TABLE SMT_PRINT_OBJECT (OBJECT_ID VARCHAR(36) PRIMARY KEY,PARK_ID VARCHAR(36),CREATED_BY VARCHAR(64),PURPOSE VARCHAR(32),ACCESS_SCOPE VARCHAR(32),OWNER_ID VARCHAR(36),CONTENT_HASH VARCHAR(80),MEDIA_TYPE VARCHAR(80),SIZE_BYTES BIGINT,CREATED_AT TIMESTAMP,CONTENT_BYTES BLOB)");
        SqlSessionFactoryBean factory=new SqlSessionFactoryBean(); factory.setDataSource(fixture.source); factory.setMapperLocations(new Resource[]{new ClassPathResource("mapper/PrintObjectMapper.xml")});
        mapper=new SqlSessionTemplate(factory.getObject()).getMapper(PrintObjectMapper.class); store=new SqlPrintObjectStore(mapper);
        service=new PrintResourceService(new PrintAccessPolicy(properties(),store),store); loginManager();
    }
    @After public void close() { fixture.close(); fixture.temporary.delete(); }
    @Test public void authorizedImageSurvivesServiceRecreationAndHashCorruptionIsRejected() throws Exception {
        PrintResourceStore.RegisteredResource resource=service.upload("1","image/png","BACKGROUND",new ByteArrayInputStream(png));
        assertEquals(PrintJson.hashBytes(png),resource.getContentHash());
        SqlPrintObjectStore reloaded=new SqlPrintObjectStore(mapper);
        PrintResourceService restarted=new PrintResourceService(new PrintAccessPolicy(properties(),reloaded),reloaded);
        assertArrayEquals(png,restarted.download(resource.getObjectId(),"1").getBytes());
        fixture.jdbc.update("UPDATE SMT_PRINT_OBJECT SET CONTENT_BYTES=? WHERE OBJECT_ID=?",new byte[]{1},resource.getObjectId());
        expectCode("PRINT_RESOURCE_HASH_MISMATCH",()->restarted.download(resource.getObjectId(),"1"));
    }
    @Test public void otherParkCannotReadAnUploadedImage() throws Exception {
        String id=service.upload("1","image/png","LOGO",new ByteArrayInputStream(png)).getObjectId();
        login(2,"test:print:resource"); expectCode("PRINT_SCOPE_DENIED",()->service.download(id,"2"));
    }
    @Test public void templateUploadCannotInventPersonPhotoPermission() throws Exception {
        expectUpload("PRINT_SCOPE_DENIED","image/png","PHOTO",png);
        expectUpload("TEMPLATE_VALIDATION_FAILED","image/png","LOGO",new byte[]{1,2,3});
        expectUpload("TEMPLATE_VALIDATION_FAILED","image/jpeg","LOGO",png);
        assertEquals(0,fixture.jdbc.queryForObject("SELECT COUNT(*) FROM SMT_PRINT_OBJECT",Integer.class).intValue());
    }
    @Test public void previewObjectCannotBeReadViaTemplateResourceEndpoint() {
        PrintPreviewArtifactStore.Batch batch=store.stage(UUID.randomUUID().toString(),"1","7");
        String id=store.write(batch,UUID.randomUUID().toString(),"%PDF-test".getBytes(),PrintJson.hashBytes("%PDF-test".getBytes())); store.commit(batch);
        expectCode("PRINT_SCOPE_DENIED",()->service.download(id,"1"));
    }
    @Test public void resourceHttpContractPreservesBinaryHeadersAndDeniesOtherPark() throws Exception {
        MockMvc mvc=MockMvcBuilders.standaloneSetup(new PrintResourceController(service)).setControllerAdvice(new PrintApiAdvice()).addFilters(new PrintRequestFilter()).build();
        String response=mvc.perform(post("/print/v1/resources").servletPath("/print/v1/resources").param("parkId","1").contentType("image/png").content(png))
            .andExpect(status().isCreated()).andExpect(header().exists("X-Request-Id")).andExpect(jsonPath("$.code").value(0)).andExpect(jsonPath("$.data.accessScope").value("TEMPLATE")).andReturn().getResponse().getContentAsString();
        String id=PrintJson.read(response).path("data").path("objectId").asText();
        mvc.perform(get("/print/v1/resources/"+id).servletPath("/print/v1/resources/"+id).param("parkId","1"))
            .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store")).andExpect(header().string("X-Artifact-Sha256",PrintJson.hashBytes(png))).andExpect(content().bytes(png));
        login(2,"test:print:resource");
        mvc.perform(get("/print/v1/resources/"+id).param("parkId","2")).andExpect(status().isForbidden()).andExpect(jsonPath("$.error.code").value("PRINT_SCOPE_DENIED"));
    }
    @Test public void imageUploadUsesImageLimitInsteadOfJsonBodyLimit() throws Exception {
        java.awt.image.BufferedImage image=new java.awt.image.BufferedImage(1024,1024,java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Random random=new Random(42); for(int y=0;y<1024;y++) for(int x=0;x<1024;x++) image.setRGB(x,y,random.nextInt());
        java.io.ByteArrayOutputStream output=new java.io.ByteArrayOutputStream(); javax.imageio.ImageIO.write(image,"png",output); byte[] largePng=output.toByteArray();
        assertTrue(largePng.length>3*1024*1024);
        MockMvc mvc=MockMvcBuilders.standaloneSetup(new PrintResourceController(service)).setControllerAdvice(new PrintApiAdvice()).addFilters(new PrintRequestFilter()).build();
        mvc.perform(post("/print/v1/resources").servletPath("/print/v1/resources").param("parkId","1").contentType("image/png").content(largePng))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.data.sizeBytes").value(largePng.length));
    }
    private void expectUpload(String code,String type,String purpose,byte[] bytes) throws Exception {
        try { service.upload("1",type,purpose,new ByteArrayInputStream(bytes)); fail("应拒绝不合法资源"); }
        catch(PrintApiException error) { assertEquals(code,error.getCode()); }
    }
}
