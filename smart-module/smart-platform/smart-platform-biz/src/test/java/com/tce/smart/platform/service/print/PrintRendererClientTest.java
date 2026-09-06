package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.core.entity.print.PrintTemplate;
import com.tce.smart.platform.core.entity.print.PrintTemplateVersion;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.Base64;

import static org.junit.Assert.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static com.tce.smart.platform.service.print.PrintAccessPolicyTest.*;

public class PrintRendererClientTest {
    private final ObjectMapper json = new ObjectMapper();
    @After public void logout() { SecurityContextHolder.clearContext(); }

    private PrintTemplate template() {
        PrintTemplate t = new PrintTemplate(); t.setTemplateId("00000000-0000-4000-8000-000000000001"); t.setParkId("1"); t.setPrintItemType("STAFF_CARD"); t.setFaceRole("BACK"); return t;
    }
    private PrintTemplateVersion version() {
        PrintTemplateVersion v = new PrintTemplateVersion(); v.setTemplateId(template().getTemplateId()); v.setVersionStatus("DRAFT"); v.setDraftRevision(3L); v.setFaceRole("BACK"); v.setSideCount(1); v.setParkId("1");
        v.setLayoutJson("{\"schemaVersion\":1,\"faceRole\":\"BACK\",\"sideCount\":1,\"schemas\":[[]]}");
        v.setPageSpecJson("{\"widthMm\":85.6,\"heightMm\":53.98,\"orientation\":\"LANDSCAPE\",\"maxPageCount\":1}");
        v.setFieldSchemaJson("{\"fields\":[]}"); v.setResourceManifestJson("[]"); return v;
    }

    @Test public void missingRendererConfigNeverPermitsPublication() {
        loginManager(); PrintFeatureProperties p = properties();
        PrintRendererClient client = new PrintRendererClient(p, new PrintAccessPolicy(p, null), new RestTemplate());
        expectCode("PRINT_RENDERER_NOT_CONFIGURED", () -> client.validate(template(), version()));
    }

    @Test public void rejectsCleartextCredentialsAcrossHostsBeforeAnyHttpRequest() {
        loginManager(); PrintFeatureProperties p=properties(); p.setRendererUrl("http://192.168.10.99"); p.setRendererToken("synthetic-test-token");
        RestTemplate rest=new RestTemplate(); MockRestServiceServer server=MockRestServiceServer.bindTo(rest).build();
        expectCode("PRINT_RENDERER_NOT_CONFIGURED",()->new PrintRendererClient(p,new PrintAccessPolicy(p,null),rest).validate(template(),version()));
        server.verify();
    }

    @Test public void sendsAuthorizedSingleBackPreviewAndVerifiesRealPdfDimensions() throws Exception {
        runResponse(false, false);
    }
    @Test public void rejectsTamperedBytesDespiteReadyMetadata() throws Exception { runResponse(true, false); }
    @Test public void rejectsActualWrongPageSizeDespiteExpectedMetadata() throws Exception { runResponse(false, true); }

    @Test public void preservesRendererComponentViolationForDesigner() {
        loginManager(); PrintFeatureProperties p=properties(); p.setRendererUrl("http://127.0.0.1:19999"); p.setRendererToken("synthetic-test-token");
        RestTemplate rest=new RestTemplate(); MockRestServiceServer server=MockRestServiceServer.bindTo(rest).build();
        server.expect(requestTo("http://127.0.0.1:19999/internal/print-renderer/v1/render")).andRespond(withStatus(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY).contentType(MediaType.APPLICATION_JSON).body("{\"error\":{\"code\":\"RENDER_VALIDATION_FAILED\",\"details\":[{\"code\":\"TEXT_OVERFLOW\",\"schemaName\":\"name_1\"}]}}"));
        PrintTemplateVersion v=version(); v.setLayoutJson("{\"schemaVersion\":1,\"faceRole\":\"BACK\",\"sideCount\":1,\"schemas\":[[{\"name\":\"name_1\",\"type\":\"text\"}]]}");
        try {new PrintRendererClient(p,new PrintAccessPolicy(p,null),rest).validate(template(),v); fail("越界必须阻止发布");}
        catch(PrintApiException error) {assertEquals("RENDER_VALIDATION_FAILED",error.getCode()); assertTrue("错误应包含组件定位",error.getDetails().containsKey("violations")); java.util.List<?> violations=(java.util.List<?>)error.getDetails().get("violations"); assertEquals("name_1",((java.util.Map<?,?>)violations.get(0)).get("schemaName"));}
        server.verify();
    }

