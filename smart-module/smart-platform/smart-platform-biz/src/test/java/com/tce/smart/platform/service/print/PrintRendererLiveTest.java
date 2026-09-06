package com.tce.smart.platform.service.print;

import com.tce.smart.platform.core.entity.print.PrintTemplateVersion;
import org.junit.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.Assert.*;
import static com.tce.smart.platform.service.print.PrintAccessPolicyTest.*;

/** 显式开启时验证真实 Java HTTP 客户端与本任务 Node 渲染器；不连接业务数据库。 */
public class PrintRendererLiveTest {
    @Test public void rendersSingleBackUsingPrivateNodeServiceAndRealChineseFont() throws Exception {
        Assume.assumeTrue(Boolean.getBoolean("print.renderer.live"));
        Path rendererRoot=Paths.get("../../..").toAbsolutePath().normalize().resolve("smart-print-renderer");
        String token=UUID.randomUUID().toString()+UUID.randomUUID().toString();
        ProcessBuilder command=new ProcessBuilder("node","--input-type=module","-e","import {createRenderServer} from './src/server.mjs'; const s=createRenderServer({token:process.env.PRINT_RENDERER_TOKEN,concurrency:1}); s.listen(0,'127.0.0.1',()=>console.log(s.address().port));");
        command.directory(rendererRoot.toFile()); command.environment().put("PRINT_RENDERER_TOKEN",token); command.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process=command.start(); ExecutorService reader=Executors.newSingleThreadExecutor();
        try {
            Future<String> portLine=reader.submit(()->new BufferedReader(new InputStreamReader(process.getInputStream())).readLine()); String port=portLine.get(20,TimeUnit.SECONDS); assertNotNull("渲染器未启动",port);
            loginManager(); PrintFeatureProperties p=properties(); p.setRendererUrl("http://127.0.0.1:"+Integer.parseInt(port)); p.setRendererToken(token); p.setRendererReadTimeoutMs(60000);
            PrintTemplateVersion version=new PrintTemplateVersion(); version.setTemplateId(UUID.randomUUID().toString()); version.setParkId("1"); version.setVersionStatus("DRAFT"); version.setDraftRevision(0L); version.setFaceRole("BACK"); version.setSideCount(1);
            version.setLayoutJson("{\"schemaVersion\":1,\"faceRole\":\"BACK\",\"sideCount\":1,\"schemas\":[[{\"name\":\"name_1\",\"type\":\"text\",\"content\":\"\",\"position\":{\"x\":5,\"y\":5},\"width\":60,\"height\":12,\"fontName\":\"NotoSansSC\",\"fontSize\":12}]]}");
            version.setPageSpecJson("{\"widthMm\":85.6,\"heightMm\":53.98,\"orientation\":\"LANDSCAPE\",\"maxPageCount\":1}"); version.setFieldSchemaJson("{\"fields\":[{\"key\":\"staffName\",\"schemaName\":\"name_1\",\"required\":true}]}"); version.setResourceManifestJson("[]");
            com.fasterxml.jackson.databind.JsonNode result=new PrintRendererClient(p,new PrintAccessPolicy(p,null)).renderPreview(UUID.randomUUID().toString(),"STAFF_CARD",Collections.singletonList(version),PrintJson.object().put("staffName","合成测试"));
            assertEquals("READY",result.path("status").asText()); assertEquals("BACK",result.at("/artifacts/0/face").asText()); assertEquals(1,result.at("/artifacts/0/pageCount").asInt()); assertTrue(result.at("/artifacts/0/bytes").asInt()>1000);
        } finally { process.destroy(); if(!process.waitFor(5,TimeUnit.SECONDS)) process.destroyForcibly(); reader.shutdownNow(); org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
    }
}
