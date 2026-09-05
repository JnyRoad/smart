package com.tce.smart.platform.service.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.smart.common.security.service.SmartUser;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class PrintAccessPolicyTest {
    @After public void clearIdentity() { SecurityContextHolder.clearContext(); }

    static PrintFeatureProperties properties() {
        PrintFeatureProperties p = new PrintFeatureProperties();
        p.setEnabled(true);
        p.getPermissions().put("read", "test:print:read");
        p.getPermissions().put("write", "test:print:write");
        p.getPermissions().put("publish", "test:print:publish");
        p.getPermissions().put("resource", "test:print:resource");
        p.getClassificationCodes().put("STAFF_CARD:EMPLOYEE", Collections.singletonList("STAFF_DEFAULT"));
        p.getClassificationCodes().put("VISITOR_SLIP:VISITOR", Arrays.asList("VISITOR_NORMAL", "VISITOR_SECURITY"));
        return p;
    }

    static void login(Integer park, String... permissions) {
        java.util.List<SimpleGrantedAuthority> grants = new java.util.ArrayList<>();
        for (String permission : permissions) grants.add(new SimpleGrantedAuthority(permission));
        SmartUser user = new SmartUser(7, 1, "print-test", Collections.singletonList(park), "unused", true, true, true, true, grants);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, grants));
    }

    static void loginManager() {
        login(1, "test:print:read", "test:print:write", "test:print:publish", "test:print:resource");
    }

    static void expectCode(String code, Runnable action) {
        try { action.run(); fail("预期拒绝：" + code); }
        catch (PrintApiException failure) { assertEquals(code, failure.getCode()); }
    }

    @Test public void requiresAuthenticatedSmartUserAndExplicitPermissionMapping() {
        PrintAccessPolicy policy = new PrintAccessPolicy(properties(), null);
        expectCode("PRINT_AUTHENTICATION_REQUIRED", () -> policy.require("read", "1"));
        loginManager();
        PrintFeatureProperties disabled = properties(); disabled.setEnabled(false);
        expectCode("PRINT_FEATURE_DISABLED", () -> new PrintAccessPolicy(disabled, null).require("read", "1"));
        PrintFeatureProperties missing = properties(); missing.getPermissions().remove("publish");
        expectCode("PRINT_PERMISSION_NOT_CONFIGURED", () -> new PrintAccessPolicy(missing, null).require("publish", "1"));
        policy.require("publish", "1");
    }

    @Test public void doesNotLetReadPermissionPublishOrClientParkOverrideTokenScope() {
        login(1, "test:print:read");
        PrintAccessPolicy policy = new PrintAccessPolicy(properties(), null);
        expectCode("PRINT_PERMISSION_DENIED", () -> policy.require("publish", "1"));
        expectCode("PRINT_SCOPE_DENIED", () -> policy.require("read", "2"));
        assertEquals("1", policy.resolvePark(null));
    }

    @Test public void rejectsRemoteReferencesAndUnregisteredResourceMetadata() throws Exception {
        loginManager();
        PrintAccessPolicy policy = new PrintAccessPolicy(properties(), null);
        ObjectMapper json = new ObjectMapper();
        expectCode("TEMPLATE_VALIDATION_FAILED", () -> policy.rejectUnsafeJson(json.createObjectNode().put("src", "https://untrusted/image.png")));
        expectCode("TEMPLATE_VALIDATION_FAILED", () -> policy.rejectUnsafeJson(json.createObjectNode().put("src", "../../secret")));
        expectCode("TEMPLATE_VALIDATION_FAILED", () -> policy.rejectUnsafeJson(json.createObjectNode().put("content", "<script>alert(1)</script>")));
        expectCode("PRINT_RESOURCE_NOT_CONFIGURED", () -> policy.validateManifest("1", json.createArrayNode().add(json.createObjectNode().put("objectId", "00000000-0000-4000-8000-000000000001"))));
    }

    @Test public void checksPhotoSubjectGrantAndStoredHashBeforeReturningBytes() throws Exception {
        loginManager();
        byte[] bytes = new byte[]{1, 2, 3};
        PrintResourceStore.RegisteredResource stored = new PrintResourceStore.RegisteredResource();
        stored.setObjectId("00000000-0000-4000-8000-000000000001"); stored.setParkId("1");
        stored.setPurpose("PHOTO"); stored.setAccessScope("STAFF_RECORD"); stored.setSubjectId("staff-2");
        stored.setContentHash("sha256:039058c6f2c0cb492c533b0a4d14ef77cc0f78abccced5287d84a1a2011cfb81");
        stored.setMediaType("image/png"); stored.setSizeBytes(3L);
        PrintResourceStore store = new PrintResourceStore() {
            public RegisteredResource describe(String id) { return stored; }
            public boolean canAccess(String actor, RegisteredResource resource) { return false; }
            public byte[] read(String id) { return bytes; }
        };
        PrintAccessPolicy policy = new PrintAccessPolicy(properties(), store);
        expectCode("PRINT_SCOPE_DENIED", () -> policy.readResource("1", stored.getObjectId(), stored.getContentHash()));
        stored.setParkId("2");
        expectCode("PRINT_SCOPE_DENIED", () -> policy.readResource("1", stored.getObjectId(), stored.getContentHash()));
    }
}