    @Test public void preservesOnlyKnownChineseComponentNamesInRendererDiagnostics() {
        loginManager(); PrintFeatureProperties p=properties(); p.setRendererUrl("http://127.0.0.1:19999"); p.setRendererToken("synthetic-test-token");
        RestTemplate rest=new RestTemplate(); MockRestServiceServer server=MockRestServiceServer.bindTo(rest).build();
        server.expect(requestTo("http://127.0.0.1:19999/internal/print-renderer/v1/render")).andRespond(withStatus(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY).contentType(MediaType.APPLICATION_JSON)
            .body("{\"error\":{\"details\":[{\"code\":\"TEXT_OVERFLOW\",\"schemaName\":\"员工姓名\"},{\"code\":\"TEXT_OVERFLOW\",\"schemaName\":\"untrusted_remote_name\"}]}}"));
        PrintTemplateVersion v=version(); v.setLayoutJson("{\"schemaVersion\":1,\"faceRole\":\"BACK\",\"sideCount\":1,\"schemas\":[[{\"name\":\"员工姓名\",\"type\":\"text\"}]]}");
        try { new PrintRendererClient(p,new PrintAccessPolicy(p,null),rest).validate(template(),v); fail("应拒绝超长文本"); }
        catch(PrintApiException error) {
            java.util.List<?> values=(java.util.List<?>)error.getDetails().get("violations");
            assertEquals("员工姓名",((java.util.Map<?,?>)values.get(0)).get("schemaName"));
            assertEquals("BACK",((java.util.Map<?,?>)values.get(0)).get("face"));
            assertFalse(((java.util.Map<?,?>)values.get(1)).containsKey("schemaName"));
        }
        server.verify();
    }

    @Test public void rendererAuthenticationFailureIsDependencyFailure() {
        loginManager(); PrintFeatureProperties p=properties(); p.setRendererUrl("http://127.0.0.1:19999"); p.setRendererToken("synthetic-test-token");
        RestTemplate rest=new RestTemplate(); MockRestServiceServer server=MockRestServiceServer.bindTo(rest).build();
        server.expect(requestTo("http://127.0.0.1:19999/internal/print-renderer/v1/render")).andRespond(withStatus(org.springframework.http.HttpStatus.UNAUTHORIZED));
        try { new PrintRendererClient(p,new PrintAccessPolicy(p,null),rest).validate(template(),version()); fail("依赖凭证错误必须拒绝"); }
        catch(PrintApiException error) { assertEquals(503,error.getStatus()); assertEquals("PRINT_RENDERER_UNAVAILABLE",error.getCode()); }
        server.verify();
    }

    private void runResponse(boolean tamperHash, boolean wrongSize) throws Exception {
        loginManager(); PrintFeatureProperties p = properties(); p.setRendererUrl("http://127.0.0.1:19999"); p.setRendererToken("synthetic-test-token");
        RestTemplate rest = new RestTemplate(); MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        byte[] pdf;
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.addPage(new PDPage(new PDRectangle((float) ((wrongSize ? 80 : 85.6) * 72 / 25.4), (float) (53.98 * 72 / 25.4)))); document.save(out); pdf = out.toByteArray();
        }
        StringBuilder hex = new StringBuilder("sha256:"); for (byte b : MessageDigest.getInstance("SHA-256").digest(pdf)) hex.append(String.format("%02x", b & 255));
        final String hash = hex.toString();
        server.expect(requestTo("http://127.0.0.1:19999/internal/print-renderer/v1/render")).andExpect(method(HttpMethod.POST)).andExpect(header("Authorization", "Bearer synthetic-test-token")).andRespond(request -> {
            ObjectNode sent = (ObjectNode) json.readTree(((MockClientHttpRequest) request).getBodyAsString());
            assertEquals("PREVIEW", sent.path("purpose").asText()); assertEquals(1, sent.path("expectedFaceCount").asInt());
            assertEquals("BACK", sent.at("/faceSources/0/face").asText()); assertEquals(3, sent.at("/faceSources/0/draftRevision").asInt());
            assertFalse(sent.at("/faceSources/0").has("templateVersionId")); assertEquals(85.6, sent.at("/faceSources/0/template/basePdf/width").asDouble(), 0.0001);
            ObjectNode response = json.createObjectNode().put("status", "READY").put("renderRequestId", sent.path("requestId").asText()).put("previewId", sent.path("previewId").asText());
            response.putArray("artifacts").addObject().put("artifactId", "00000000-0000-4000-8000-000000000002").put("face", "BACK").put("mediaType", "application/pdf").put("sha256", tamperHash ? "sha256:invalid" : hash).put("bytes", pdf.length).put("contentBase64", Base64.getEncoder().encodeToString(pdf)).put("pageCount", 1).put("widthMm", 85.6).put("heightMm", 53.98);
            return withSuccess(response.toString(), MediaType.APPLICATION_JSON).createResponse(request);
        });
        PrintRendererClient client = new PrintRendererClient(p, new PrintAccessPolicy(p, null), rest);
        if (tamperHash || wrongSize) expectCode("RENDER_VALIDATION_FAILED", () -> client.validate(template(), version()));
        else assertEquals("READY", client.validate(template(), version()).get("status"));
        server.verify();
    }
}
