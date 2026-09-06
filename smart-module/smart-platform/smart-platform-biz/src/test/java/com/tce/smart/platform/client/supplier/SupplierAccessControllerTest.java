package com.tce.smart.platform.client.supplier;

import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.client.identity.ClientApiException;
import com.tce.smart.platform.client.identity.ClientPersonnelDirectory;
import com.tce.smart.platform.core.client.supplier.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** HTTP契约测试仅注入测试认证上下文；不启动OAuth、Nacos或业务库。 */
public class SupplierAccessControllerTest extends SupplierAccessTestFixture {
    MockMvc mvc;
    SupplierAccessRepository repository;
    ClientPersonnelDirectory personnel;
    SupplierVerification verification;
    SupplierPresence fixturePresence = SupplierPresence.UNKNOWN;
    static final String PATH = "/api/v1";

    @Before public void prepareHttp() {
        repository = mock(SupplierAccessRepository.class);
		personnel = mock(ClientPersonnelDirectory.class);
		when(personnel.displayNameOrStaffNo("synthetic-operator")).thenReturn("合成安检员");
        SupplierAdmissionLock admissionLock = mock(SupplierAdmissionLock.class);
        when(admissionLock.withQualification(any(), anyString(), any(), any(), any())).thenAnswer(invocation -> {
            SupplierAdmissionSource source = invocation.getArgument(0);
            SupplierQualificationSnapshot qualification = source.load(invocation.getArgument(1), invocation.getArgument(2), NOW);
            java.util.function.Function<SupplierQualificationSnapshot, Object> action = invocation.getArgument(4);
            return action.apply(qualification);
        });
        SupplierAccessService service = new SupplierAccessService(properties, source, repository,
                Clock.fixed(NOW, ZoneOffset.UTC), admissionLock, personnel);
        mvc = MockMvcBuilders.standaloneSetup(new SupplierAccessController(service))
                .setControllerAdvice(new SupplierAccessExceptionHandler()).build();
        authenticate(10, true, "supplier:execute", "supplier:read", "supplier:post:gate");
        when(repository.verifyOrInitialize(any(), any(), any(), any(), anyString())).thenAnswer(invocation -> {
            SupplierQualificationSnapshot q = invocation.getArgument(0);
            SupplierOperator actor = invocation.getArgument(1);
            SupplierPostAreaMapping mapping = invocation.getArgument(2);
            verification = new SupplierAccessWorkflow().verify(q, actor, mapping,
                    SupplierPresenceSnapshot.current(q.getPersonId(), "area", fixturePresence, 0), NOW, "verify-1");
            return verification;
        });
        when(repository.findVerification("verify-1")).thenAnswer(invocation -> verification);
        when(repository.listEvents(anySet(), anyInt())).thenReturn(Collections.emptyList());
    }
    @After public void clearAuthentication() { SecurityContextHolder.clearContext(); }

