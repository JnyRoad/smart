package com.tce.smart.platform.client.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.client.supplier.*;
import com.tce.smart.platform.core.client.supplier.JdbcSupplierAccessStore;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.mapper.SmtAdmittanceFellowMapper;
import com.tce.smart.platform.service.ImageService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import com.tce.smart.platform.core.client.supplier.SupplierQualificationSnapshot;
import org.apache.ibatis.session.SqlSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** HTTP协议、真实MyBatis入厂资料读取和真实Oracle通行事务联合验证；认证只在测试中注入。 */
public class SupplierAccessOracleFlowTest {
    private static final String ROOT = "/api/v1";
    private SupplierFlowOracleFixture fixture;
    private SqlSession session;
    private MockMvc mvc;
    private final ObjectMapper json = new ObjectMapper();
    private int actorId;
    private volatile Runnable afterSourceRead;
    private final MutableClock clock = new MutableClock();
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private final ExecutorService workers = Executors.newFixedThreadPool(2);

    @Before
    public void prepare() throws Exception {
        fixture = SupplierFlowOracleFixture.open();
        Instant now = Instant.now(); clock.set(now);
        LocalDateTime local = LocalDateTime.ofInstant(now, ZoneId.of("Asia/Shanghai"));
        windowStart = local.minusHours(1); windowEnd = local.plusHours(2);
        fixture.seed(windowStart, windowEnd);
        org.apache.ibatis.session.SqlSessionFactory sessions = fixture.mapperFactory();
        session = sessions.openSession(true);
        SupplierAccessProperties props = new SupplierAccessProperties();
        props.setEnabled(true);
        SupplierAccessProperties.Post first = configuredPost(fixture.postId, fixture.areaId, "0");
        SupplierAccessProperties.Post second = configuredPost(fixture.otherPostId, fixture.otherAreaId, "11");
        props.setPosts(Arrays.asList(first, second));
        props.validate();
        ImageService images = new ImageService() {
            public String buildImageUrl(String id) { throw new AssertionError("合成记录没有照片，不应请求图片服务"); }
            public String buildImageUrl(Integer park, String id) { return "http://127.0.0.1/synthetic-photos/" + park + "/" + id; }
            public String buildDownloadUrl(String id, String name) { throw new AssertionError("不应下载附件"); }
            public void saveDeviceUploadImg(String device, String code) { throw new AssertionError("不应写入设备照片"); }
        };
        SupplierAdmissionSource source = new SupplierAdmissionSource(session.getMapper(SmtAdmittanceFellowMapper.class),
                session.getMapper(SmtAdmittanceApplyMapper.class), images, props) {
            @Override public SupplierQualificationSnapshot loadUsingSession(SqlSession lockedSession, String badge, SupplierAccessProperties.Post post, Instant now) {
                SupplierQualificationSnapshot result = super.loadUsingSession(lockedSession, badge, post, now);
                Runnable hook = afterSourceRead;
                if (hook != null) hook.run();
                return result;
            }
        };
        SupplierAccessService service = new SupplierAccessService(props, source,
                new JdbcSupplierAccessRepository(new JdbcSupplierAccessStore(fixture.dataSource)), clock, new SupplierAdmissionLock(fixture.dataSource, sessions));
        mvc = MockMvcBuilders.standaloneSetup(new SupplierAccessController(service))
                .setControllerAdvice(new SupplierAccessExceptionHandler()).build();
        actorId = 1900000000 + (int) Math.floorMod(UUID.randomUUID().getLeastSignificantBits(), 100000000L);
        authorize(fixture.postId, fixture.otherPostId);
    }

    private SupplierAccessProperties.Post configuredPost(String id, String area, String code) {
        SupplierAccessProperties.Post p = new SupplierAccessProperties.Post();
        p.setId(id); p.setName("合成岗位"); p.setParkId(7); p.setParkName("合成园区");
        p.setAreaId(area); p.setAreaName("合成区域" + code); p.setAdmittanceAreaTypeCode(code);
        return p;
    }

