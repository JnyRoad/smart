package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.service.impl.AuthOperationVersionService;
import org.junit.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/** 本机合成 Oracle 的真实 Mapper、行锁、外层回滚和来源资源隔离验收。 */
public class AuthOperationVersionOracleTest {
    private JdbcTemplate jdbc;
    private AuthOperationVersionService service;
    private TransactionTemplate tx;
    private int park;
    private static boolean checked;
    private static HikariDataSource sharedDataSource;

    @Before
    public void setUp() throws Exception {
        String url = System.getenv("SMART_AUTH_ORACLE_URL");
        Assume.assumeTrue("显式启用本机合成 Oracle 才执行", url != null);
        Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:32768/FREEPDB1", url);
        Assert.assertEquals("SMART_AUTH_TEST", System.getenv("SMART_AUTH_ORACLE_USER"));
        if(sharedDataSource==null) {
            sharedDataSource=new HikariDataSource(); sharedDataSource.setJdbcUrl(url);
            sharedDataSource.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));
            sharedDataSource.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));
            sharedDataSource.setDriverClassName("oracle.jdbc.OracleDriver");
            sharedDataSource.setMaximumPoolSize(4); sharedDataSource.setMinimumIdle(0);
            sharedDataSource.setPoolName("auth-version-test");
        }
        HikariDataSource ds=sharedDataSource;
        jdbc = new JdbcTemplate(ds);
        synchronized (AuthOperationVersionOracleTest.class) {
            if (!checked) {
                int count = jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME IN "
                    + "('SMT_AUTH_SOURCE_COORD','SMT_AUTH_RESOURCE_COORD','SMT_AUTH_SOURCE_RESOURCE','SMT_AUTH_IDENTITY_ALIAS')", Integer.class);
                Assert.assertTrue("只有四表全不存在或完整已有才允许继续", count == 0 || count == 4);
                if (count == 0) {
                    try (Connection c = ds.getConnection()) {
                        ScriptUtils.executeSqlScript(c, new ClassPathResource("auth-operation/version-schema.sql"));
                    }
                }
                Assert.assertEquals(Integer.valueOf(4), jdbc.queryForObject("SELECT COUNT(*) FROM USER_CONSTRAINTS WHERE CONSTRAINT_NAME IN "
                    + "('PK_ASC','PK_ARC','PK_ASR','PK_AIA') AND STATUS='ENABLED'", Integer.class));
                checked = true;
            }
        }
        MybatisConfiguration cfg = new MybatisConfiguration();
        cfg.setMapUnderscoreToCamelCase(true);
        cfg.addMapper(SmtAuthSubjectCoordMapper.class);cfg.addMapper(SmtAuthSourceCoordMapper.class); cfg.addMapper(SmtAuthResourceCoordMapper.class);
        cfg.addMapper(SmtAuthSourceResourceMapper.class); cfg.addMapper(SmtAuthIdentityAliasMapper.class);
        MybatisSqlSessionFactoryBean f = new MybatisSqlSessionFactoryBean();
        f.setDataSource(ds); f.setConfiguration(cfg);
        f.setMapperLocations(new Resource[]{new ClassPathResource("mapper/SmtAuthSubjectCoordMapper.xml"),new ClassPathResource("mapper/SmtAuthSourceCoordMapper.xml"),
            new ClassPathResource("mapper/SmtAuthResourceCoordMapper.xml"),
            new ClassPathResource("mapper/SmtAuthSourceResourceMapper.xml"),
            new ClassPathResource("mapper/SmtAuthIdentityAliasMapper.xml")});
        SqlSessionTemplate session = new SqlSessionTemplate(f.getObject());
        AuthOperationVersionService raw = new AuthOperationVersionService(session.getMapper(SmtAuthSubjectCoordMapper.class),session.getMapper(SmtAuthSourceCoordMapper.class),
            session.getMapper(SmtAuthResourceCoordMapper.class), session.getMapper(SmtAuthSourceResourceMapper.class),
            session.getMapper(SmtAuthIdentityAliasMapper.class));
        DataSourceTransactionManager tm = new DataSourceTransactionManager(ds);
        tx = new TransactionTemplate(tm);
        ProxyFactory proxy = new ProxyFactory(raw); proxy.setProxyTargetClass(true);
        proxy.addAdvice(new TransactionInterceptor(tm, new AnnotationTransactionAttributeSource()));
        service = (AuthOperationVersionService) proxy.getProxy();
        park = 100000 + (int)(positive() % 700000000L);
    }

    @AfterClass
    public static void closePool() { if(sharedDataSource!=null) sharedDataSource.close(); }

    @After
    public void cleanupOwnSyntheticPark() {
        if (jdbc == null || park == 0) return;
        jdbc.update("DELETE FROM SMT_AUTH_IDENTITY_ALIAS WHERE PARK_ID=?", park);
        jdbc.update("DELETE FROM SMT_AUTH_SOURCE_RESOURCE WHERE SOURCE_COORD_ID IN (SELECT ID FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?)", park);
        jdbc.update("DELETE FROM SMT_AUTH_RESOURCE_COORD WHERE PARK_ID=?", park);
        jdbc.update("DELETE FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?", park);
        jdbc.update("DELETE FROM SMT_AUTH_SUBJECT_COORD WHERE PARK_ID=?", park);
        jdbc.update("DELETE FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?)", park);
        jdbc.update("DELETE FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?", park);
        jdbc.update("DELETE FROM SMT_AUTH_DELETE_REQUEST WHERE PARK_ID=?", park);
        jdbc.update("DELETE FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?", park);
    }

    @Test
    public void concurrentSourceAcceptanceKeepsOnePermanentGeneration() throws Exception {
        List<SourceVersion> v = parallel(() -> service.reserveSourceIntent(intent("a", 1, 2)),
            () -> service.reserveSourceIntent(intent("a", 1, 2)));
        Assert.assertEquals(v.get(0).getSourceId(), v.get(1).getSourceId());
        Assert.assertEquals(1L, v.get(0).getGeneration()); Assert.assertEquals(1L, v.get(1).getGeneration());
        Assert.assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?", Integer.class, park).intValue());
    }

    @Test
    public void simultaneousLastTwoRevocationsProduceDelete() throws Exception {
        SourceIntent a = intent("a",1,2), b = intent("b",1,2);
        SourceVersion sa = service.reserveSourceIntent(a), sb = service.reserveSourceIntent(b);
        ResourceDecision first = stage(sa, key("staff", "DIRECT"), 1);
        stage(sb,key("staff","DIRECT"),2);
        sealAndSetActive(sa); sealAndSetActive(sb);
        List<ResourceDecision> decisions = parallel(() -> {
            SourceVersion da=service.reserveSourceIntent(a.toBuilder().intentKey("delete-a").action("DELETE").build());
            return stage(da,key("staff","DIRECT"),3);
        }, () -> {
            SourceVersion db=service.reserveSourceIntent(b.toBuilder().intentKey("delete-b").action("DELETE").build());
            return stage(db,key("staff","DIRECT"),4);
        });
        ResourceDecision last = service.currentDesired(first.getResourceId());
        Assert.assertEquals("DELETE", last.getAction()); Assert.assertTrue(last.getWindows().isEmpty());
        Assert.assertTrue(decisions.stream().anyMatch(d -> "DELETE".equals(d.getAction())));
    }

    @Test
    public void disjointWindowsRemainSeparateWhileTouchingWindowsMerge() {
        SourceVersion a=service.reserveSourceIntent(intent("a",1,2));
        SourceVersion b=service.reserveSourceIntent(intent("b",4,5));
        stage(a,key("staff","DIRECT"),1);
        ResourceDecision gap=stage(b,key("staff","DIRECT"),2);
        Assert.assertEquals(2,gap.getWindows().size()); Assert.assertTrue(gap.isRequiresMultipleWindows());
        Assert.assertEquals(LocalDateTime.of(2030,1,2,0,0),gap.getWindows().get(0).getTo());
        SourceVersion c=service.reserveSourceIntent(intent("c",2,4));
        ResourceDecision merged=stage(c,key("staff","DIRECT"),3);
        Assert.assertEquals(1,merged.getWindows().size());
        Assert.assertEquals(LocalDateTime.of(2030,1,5,0,0),merged.getWindows().get(0).getTo());
    }

    @Test
    public void rollbackDoesNotLeakSourceContributionOrResource() {
        try { tx.execute(status -> {
            SourceVersion s=service.reserveSourceIntent(intent("rollback",1,2));
            stage(s,key("staff","DIRECT"),1); throw new IllegalStateException("测试外层回滚");
        }); Assert.fail(); } catch(IllegalStateException expected) { }
        Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?",Integer.class,park));
        Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_RESOURCE_COORD WHERE PARK_ID=?",Integer.class,park));
    }

    @Test
    public void historicalAliasesRetainGenerationsAndRejectAmbiguousOwnership() {
        SourceVersion a=service.reserveSourceIntent(intent("a",1,2));
        ResourceKey ka=key("staff","DIRECT"); ResourceDecision ra=stage(a,ka,1);
        alias(ka,ra.getGeneration(),"old-card"); alias(ka,ra.getGeneration(),"new-card");
        Assert.assertEquals("UNIQUE",service.resolveLegacyAlias(ka,"CARD_NO","old-card").getOutcome());
        Assert.assertEquals("NONE",service.resolveLegacyAlias(key("staff","ISC"),"CARD_NO","old-card").getOutcome());
        Assert.assertEquals("NONE",service.resolveLegacyAlias(ka.toBuilder().parkId(park+1).build(),"CARD_NO","old-card").getOutcome());
        Assert.assertEquals("NONE",service.resolveLegacyAlias(ka.toBuilder().subjectType("VISITOR").build(),"CARD_NO","old-card").getOutcome());
        SourceVersion b=service.reserveSourceIntent(intent("b",1,2).toBuilder().subjectId("staff-b").build());
        ResourceKey kb=key("staff-b","DIRECT"); ResourceDecision rb=stage(b,kb,2); alias(kb,rb.getGeneration(),"old-card");
        Assert.assertEquals("AMBIGUOUS",service.resolveLegacyAlias(ka,"CARD_NO","old-card").getOutcome());
    }

    @Test
    public void unknownAttemptBlocksOnlySameResourceAndOldSuccessReturnsCompensation() {
        SourceIntent ai=intent("a",1,2); SourceVersion a=service.reserveSourceIntent(ai);
        ResourceDecision old=stage(a,key("staff","DIRECT"),1);
        Binding binding=persistBinding(a,old,1,ai.getBatchId());
        service.bindTarget(binding); service.bindAttempt(binding); service.markUnknown(binding);
        SourceVersion b=service.reserveSourceIntent(intent("b",4,5));
        ResourceDecision next=stage(b,key("staff","DIRECT"),2);
        Assert.assertEquals(binding.getAttemptId(),next.getBlockingAttemptId());
        SourceVersion c=service.reserveSourceIntent(intent("c",1,2).toBuilder().subjectId("other").build());
        Assert.assertNull(stage(c,key("other","DIRECT"),3).getBlockingAttemptId());
        Evidence e=Evidence.builder().binding(binding).action("ADD").sourceRowId(a.getSourceRowId()).sourceFingerprint(a.getSourceFingerprint()).trusted(false).build();
        Assert.assertFalse(service.applyEvidence(e).isMayApply());
        Assert.assertEquals(binding.getAttemptId(),service.currentDesired(old.getResourceId()).getBlockingAttemptId());
        EvidenceResult stale=service.applyEvidence(e.toBuilder().trusted(true).build());
        Assert.assertFalse(stale.isMayApply()); Assert.assertTrue(stale.isCompensationRequired());
        Assert.assertEquals(next.getGeneration(),stale.getCurrent().getGeneration()); Assert.assertEquals(2,stale.getCurrent().getWindows().size());
        Assert.assertEquals(0L,service.currentDesired(old.getResourceId()).getAppliedGeneration());
    }

    @Test
    public void changedSourceFingerprintCannotConvergeCurrentEvidence() {
        SourceIntent si=intent("a",1,2); SourceVersion s=service.reserveSourceIntent(si);
        ResourceDecision r=stage(s,key("staff","DIRECT"),1); Binding b=persistBinding(s,r,1,si.getBatchId());
        service.bindTarget(b); service.bindAttempt(b);
        jdbc.update("UPDATE SMT_AUTH_SOURCE_COORD SET SOURCE_FINGERPRINT='changed' WHERE ID=?",s.getSourceId());
        EvidenceResult result=service.applyEvidence(Evidence.builder().binding(b).trusted(true).action("ADD")
            .sourceRowId(s.getSourceRowId()).sourceFingerprint(s.getSourceFingerprint()).build());
        Assert.assertFalse(result.isMayApply()); Assert.assertEquals(0L,service.currentDesired(r.getResourceId()).getAppliedGeneration());
    }

    @Test
    public void deleteTombstoneAndRecreationNeverReuseSourceOrResourceGeneration() {
        SourceIntent si=intent("a",1,2); SourceVersion a=service.reserveSourceIntent(si);
        ResourceDecision add=stage(a,key("staff","DIRECT"),1); apply(a,add,1,si.getBatchId());
        service.sealSourceExpansion(a.getSourceId(),a.getGeneration(),a.getSourceFingerprint());
        service.completeSource(a.getSourceId(),a.getGeneration(),a.getSourceRowId(),a.getSourceFingerprint());
        SourceVersion d=service.reserveSourceIntent(si.toBuilder().intentKey("delete").action("DELETE").build());
        ResourceDecision del=stage(d,key("staff","DIRECT"),2); apply(d,del,2,si.getBatchId());
        service.sealSourceExpansion(d.getSourceId(),d.getGeneration(),d.getSourceFingerprint());
        Assert.assertEquals("TOMBSTONE",service.completeSource(d.getSourceId(),d.getGeneration(),d.getSourceRowId(),d.getSourceFingerprint()).getState());
        SourceVersion fresh=service.reserveSourceIntent(si.toBuilder().intentKey("replacement").sourceRowId("new-row").sourceFingerprint("new-fp").build());
        ResourceDecision latest=stage(fresh,key("staff","DIRECT"),3);
        Assert.assertEquals(3L,fresh.getGeneration()); Assert.assertEquals(3L,latest.getGeneration());
        Assert.assertEquals(add.getResourceId(),latest.getResourceId());
    }

    @Test
    public void resendSameWindowsConsumesNewResourceGeneration() {
        SourceIntent si=intent("resend",1,2); SourceVersion a=service.reserveSourceIntent(si);
        ResourceDecision one=stage(a,key("staff","DIRECT"),1); apply(a,one,1,si.getBatchId());
        service.sealSourceExpansion(a.getSourceId(),a.getGeneration(),a.getSourceFingerprint());
        service.completeSource(a.getSourceId(),a.getGeneration(),a.getSourceRowId(),a.getSourceFingerprint());
        SourceVersion resend=service.reserveSourceIntent(si.toBuilder().intentKey("resend-2").build());
        ResourceDecision two=stage(resend,key("staff","DIRECT"),2);
        Assert.assertEquals(2L,two.getGeneration());
    }

    @Test
    public void completedHistoricalIntentRetryDoesNotCreateAnotherGeneration() {
        SourceIntent si=intent("history",1,2); SourceVersion a=service.reserveSourceIntent(si);
        ResourceDecision one=stage(a,key("staff","DIRECT"),1); apply(a,one,1,si.getBatchId());
        service.sealSourceExpansion(a.getSourceId(),a.getGeneration(),a.getSourceFingerprint());
        service.completeSource(a.getSourceId(),a.getGeneration(),a.getSourceRowId(),a.getSourceFingerprint());
        SourceVersion second=service.reserveSourceIntent(si.toBuilder().intentKey("second").build());
        SourceVersion retry=service.reserveSourceIntent(si);
        Assert.assertTrue(retry.isIdempotent()); Assert.assertEquals(1L,retry.getGeneration());
        Assert.assertEquals(Long.valueOf(2L),jdbc.queryForObject("SELECT GENERATION FROM SMT_AUTH_SOURCE_COORD WHERE ID=?",Long.class,second.getSourceId()));
    }

    @Test
    public void staleDeleteKeepsCurrentAddAndReturnsItsExactCompensationBasis() {
        SourceIntent si=intent("old-delete",1,2);
        SourceVersion d=service.reserveSourceIntent(si.toBuilder().action("DELETE").build());
        ResourceDecision deleted=stage(d,key("staff","DIRECT"),1);
        Binding binding=persistBinding(d,deleted,1,si.getBatchId());
        service.bindTarget(binding); service.bindAttempt(binding); service.markUnknown(binding);
        SourceVersion newer=service.reserveSourceIntent(intent("new-add",4,5));
        ResourceDecision add=stage(newer,key("staff","DIRECT"),2);
        EvidenceResult old=service.applyEvidence(Evidence.builder().binding(binding).action("DELETE").trusted(true)
            .sourceRowId(d.getSourceRowId()).sourceFingerprint(d.getSourceFingerprint()).build());
        Assert.assertFalse(old.isMayApply()); Assert.assertTrue(old.isCompensationRequired());
        Assert.assertEquals("ADD",old.getCurrent().getAction()); Assert.assertEquals(add.getGeneration(),old.getCurrent().getGeneration());
        Assert.assertEquals(LocalDateTime.of(2030,1,4,0,0),old.getCurrent().getWindows().get(0).getFrom());
        Assert.assertEquals(0L,old.getCurrent().getAppliedGeneration());
    }

    @Test
    public void sameBatchSharesTargetButCrossBatchMustKeepItsOwnLogicalTarget() throws Exception {
        SourceIntent ai=intent("owner",1,2), bi=intent("share",1,2);
        SourceVersion a=service.reserveSourceIntent(ai), b=service.reserveSourceIntent(bi);
        ResourceDecision r=stage(a,key("staff","DIRECT"),1); stage(b,key("staff","DIRECT"),2);
        Binding first=persistBinding(a,r,1,ai.getBatchId(),(long)park*100000+1); service.bindTarget(first);
        long req=(long)park*100+2;
        jdbc.update("INSERT INTO SMT_AUTH_DELETE_REQUEST (ID,BATCH_ID,PARK_ID,SUBJECT_TYPE,SOURCE_TYPE,SOURCE_ROW_ID,SOURCE_IDENTITY_KEY,IDENTITY_SNAPSHOT,GENERATION,DEADLINE_AT,STATUS,CREATE_TIME,UPDATE_TIME) VALUES (?,?,?,'STAFF','7',?,?,'{}',?,SYSTIMESTAMP+1,'PREPARING',SYSTIMESTAMP,SYSTIMESTAMP)",req,bi.getBatchId(),park,b.getSourceRowId(),b.getSourceId(),b.getGeneration());
        Binding shared=first.toBuilder().sourceId(b.getSourceId()).sourceGeneration(b.getGeneration()).requestId(req).build();
        service.bindTarget(shared);
        SourceIntent ci=intent("cross",1,2).toBuilder().batchId((long)park+900000000L).build();
        SourceVersion c=service.reserveSourceIntent(ci); ResourceDecision same=stage(c,key("staff","DIRECT"),3);
        Binding own=persistBinding(c,same,3,ci.getBatchId(),(long)park*100000+2);
        try {service.bindTarget(own.toBuilder().targetId(first.getTargetId()).build());Assert.fail("跨批次不能只复用别批target");}
        catch(IllegalArgumentException expected) { }
        Assert.assertEquals("REUSE_PENDING",service.bindTarget(own).getOutcome());
        Assert.assertEquals("READY",service.bindTarget(first).getOutcome());
        Assert.assertEquals(first.getTargetId(),service.bindTarget(own).getReuseTargetId());
        List<Boolean> attempts=parallel(() -> {try{service.bindAttempt(first);return true;}catch(IllegalArgumentException expected){return false;}},
            () -> {try{service.bindAttempt(own);return true;}catch(IllegalArgumentException expected){return false;}});
        Assert.assertEquals(1,attempts.stream().filter(Boolean::booleanValue).count());
        Assert.assertEquals("BLOCKED",service.bindTarget(own).getOutcome());
        try {service.bindAttempt(own);Assert.fail("同资源后继必须等待");}catch(IllegalArgumentException expected) { }
    }

    @Test
    public void bindingRejectsWrongWindowAndExplicitlyReportsUnsupportedMultipleWindows() {
        SourceIntent si=intent("window",1,2); SourceVersion a=service.reserveSourceIntent(si);
        ResourceDecision r=stage(a,key("staff","DIRECT"),1); Binding b=persistBinding(a,r,1,si.getBatchId());
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET VALID_TO=VALID_TO+1 WHERE ID=?",b.getTargetId());
        try {service.bindTarget(b);Assert.fail("目标不能扩大当前有效期");}catch(IllegalArgumentException expected) { }
        SourceVersion other=service.reserveSourceIntent(intent("window-other",4,5));
        ResourceDecision multi=stage(other,key("staff","DIRECT"),2); Binding mb=persistBinding(other,multi,2,si.getBatchId());
        Assert.assertEquals("MULTI_WINDOW_UNSUPPORTED",service.bindTarget(mb).getOutcome());
    }

    @Test
    public void subjectLockSerializesRevocationAndEvidenceAcrossConnections() throws Exception {
        SourceIntent bi=intent("linear-b",2,3);SourceVersion b=service.reserveSourceIntent(bi);
        stage(b,key("staff","DIRECT"),2);sealAndSetActive(b);
        SourceIntent ai=intent("linear-a",1,2);SourceVersion a=service.reserveSourceIntent(ai);
        ResourceDecision d=stage(a,key("staff","DIRECT"),1);Binding binding=persistBinding(a,d,1,ai.getBatchId());
        service.bindTarget(binding);service.bindAttempt(binding);
        CountDownLatch reserved=new CountDownLatch(1), release=new CountDownLatch(1), applying=new CountDownLatch(1);
        ExecutorService pool=Executors.newFixedThreadPool(3);
        Future<?> revoke=null;Future<EvidenceResult> receipt=null;
        try {
            revoke=pool.submit(() -> tx.execute(status -> {
                service.reserveSourceIntent(bi.toBuilder().intentKey("linear-delete").action("DELETE").build());
                reserved.countDown();try {Assert.assertTrue(release.await(10,TimeUnit.SECONDS));}
                catch(InterruptedException e){throw new RuntimeException(e);}return null;
            }));
            Assert.assertTrue(reserved.await(5,TimeUnit.SECONDS));
            receipt=pool.submit(() -> {applying.countDown();return service.applyEvidence(Evidence.builder().binding(binding)
                .trusted(true).action("ADD").sourceRowId(a.getSourceRowId()).sourceFingerprint(a.getSourceFingerprint()).build());});
            Assert.assertTrue(applying.await(5,TimeUnit.SECONDS));
            Future<SourceVersion> other=pool.submit(() -> service.reserveSourceIntent(intent("different-person",1,2).toBuilder().subjectId("other-person").build()));
            Assert.assertEquals(1L,other.get(5,TimeUnit.SECONDS).getGeneration());
            Thread.sleep(150);
            Assert.assertFalse("同主体撤权事务未提交，回执必须等待共同协调锁",receipt.isDone());
            release.countDown();revoke.get(5,TimeUnit.SECONDS);
            Assert.assertFalse("撤权先线性化，旧窗口证据不能应用",receipt.get(5,TimeUnit.SECONDS).isMayApply());
        } finally {release.countDown();if(revoke!=null)revoke.get(10,TimeUnit.SECONDS);if(receipt!=null)receipt.get(10,TimeUnit.SECONDS);pool.shutdownNow();Assert.assertTrue(pool.awaitTermination(5,TimeUnit.SECONDS));}
    }

    @Test
    public void thousandTombstonesDoNotConsumeLiveSourceBudget() {
        SourceVersion active=service.reserveSourceIntent(intent("live",1,2));ResourceDecision d=stage(active,key("staff","DIRECT"),1);
        jdbc.update("INSERT INTO SMT_AUTH_SOURCE_COORD (ID,PARK_ID,SOURCE_KIND,STABLE_KEY,SUBJECT_TYPE,SUBJECT_ID,SOURCE_ROW_ID,SOURCE_FINGERPRINT,GENERATION,INTENT_KEY,INTENT_FINGERPRINT,BATCH_ID,ACTION,STATE,EXPANDED,WINDOWS,CREATE_TIME,UPDATE_TIME) "
            +"SELECT LPAD(TO_CHAR(LEVEL),64,'0'),?,'HISTORY','history-'||LEVEL,'STAFF','staff','old','fp',1,'history','fp',?,'DELETE','TOMBSTONE',1,TO_CLOB('#'),SYSTIMESTAMP,SYSTIMESTAMP FROM DUAL CONNECT BY LEVEL<=1000",park,(long)park);
        jdbc.update("INSERT INTO SMT_AUTH_SOURCE_RESOURCE (ID,SOURCE_COORD_ID,SOURCE_GENERATION,RESOURCE_COORD_ID,RESOURCE_GENERATION,SOURCE_ROW_ID,SOURCE_FINGERPRINT,WINDOWS,ACTION,STATE,REQUEST_ID,CREATE_TIME,UPDATE_TIME,INTENT_KEY,INTENT_FINGERPRINT) "
            +"SELECT ID,ID,1,?,1,'old','fp',TO_CLOB('#'),'DELETE','CONVERGED',1,SYSTIMESTAMP,SYSTIMESTAMP,'history','fp' FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=? AND SOURCE_KIND='HISTORY'",d.getResourceId(),park);
        Assert.assertEquals("ADD",service.currentDesired(d.getResourceId()).getAction());
        Assert.assertEquals(d.getBasisFingerprint(),service.currentDesired(d.getResourceId()).getBasisFingerprint());
        jdbc.update("UPDATE SMT_AUTH_SOURCE_COORD SET ACTION='ADD',STATE='ACTIVE' WHERE PARK_ID=? AND SOURCE_KIND='HISTORY'",park);
        jdbc.update("UPDATE SMT_AUTH_SOURCE_RESOURCE SET ACTION='ADD',STATE='ACTIVE',WINDOWS=TO_CLOB('2030-01-01T00:00/2030-01-02T00:00') WHERE SOURCE_COORD_ID IN (SELECT ID FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=? AND SOURCE_KIND='HISTORY')",park);
        try {service.currentDesired(d.getResourceId());Assert.fail("真正超过1000个有效来源必须拒绝");}
        catch(IllegalArgumentException expected){Assert.assertTrue(expected.getMessage().contains("来源"));}
    }

    @Test
    public void coveringDevicesUsesExplicitExclusionAndPreservesOtherSourceWindow() {
        for(boolean shared:new boolean[]{false,true}) {
            String suffix=shared?"shared":"alone";ResourceKey d1=key("staff","DIRECT").toBuilder().deviceId("d1-"+suffix).build();
            ResourceKey d2=d1.toBuilder().deviceId("d2-"+suffix).build(), d3=d1.toBuilder().deviceId("d3-"+suffix).build();
            SourceIntent si=intent("cover-"+suffix,1,2);SourceVersion old=service.reserveSourceIntent(si);
            stage(old,d1,1);stage(old,d2,1);sealAndSetActive(old);
            if(shared){SourceVersion another=service.reserveSourceIntent(intent("survivor",4,5));stage(another,d1,2);}
            SourceVersion next=service.reserveSourceIntent(si.toBuilder().intentKey("cover-next").build());
            ContributionCommand excluded=ContributionCommand.builder().sourceId(next.getSourceId()).sourceGeneration(next.getGeneration()).resource(d1)
                .requestId((long)park*100+3).participation("EXCLUDE").build();
            ResourceDecision result=service.stageContribution(excluded);
            Assert.assertEquals(shared?"ADD":"DELETE",result.getAction());
            if(shared)Assert.assertEquals(LocalDateTime.of(2030,1,4,0,0),result.getWindows().get(0).getFrom());
            Assert.assertEquals("ADD",stage(next,d2,3).getAction());Assert.assertEquals("ADD",stage(next,d3,3).getAction());
            try {service.stageContribution(excluded.toBuilder().participation("INCLUDE").build());Assert.fail("重试不能悄悄改资源排除");}
            catch(IllegalArgumentException expected) { }
        }
    }

    @Test
    public void rebasingContributionPreservesOldBindingAndAllowsCurrentTarget() {
        SourceIntent ai=intent("recover-a",1,2);SourceVersion a=service.reserveSourceIntent(ai);
        ResourceDecision first=stage(a,key("staff","DIRECT"),1);Binding old=persistBinding(a,first,1,ai.getBatchId());
        service.bindTarget(old);service.bindAttempt(old);service.markUnknown(old);
        SourceVersion b=service.reserveSourceIntent(intent("recover-b",2,3));ResourceDecision latest=stage(b,key("staff","DIRECT"),2);
        ContributionRecovery recovered=service.recoverContribution(ContributionCommand.builder().sourceId(a.getSourceId()).sourceGeneration(a.getGeneration())
            .resource(key("staff","DIRECT")).requestId((long)park*100+1).build());
        Assert.assertEquals(old.getTargetId(),recovered.getPreviousBinding().getTargetId());
        Assert.assertEquals(latest.getGeneration(),recovered.getCurrentBinding().getResourceGeneration());
        Binding current=persistBinding(a,recovered.getCurrent(),1,ai.getBatchId());
        Assert.assertEquals("BLOCKED",service.bindTarget(current).getOutcome());
        EvidenceResult stale=service.applyEvidence(Evidence.builder().binding(old).action("ADD").trusted(true)
            .sourceRowId(a.getSourceRowId()).sourceFingerprint(a.getSourceFingerprint()).build());
        Assert.assertTrue(stale.isCompensationRequired());service.bindAttempt(current);
        Assert.assertTrue(service.applyEvidence(Evidence.builder().binding(current).action("ADD").trusted(true)
            .sourceRowId(a.getSourceRowId()).sourceFingerprint(a.getSourceFingerprint()).build()).isMayApply());
    }

    @Test
    public void lateDeleteInvalidatesAppliedOrExecutingAddOnceAndPreservesItsBlocker() {
        for(boolean inFlight:new boolean[]{false,true}) {
            String suffix=inFlight?"executing":"applied";
            ResourceKey key=key("staff-"+suffix,"DIRECT");
            SourceIntent di=intent("late-delete-"+suffix,1,2).toBuilder().subjectId(key.getSubjectId()).action("DELETE").build();
            SourceVersion deleted=service.reserveSourceIntent(di);ResourceDecision oldDecision=stage(deleted,key,inFlight?11:1);
            Binding old=persistBinding(deleted,oldDecision,inFlight?11:1,di.getBatchId());service.bindTarget(old);
            // 模拟接管时已有的历史执行尝试；不把合成记录描述为真实设备已下发。
            jdbc.update("UPDATE SMT_AUTH_SOURCE_RESOURCE SET ATTEMPT_ID=? WHERE TARGET_ID=?",old.getAttemptId(),old.getTargetId());
            SourceIntent ai=intent("late-add-"+suffix,4,5).toBuilder().subjectId(key.getSubjectId()).build();
            SourceVersion added=service.reserveSourceIntent(ai);ResourceDecision current=stage(added,key,inFlight?12:2);
            Binding now=persistBinding(added,current,inFlight?12:2,ai.getBatchId());service.bindTarget(now);service.bindAttempt(now);
            Evidence currentEvidence=Evidence.builder().binding(now).trusted(true).action("ADD").sourceRowId(added.getSourceRowId()).sourceFingerprint(added.getSourceFingerprint()).build();
            if(!inFlight)Assert.assertTrue(service.applyEvidence(currentEvidence).isMayApply());
            Evidence late=Evidence.builder().binding(old).trusted(true).action("DELETE").sourceRowId(deleted.getSourceRowId()).sourceFingerprint(deleted.getSourceFingerprint()).build();
            EvidenceResult result=service.applyEvidence(late);
            Assert.assertEquals(current.getGeneration()+1,result.getCurrent().getGeneration());
            Assert.assertTrue(result.getCurrent().getAppliedGeneration()<result.getCurrent().getGeneration());
            Assert.assertEquals(inFlight?now.getAttemptId():null,result.getCurrent().getBlockingAttemptId());
            Assert.assertEquals(result.getCurrent().getGeneration(),service.applyEvidence(late).getCurrent().getGeneration());
            ContributionCommand recover=ContributionCommand.builder().sourceId(added.getSourceId()).sourceGeneration(added.getGeneration()).resource(key)
                .requestId((long)park*100+(inFlight?12:2)).build();
            ContributionRecovery next=service.recoverContribution(recover);
            if(inFlight) {Assert.assertTrue(service.applyEvidence(currentEvidence).isCompensationRequired());}
            Binding compensation=persistBinding(added,next.getCurrent(),inFlight?22:3,ai.getBatchId()+900000000L+(inFlight?1:0));
            service.bindRecoveryTarget(compensation);service.bindAttempt(compensation);
            Assert.assertEquals(ai.getBatchId(),jdbc.queryForObject("SELECT BATCH_ID FROM SMT_AUTH_SOURCE_COORD WHERE ID=?",Long.class,added.getSourceId()));
            Assert.assertTrue(service.applyEvidence(currentEvidence.toBuilder().binding(compensation).build()).isMayApply());
            Assert.assertEquals(next.getCurrent().getGeneration(),service.applyEvidence(late).getCurrent().getGeneration());
        }
    }

    @Test
    public void tombstoneLateAddCompensationExecutesDeleteAndCompletes() {
        SourceIntent ai=intent("tombstone-compensation",1,2);SourceVersion a=service.reserveSourceIntent(ai);
        ResourceDecision initial=stage(a,key("staff","DIRECT"),1);Binding add=persistBinding(a,initial,1,ai.getBatchId());
        service.bindTarget(add);service.bindAttempt(add);
        Evidence oldAdd=Evidence.builder().binding(add).trusted(true).action("ADD").sourceRowId(a.getSourceRowId()).sourceFingerprint(a.getSourceFingerprint()).build();
        Assert.assertTrue(service.applyEvidence(oldAdd).isMayApply());
        service.sealSourceExpansion(a.getSourceId(),a.getGeneration(),a.getSourceFingerprint());
        service.completeSource(a.getSourceId(),a.getGeneration(),a.getSourceRowId(),a.getSourceFingerprint());
        SourceVersion deleted=service.reserveSourceIntent(ai.toBuilder().intentKey("delete").action("DELETE").build());
        ResourceDecision deletion=stage(deleted,key("staff","DIRECT"),2);apply(deleted,deletion,2,ai.getBatchId());
        service.sealSourceExpansion(deleted.getSourceId(),deleted.getGeneration(),deleted.getSourceFingerprint());
        Assert.assertEquals("TOMBSTONE",service.completeSource(deleted.getSourceId(),deleted.getGeneration(),deleted.getSourceRowId(),deleted.getSourceFingerprint()).getState());
        EvidenceResult late=service.applyEvidence(oldAdd);Assert.assertTrue(late.isCompensationRequired());
        Assert.assertEquals("DELETE",late.getCurrent().getAction());Assert.assertTrue(late.getCurrent().getSources().isEmpty());
        ContributionRecovery recovered=service.recoverContribution(ContributionCommand.builder().sourceId(deleted.getSourceId()).sourceGeneration(deleted.getGeneration())
            .resource(key("staff","DIRECT")).requestId((long)park*100+2).build());
        Binding compensation=persistBinding(deleted,recovered.getCurrent(),3,ai.getBatchId()+900000000L);
        Assert.assertEquals("READY",service.bindRecoveryTarget(compensation).getOutcome());service.bindAttempt(compensation);
        Assert.assertTrue(service.applyEvidence(oldAdd.toBuilder().binding(compensation).action("DELETE").build()).isMayApply());
        Assert.assertEquals("TOMBSTONE",service.completeSource(deleted.getSourceId(),deleted.getGeneration(),deleted.getSourceRowId(),deleted.getSourceFingerprint()).getState());
        ResourceDecision complete=service.currentDesired(initial.getResourceId());
        Assert.assertTrue(complete.getSources().isEmpty());Assert.assertEquals(complete.getGeneration(),complete.getAppliedGeneration());
        Assert.assertEquals(deleted.getGeneration(),jdbc.queryForObject("SELECT GENERATION FROM SMT_AUTH_SOURCE_COORD WHERE ID=?",Long.class,deleted.getSourceId()).longValue());
        Assert.assertEquals("STALE_REPLAY",service.applyEvidence(oldAdd).getOutcome());
    }

    @Test
    public void currentDesiredWaitsThenReadsCommittedResourceGeneration() throws Exception {
        SourceIntent ai=intent("fresh-read",1,2);SourceVersion a=service.reserveSourceIntent(ai);
        ResourceDecision initial=stage(a,key("staff","DIRECT"),1);sealAndSetActive(a);
        CountDownLatch advanced=new CountDownLatch(1),release=new CountDownLatch(1),reading=new CountDownLatch(1);
        ExecutorService pool=Executors.newFixedThreadPool(2);Future<ResourceDecision> writer=null,reader=null;
        try {
            writer=pool.submit(() -> tx.execute(status -> {
                SourceVersion b=service.reserveSourceIntent(intent("fresh-read-b",2,3));
                ResourceDecision next=stage(b,key("staff","DIRECT"),2);advanced.countDown();
                try {Assert.assertTrue(release.await(10,TimeUnit.SECONDS));}catch(InterruptedException e){throw new RuntimeException(e);}return next;
            }));
            Assert.assertTrue(advanced.await(5,TimeUnit.SECONDS));
            reader=pool.submit(() -> {reading.countDown();return service.currentDesired(initial.getResourceId());});
            Assert.assertTrue(reading.await(5,TimeUnit.SECONDS));Thread.sleep(150);
            Assert.assertFalse("新代次未提交，读取必须等待主体锁",reader.isDone());
            release.countDown();ResourceDecision committed=writer.get(5,TimeUnit.SECONDS),actual=reader.get(5,TimeUnit.SECONDS);
            Assert.assertTrue(committed.getGeneration()>initial.getGeneration());
            Assert.assertEquals(committed.getGeneration(),actual.getGeneration());
            Assert.assertEquals(committed.getDesiredFingerprint(),actual.getDesiredFingerprint());
        } finally {release.countDown();pool.shutdownNow();Assert.assertTrue(pool.awaitTermination(10,TimeUnit.SECONDS));}
    }

    private SourceIntent intent(String source,int from,int to) {
        return SourceIntent.builder().parkId(park).sourceKind("STAFF_AUTH").stableKey("staff:"+source)
            .subjectType("STAFF").subjectId("staff").sourceRowId("row-"+source).sourceFingerprint("fp-"+source)
            .intentKey("intent-"+source).batchId((long)park).payloadSnapshot("frozen-devices").action("ADD")
            .window(Window.builder().from(LocalDateTime.of(2030,1,from,0,0)).to(LocalDateTime.of(2030,1,to,0,0)).build()).build();
    }
    private ResourceKey key(String subject,String access) {
        return ResourceKey.builder().parkId(park).subjectType("STAFF").subjectId(subject).accessType(access)
            .deviceId("device").resourceType("CARD").resourceId("permission").serviceType("1").credentialChannel("CARD").build();
    }
    private ResourceDecision stage(SourceVersion s,ResourceKey k,long request) {
        return service.stageContribution(ContributionCommand.builder().sourceId(s.getSourceId()).sourceGeneration(s.getGeneration()).resource(k).requestId((long)park*100+request).build());
    }
    private void sealAndSetActive(SourceVersion s) {
        service.sealSourceExpansion(s.getSourceId(),s.getGeneration(),s.getSourceFingerprint());
        // 此竞争用例只需要已经存在的有效来源，设备证据另有独立用例。
        jdbc.update("UPDATE SMT_AUTH_SOURCE_COORD SET STATE='ACTIVE' WHERE ID=?",s.getSourceId());
    }
    private void alias(ResourceKey k,long generation,String value) {
        service.rememberAlias(AliasCommand.builder().resource(k).resourceGeneration(generation).aliasKind("CARD_NO").aliasValue(value).build());
    }
    private static long positive() { return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE; }
    private <T> List<T> parallel(Callable<T> a,Callable<T> b) throws Exception {
        CountDownLatch start=new CountDownLatch(1); ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Future<T> x=pool.submit(() -> {start.await();return a.call();});
            Future<T> y=pool.submit(() -> {start.await();return b.call();}); start.countDown();
            return Arrays.asList(x.get(30,TimeUnit.SECONDS),y.get(30,TimeUnit.SECONDS));
        } finally { pool.shutdownNow(); Assert.assertTrue(pool.awaitTermination(10,TimeUnit.SECONDS)); }
    }
    private void apply(SourceVersion s,ResourceDecision r,long request,long batch) {
        Binding b=persistBinding(s,r,request,batch); service.bindTarget(b); service.bindAttempt(b);
        Assert.assertTrue(service.applyEvidence(Evidence.builder().binding(b).trusted(true).action(r.getAction())
            .sourceRowId(s.getSourceRowId()).sourceFingerprint(s.getSourceFingerprint()).build()).isMayApply());
    }
    private Binding persistBinding(SourceVersion s,ResourceDecision r,long request,long batch) {
        return persistBinding(s,r,request,batch,null);
    }
    private Binding persistBinding(SourceVersion s,ResourceDecision r,long request,long batch,Long targetOverride) {
        if(jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",Integer.class,batch)==0)
            jdbc.update("INSERT INTO SMT_AUTH_OPERATION_BATCH (ID,PARK_ID,IDEMPOTENCY_KEY,ACTION,SOURCE_TYPE,SELECTION_SNAPSHOT,PAYLOAD_FINGERPRINT,EXPECTED_COUNT,EXPANDED_COUNT,EXPANSION_CURSOR,STATUS,ACCEPTED_AT,DEADLINE_AT,CREATE_TIME,UPDATE_TIME) VALUES (?,?,?,'DELETE','7','{}','fp',1,0,0,'PREPARING',SYSTIMESTAMP,SYSTIMESTAMP+1,SYSTIMESTAMP,SYSTIMESTAMP)",batch,park,"test-"+batch);
        long req=(long)park*100+request;
        if(jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_DELETE_REQUEST WHERE ID=?",Integer.class,req)==0)
        jdbc.update("INSERT INTO SMT_AUTH_DELETE_REQUEST (ID,BATCH_ID,PARK_ID,SUBJECT_TYPE,SOURCE_TYPE,SOURCE_ROW_ID,SOURCE_IDENTITY_KEY,IDENTITY_SNAPSHOT,GENERATION,DEADLINE_AT,STATUS,CREATE_TIME,UPDATE_TIME) VALUES (?,?,?,'STAFF','7',?,?,'{}',?,SYSTIMESTAMP+1,'PREPARING',SYSTIMESTAMP,SYSTIMESTAMP)",req,batch,park,s.getSourceRowId(),s.getSourceId(),s.getGeneration());
        long target=targetOverride==null?positive():targetOverride,attempt=positive();
        jdbc.update("INSERT INTO SMT_AUTH_OPERATION_TARGET (ID,BATCH_ID,REQUEST_ID,PARK_ID,TARGET_KEY,SUBJECT_TYPE,SUBJECT_ID,SUBJECT_SNAPSHOT,RESOURCE_TYPE,DEVICE_ID,RESOURCE_ID,ACCESS_TYPE,OPERATION_QUEUE,ACTION,OPERATION_VERSION,STATE,ACCEPTED_AT,CREATE_TIME,UPDATE_TIME) VALUES (?,?,?, ?,?,'STAFF',?,'{}','CARD','device',?,'DIRECT','CARD',?,?,'EXECUTING',SYSTIMESTAMP,SYSTIMESTAMP,SYSTIMESTAMP)",target,batch,req,park,"t-"+target,r.getResource().getSubjectId(),AuthOperationVersionService.canonicalTargetResourceId(r.getResource()),r.getAction(),r.getGeneration());
        jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET DEVICE_ID=?,ACCESS_TYPE=? WHERE ID=?",r.getResource().getDeviceId(),r.getResource().getAccessType(),target);
        if(!r.getWindows().isEmpty()) jdbc.update("UPDATE SMT_AUTH_OPERATION_TARGET SET VALID_FROM=?,VALID_TO=? WHERE ID=?",
            java.sql.Timestamp.valueOf(r.getWindows().get(0).getFrom()),java.sql.Timestamp.valueOf(r.getWindows().get(0).getTo()),target);
        jdbc.update("INSERT INTO SMT_AUTH_OPERATION_ATTEMPT (ID,TARGET_ID,ATTEMPT_NO,ACCESS_TYPE,STATUS,LEASE_TOKEN,CREATE_TIME,UPDATE_TIME) VALUES (?,?,1,'DIRECT','EXECUTING',?,SYSTIMESTAMP,SYSTIMESTAMP)",attempt,target,"l-"+attempt);
        return Binding.builder().sourceId(s.getSourceId()).sourceGeneration(s.getGeneration()).resourceId(r.getResourceId()).resourceGeneration(r.getGeneration()).requestId(req).targetId(target).attemptId(attempt).build();
    }
}