    static void authenticate(int id, boolean enabled, String... permissions) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String p : permissions) authorities.add(new SimpleGrantedAuthority(p));
		String staffNo = id == 10 ? "synthetic-operator" : "synthetic-operator-" + id;
		SmartUser user = new SmartUser(id, 1, staffNo, Collections.singletonList(1), "unused", enabled, true, true, true, authorities);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, authorities));
    }

    @Test public void exceptionResolverNeverLogsRawBackendFailure() throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(
                "org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver");
        ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new ch.qos.logback.core.read.ListAppender<>();
        ch.qos.logback.classic.Level previous = logger.getLevel();
        appender.start(); logger.addAppender(appender); logger.setLevel(ch.qos.logback.classic.Level.DEBUG);
        when(repository.listEvents(anySet(), anyInt())).thenThrow(new RuntimeException("synthetic-secret document SQL"));
        try {
            mvc.perform(get(PATH + "/visitor-passes")).andExpect(status().isServiceUnavailable());
            for (ch.qos.logback.classic.spi.ILoggingEvent event : appender.list) {
                assertFalse(event.getFormattedMessage().contains("synthetic-secret"));
                assertFalse(event.getFormattedMessage().contains("document SQL"));
            }
        } finally { logger.detachAppender(appender); logger.setLevel(previous); appender.stop(); }
    }

    @Test public void debugFrameworkLoggingDoesNotExposeScanOrPersonnelSnapshots() throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(
                "org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor");
        ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new ch.qos.logback.core.read.ListAppender<>();
        ch.qos.logback.classic.Level previous = logger.getLevel();
        appender.start(); logger.addAppender(appender); logger.setLevel(ch.qos.logback.classic.Level.DEBUG);
        try {
            verificationStatus(200);
            for (ch.qos.logback.classic.spi.ILoggingEvent event : appender.list) {
                assertFalse(event.getFormattedMessage().contains(BADGE));
                assertFalse(event.getFormattedMessage().contains("合成访客甲"));
                assertFalse(event.getFormattedMessage().contains("synthetic-phone"));
            }
        } finally { logger.detachAppender(appender); logger.setLevel(previous); appender.stop(); }
    }

    @Test public void knownPresenceOffersOnlyTheOppositeDirection() throws Exception {
        fixturePresence = SupplierPresence.INSIDE;
            mvc.perform(post(PATH + "/visitor-checks").contentType("application/json").content(verifyBody()))
                .andExpect(jsonPath("$.presence").value("inside"))
                .andExpect(jsonPath("$.allowedDirections[0]").value("leave"))
                .andExpect(jsonPath("$.allowedDirections.length()").value(1));
        fixturePresence = SupplierPresence.OUTSIDE;
        mvc.perform(post(PATH + "/visitor-checks").contentType("application/json").content(verifyBody()))
                .andExpect(jsonPath("$.presence").value("outside"))
                .andExpect(jsonPath("$.allowedDirections[0]").value("enter"))
                .andExpect(jsonPath("$.allowedDirections.length()").value(1));
    }

    @Test public void rejectsNonSmartPrincipalAndEveryExpiredAccountFlag() throws Exception {
        List<SimpleGrantedAuthority> grants = Arrays.asList(new SimpleGrantedAuthority("supplier:execute"), new SimpleGrantedAuthority("supplier:post:gate"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("synthetic", null, grants));
        verificationStatus(401);
        for (int flag = 0; flag < 3; flag++) {
            SmartUser user = new SmartUser(10, 1, "synthetic-operator", Collections.singletonList(1), "unused", true, flag != 0, flag != 1, flag != 2, grants);
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, grants));
            verificationStatus(401);
        }
        verifyZeroInteractions(fellows, applies, repository);
    }

    @Test public void malformedKeyMissingVerificationAndRuleConflictHaveSafeStatus() throws Exception {
        for (String key : new String[]{"with spaces", "<synthetic-secret>", String.join("", Collections.nCopies(129, "x"))}) {
            mvc.perform(post(PATH + "/visitor-passes").header("Idempotency-Key", key).contentType("application/json").content(eventBody())).andExpect(status().isBadRequest());
        }
        eventStatus(404);
        doThrow(new SupplierRuleViolation(SupplierRuleViolation.Code.VERSION_CONFLICT, "synthetic-secret"))
                .when(repository).verifyOrInitialize(any(), any(), any(), any(), anyString());
        String response = mvc.perform(post(PATH + "/visitor-checks").contentType("application/json").content(verifyBody()))
                .andExpect(status().isConflict()).andReturn().getResponse().getContentAsString();
        assertFalse(response.contains("synthetic-secret"));
    }

    @Test public void returnsExactStringIdsUnknownAndNoInternalIdentityFields() throws Exception {
        String json = mvc.perform(post(PATH + "/visitor-checks").contentType("application/json").content(verifyBody()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.badgeId").value(BADGE))
                .andExpect(jsonPath("$.admissionId").value("8000001"))
                .andExpect(jsonPath("$.presence").value("unknown"))
                .andExpect(jsonPath("$.allowedDirections[0]").value("enter"))
                .andExpect(jsonPath("$.allowedDirections[1]").value("leave"))
                .andExpect(jsonPath("$.personId").doesNotExist())
                .andExpect(jsonPath("$.qualificationSnapshot").doesNotExist()).andReturn().getResponse().getContentAsString();
        assertFalse(json.contains("SYNTHETIC-X")); assertFalse(json.contains("admittance-company:"));
    }

    @Test public void deniesUnauthenticatedDisabledAndMissingPermissionsBeforeLookingUpIdentity() throws Exception {
        SecurityContextHolder.clearContext(); verificationStatus(401);
        authenticate(10, false, "supplier:execute", "supplier:post:gate"); verificationStatus(401);
        authenticate(10, true, "supplier:execute"); verificationStatus(403);
        authenticate(10, true, "supplier:read", "supplier:post:gate"); verificationStatus(403);
        authenticate(10, true, "supplier:execute", "supplier:post:gate");
        ((SmartUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).setParkIdList(Collections.singletonList(2));
        verificationStatus(403); verifyZeroInteractions(fellows, applies, repository);
    }

    @Test public void disabledEndpointFailsClosedWithoutDataAccess() throws Exception {
        properties.setEnabled(false); verificationStatus(503); verifyZeroInteractions(fellows, applies, repository);
    }

    @Test public void rechecksCurrentOperatorEligibilityBeforeReadingSupplierData() throws Exception {
        when(personnel.require("synthetic-operator")).thenThrow(new ClientApiException(403));
        verificationStatus(403);
        verifyZeroInteractions(fellows, applies, repository);
    }

    @Test public void rejectsNumericCredentialExtraFieldsAndMalformedJsonWithoutEcho() throws Exception {
        for (String body : new String[]{"{\"credentialCode\":9223372036854775806,\"postId\":\"gate\"}",
                "{\"credentialCode\":\"" + BADGE + "\",\"postId\":\"gate\",\"operatorId\":\"fake\"}", "{synthetic-secret"}) {
            String response = mvc.perform(post(PATH + "/visitor-checks").contentType("application/json").content(body))
                    .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
            assertFalse(response.contains(BADGE)); assertFalse(response.contains("synthetic-secret"));
        }
        verifyZeroInteractions(fellows, applies, repository);
    }

    @Test public void eventRequiresKeyBindingAndCurrentEligibility() throws Exception {
        verificationStatus(200);
        mvc.perform(post(PATH + "/visitor-passes").contentType("application/json").content(eventBody())).andExpect(status().isBadRequest());
        authenticate(11, true, "supplier:execute", "supplier:post:gate"); eventStatus(403);
        authenticate(10, true, "supplier:execute", "supplier:post:gate"); apply.setStatus(7); eventStatus(403);
        apply.setStatus(0); apply.setEndTime(java.time.LocalDateTime.parse("2026-09-05T12:00:00")); eventStatus(403);
        apply.setEndTime(java.time.LocalDateTime.parse("2026-09-05T13:00:00")); apply.setAreaType("11"); eventStatus(403);
        verify(repository, never()).record(anyString(), anyString(), anyString(), any(), any(), any(), any(), any(), anyString());
    }

    @Test public void recordsExplicitDirectionAndReturnsOriginalEventProjection() throws Exception {
        verificationStatus(200);
        when(repository.record(anyString(), anyString(), anyString(), any(), any(), any(), any(), any(), anyString()))
                .thenAnswer(invocation -> new SupplierAccessWorkflow().record(verification, invocation.getArgument(3),
                        invocation.getArgument(4), invocation.getArgument(5),
                        SupplierPresenceSnapshot.current(verification.getQualificationSnapshot().getPersonId(), "area", SupplierPresence.UNKNOWN, 0),
                        invocation.getArgument(6), NOW, "event-1"));
        mvc.perform(post(PATH + "/visitor-passes").header("Idempotency-Key", "event-key-1").contentType("application/json").content(eventBody()))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value("event-1"))
				.andExpect(jsonPath("$.operatorName").value("合成安检员")).andExpect(jsonPath("$.direction").value("enter"))
                .andExpect(jsonPath("$.occurredAt").value("2026-09-05T04:00:00Z"));
    }

    @Test public void limitsReadToAuthorizedPostsAndHidesBackendFailures() throws Exception {
        mvc.perform(get(PATH + "/visitor-passes")).andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(repository).listEvents(Collections.singleton("gate"), 100);
        when(repository.listEvents(anySet(), anyInt())).thenThrow(new RuntimeException("synthetic-secret document SQL"));
        String response = mvc.perform(get(PATH + "/visitor-passes")).andExpect(status().isServiceUnavailable()).andReturn().getResponse().getContentAsString();
        assertFalse(response.contains("synthetic-secret")); assertFalse(response.contains("SQL"));
        authenticate(10, true, "supplier:execute", "supplier:post:gate");
        mvc.perform(get(PATH + "/visitor-passes")).andExpect(status().isForbidden());
    }
    private String verifyBody() { return "{\"credentialCode\":\"" + BADGE + "\",\"postId\":\"gate\"}"; }
    private String eventBody() { return "{\"verificationId\":\"verify-1\",\"postId\":\"gate\",\"direction\":\"enter\"}"; }
    private void verificationStatus(int expected) throws Exception { mvc.perform(post(PATH + "/visitor-checks").contentType("application/json").content(verifyBody())).andExpect(status().is(expected)); }
    private void eventStatus(int expected) throws Exception { mvc.perform(post(PATH + "/visitor-passes").header("Idempotency-Key", "event-key-1").contentType("application/json").content(eventBody())).andExpect(status().is(expected)); }
}