    private void authorize(String... posts) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(posts)
                .map(p -> new SimpleGrantedAuthority("supplier:post:" + p)).collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("supplier:execute"));
        authorities.add(new SimpleGrantedAuthority("supplier:read"));
        SmartUser principal = new SmartUser(actorId, 1, "LOCAL-FLOW-" + fixture.suffix,
                Collections.singletonList(7), "test-only-unused-hash", true, true, true, true, authorities);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }

    @After
    public void cleanup() throws Exception {
        SecurityContextHolder.clearContext();
        workers.shutdownNow(); workers.awaitTermination(10, TimeUnit.SECONDS);
        if (session != null) session.close();
        if (fixture != null) fixture.cleanup();
    }

    @Test
    public void personIdToEnterLeaveAndScopedRecordsUsesActualOracle() throws Exception {
        JsonNode first = verify(fixture.fellowId, fixture.postId, 200);
        assertTrue(first.get("badgeId").isTextual());
        assertEquals(Long.toString(fixture.fellowId), first.get("badgeId").textValue());
        assertEquals(Long.toString(fixture.applyId), first.get("admissionId").textValue());
        assertEquals("unknown", first.get("presence").textValue());
        assertEquals(2, first.get("allowedDirections").size());
        assertEquals("合成主访客", first.get("visitorName").textValue());
        assertEquals("http://127.0.0.1/synthetic-photos/7/synthetic-main-photo", first.get("photoUrl").textValue());
        assertEquals("测试联系方式", first.get("visitorPhone").textValue());
        assertEquals("测试入厂单位", first.get("supplierName").textValue());
        assertEquals("合成被访人", first.get("hostName").textValue());
        assertEquals("测试被访电话", first.get("hostPhone").textValue());
        assertEquals(windowStart.atZone(ZoneId.of("Asia/Shanghai")).toInstant(), Instant.parse(first.get("validFrom").textValue()));
        assertEquals(windowEnd.atZone(ZoneId.of("Asia/Shanghai")).toInstant(), Instant.parse(first.get("validUntil").textValue()));
        assertEquals(json.readTree("[\"合成区域0\",\"合成区域11\"]"), first.get("authorizedAreas"));
        assertFalse(first.toString().contains(fixture.mainDocument));
        assertFalse(first.toString().contains("SYNTHETIC-"));
        JsonNode entered = record(first, fixture.postId, "enter", "first-enter", 200);
        JsonNode replay = record(first, fixture.postId, "enter", "first-enter", 200);
        assertEquals(entered, replay);
        assertEquals(1, fixture.eventCount());
        JsonNode inside = verify(fixture.fellowId, fixture.postId, 200);
        assertEquals("inside", inside.get("presence").textValue());
        record(inside, fixture.postId, "enter", "duplicate-direction", 409);
        record(inside, fixture.postId, "leave", "first-leave", 200);
        JsonNode fellow = verify(fixture.secondFellowId, fixture.otherPostId, 200);
        assertEquals("合成随行访客", fellow.get("visitorName").textValue());
        assertEquals("", fellow.get("visitorPhone").textValue());
        record(fellow, fixture.otherPostId, "leave", "initial-leave", 200);
        assertEquals(3, fixture.eventCount());
        authorize(fixture.postId);
        MvcResult list = mvc.perform(get(ROOT + "/visitor-passes")).andReturn();
        assertEquals(200, list.getResponse().getStatus());
        JsonNode events = json.readTree(list.getResponse().getContentAsByteArray());
        assertEquals(2, events.size());
        for (JsonNode event : events) assertEquals(fixture.postId, event.get("postId").textValue());
        verify(fixture.secondFellowId, fixture.otherPostId, 403);
    }

    @Test public void historicalNullTypeBecomesExplicitZeroWithoutResettingPresence() throws Exception {
        JsonNode first = verify(fixture.fellowId, fixture.postId, 200);
        record(first, fixture.postId, "enter", "legacy-null-enter", 200);
        fixture.execute("UPDATE SMT_ADMITTANCE_FELLOW SET CERT_TYPE = 0 WHERE ID = ?", fixture.fellowId);
        assertEquals("inside", verify(fixture.fellowId, fixture.postId, 200).get("presence").textValue());
        assertEquals(1, fixture.eventCount());
    }

    @Test public void sixDigitFellowIdWorksButSmsOnlyValueNeverResolvesToAnyone() throws Exception {
        fixture.execute("INSERT INTO SMT_ADMITTANCE_FELLOW (ID,VISITOR_ID,FELLOW_NAME,CERT_NO,CERT_TYPE,IS_MAIN) VALUES (?,?,'合成短ID',?,NULL,0)", fixture.shortFellowId, fixture.applyId, fixture.fellowDocument);
        JsonNode shortBadge = verify(fixture.shortFellowId, fixture.postId, 200);
        assertEquals(Long.toString(fixture.shortFellowId), shortBadge.get("badgeId").textValue());
        record(shortBadge, fixture.postId, "enter", "short-enter", 200);
        fixture.execute("UPDATE SMT_ADMITTANCE_APPLY SET SMS_CODE = '123456' WHERE ID = ?", fixture.applyId);
        verify(123456L, fixture.postId, 404); assertEquals(1, fixture.eventCount());
    }

    @Test public void withdrawalHoldingApplyRowCommitsBeforeActionAndActionIsDenied() throws Exception {
        explicitTypes(); JsonNode verified = verify(fixture.fellowId, fixture.postId, 200);
        try (Connection withdrawal = fixture.dataSource.getConnection()) {
            withdrawal.setAutoCommit(false);
            try (PreparedStatement statement = withdrawal.prepareStatement("UPDATE SMT_ADMITTANCE_APPLY SET STATUS = 7 WHERE ID = ?")) {
                statement.setLong(1, fixture.applyId); statement.executeUpdate();
            }
            CountDownLatch started = new CountDownLatch(1);
            Future<JsonNode> event = asyncRecord(verified, "withdrawal-first", 403, started);
            assertTrue(started.await(3, TimeUnit.SECONDS)); assertStillBlocked(event);
            withdrawal.commit(); event.get(8, TimeUnit.SECONDS);
        }
        assertEquals(0, fixture.eventCount());
    }

    @Test public void actionHoldingAdmissionRowsMakesConcurrentWithdrawalWaitUntilCommit() throws Exception {
        explicitTypes(); JsonNode verified = verify(fixture.fellowId, fixture.postId, 200);
        CountDownLatch loaded = new CountDownLatch(1), continueAction = new CountDownLatch(1);
        afterSourceRead = () -> { loaded.countDown(); await(continueAction); };
        Future<JsonNode> event = asyncRecord(verified, "action-first", 200, new CountDownLatch(0));
        try {
            assertTrue(loaded.await(3, TimeUnit.SECONDS));
            Future<?> withdrawal = workers.submit(() -> {
                try { fixture.execute("UPDATE SMT_ADMITTANCE_APPLY SET STATUS = 7 WHERE ID = ?", fixture.applyId); }
                catch (Exception failure) { throw new IllegalStateException(failure); }
            });
            assertStillBlocked(withdrawal); continueAction.countDown();
            event.get(8, TimeUnit.SECONDS); withdrawal.get(8, TimeUnit.SECONDS);
            assertEquals(1, fixture.eventCount()); afterSourceRead = null;
            verify(fixture.fellowId, fixture.postId, 403);
        } finally { continueAction.countDown(); }
    }

    @Test public void presenceLockWaitDoesNotReuseAnExpiredRequestStartTime() throws Exception {
        explicitTypes(); JsonNode verified = verify(fixture.fellowId, fixture.postId, 200);
        CountDownLatch loaded = new CountDownLatch(1); afterSourceRead = loaded::countDown;
        try (Connection holding = fixture.dataSource.getConnection()) {
            holding.setAutoCommit(false);
            try (PreparedStatement statement = holding.prepareStatement("SELECT VERSION_NO FROM SMT_CLIENT_SUP_PRESENCE WHERE AREA_ID = ? FOR UPDATE")) {
                statement.setString(1, fixture.areaId); statement.executeQuery().close();
            }
            Future<JsonNode> event = asyncRecord(verified, "expired-during-wait", 403, new CountDownLatch(0));
            assertTrue(loaded.await(3, TimeUnit.SECONDS)); assertStillBlocked(event);
            clock.set(windowEnd.atZone(ZoneId.of("Asia/Shanghai")).toInstant()); holding.commit();
            event.get(8, TimeUnit.SECONDS);
        }
        assertEquals(0, fixture.eventCount());
    }

    private void explicitTypes() throws Exception {
        fixture.execute("UPDATE SMT_ADMITTANCE_FELLOW SET CERT_TYPE = 0 WHERE VISITOR_ID = ?", fixture.applyId);
    }
    private Future<JsonNode> asyncRecord(JsonNode verified, String key, int expected, CountDownLatch started) {
        return workers.submit(() -> {
            authorize(fixture.postId, fixture.otherPostId); started.countDown();
            try { return record(verified, fixture.postId, "enter", key, expected); }
            finally { SecurityContextHolder.clearContext(); }
        });
    }
    private static void await(CountDownLatch latch) {
        try { if (!latch.await(8, TimeUnit.SECONDS)) throw new IllegalStateException("合成并发协调超时"); }
        catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); throw new IllegalStateException("合成并发已取消"); }
    }
    private static void assertStillBlocked(Future<?> operation) throws Exception {
        try { operation.get(250, TimeUnit.MILLISECONDS); fail("相冲突的事务应等待行锁"); }
        catch (TimeoutException expected) { }
    }
    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> value = new AtomicReference<>();
        void set(Instant now) { value.set(now); }
        @Override public Instant instant() { return value.get(); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }

    @Test
    public void withdrawalBetweenVerificationAndActionIsReReadFromOracle() throws Exception {
        JsonNode verified = verify(fixture.fellowId, fixture.postId, 200);
        fixture.execute("UPDATE SMT_ADMITTANCE_APPLY SET STATUS = 7 WHERE ID = ?", fixture.applyId);
        record(verified, fixture.postId, "enter", "after-withdrawal", 403);
        assertEquals(0, fixture.eventCount());
        verify(fixture.fellowId, fixture.postId, 403);
    }

    @Test
    public void admissionIdAndSmsCodeCannotSubstituteForPersonId() throws Exception {
        verify(fixture.applyId, fixture.postId, 404);
        Map<String, String> body = new LinkedHashMap<>();
        body.put("credentialCode", "123456"); body.put("postId", fixture.postId);
        MvcResult response = mvc.perform(post(ROOT + "/visitor-checks").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(body))).andReturn();
        assertEquals(404, response.getResponse().getStatus());
        assertFalse(response.getResponse().getContentAsString().contains("123456"));
        assertEquals(0, fixture.eventCount());
    }

    @Test
    public void unauthenticatedRequestsDoNotDiscloseVisitorData() throws Exception {
        SecurityContextHolder.clearContext();
        verify(fixture.fellowId, fixture.postId, 401);
        assertEquals(0, fixture.eventCount());
    }

    private JsonNode verify(long id, String postId, int status) throws Exception {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("credentialCode", Long.toString(id)); body.put("postId", postId);
        MvcResult response = mvc.perform(post(ROOT + "/visitor-checks").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(body))).andReturn();
        assertEquals(status, response.getResponse().getStatus());
        return json.readTree(response.getResponse().getContentAsByteArray());
    }

    private JsonNode record(JsonNode verification, String postId, String direction, String key, int status) throws Exception {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("verificationId", verification.get("id").textValue()); body.put("postId", postId); body.put("direction", direction);
        MvcResult response = mvc.perform(post(ROOT + "/visitor-passes").contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", fixture.suffix + ":" + key).content(json.writeValueAsBytes(body))).andReturn();
        assertEquals(status, response.getResponse().getStatus());
        return json.readTree(response.getResponse().getContentAsByteArray());
    }
}
