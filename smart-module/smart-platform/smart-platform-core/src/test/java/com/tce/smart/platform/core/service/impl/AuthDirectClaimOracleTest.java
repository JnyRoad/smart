package com.tce.smart.platform.core.service.impl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.*;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.*;
import java.util.concurrent.*;
/** 单独授权后才运行的本机 Oracle 归属竞争；不在测试中自动执行 DDL。 */
public class AuthDirectClaimOracleTest {
 private static HikariDataSource pool;private JdbcTemplate jdbc;private AuthOperationTransportMapper phases;private AuthOperationDirectClaimService claims;private TransactionTemplate tx;private String instance;
 @Before public void setup() throws Exception {
  Assume.assumeTrue("true".equals(System.getenv("SMART_AUTH_DIRECT_CLAIM_ORACLE")));
  Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:32768/FREEPDB1",System.getenv("SMART_AUTH_ORACLE_URL"));Assert.assertEquals("SMART_AUTH_TEST",System.getenv("SMART_AUTH_ORACLE_USER"));
  if(pool==null){pool=new HikariDataSource();pool.setJdbcUrl(System.getenv("SMART_AUTH_ORACLE_URL"));pool.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));pool.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));pool.setMaximumPoolSize(4);pool.setMinimumIdle(0);pool.setConnectionTimeout(5000);pool.setPoolName("direct-claim-foundation-test");}
  jdbc=new JdbcTemplate(pool);instance="direct-claim-test-"+UUID.randomUUID();
  Assert.assertEquals(Integer.valueOf(44),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_TRANSPORT_PHASE'",Integer.class));Assert.assertEquals(Integer.valueOf(13),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_DIRECT_CLAIM'",Integer.class));
  Assert.assertEquals(Integer.valueOf(5),jdbc.queryForObject("SELECT COUNT(*) FROM USER_CONSTRAINTS WHERE TABLE_NAME='SMT_AUTH_DIRECT_CLAIM' AND CONSTRAINT_TYPE IN ('P','U','R','C') AND CONSTRAINT_NAME IN ('PK_AUTH_DIRECT_CLAIM','UK_AUTH_DIRECT_CLAIM','FK_AUTH_DIRECT_PHASE','CK_AUTH_DIRECT_KEY','CK_AUTH_DIRECT_PROOF') AND STATUS='ENABLED'",Integer.class));
  MybatisConfiguration cfg=new MybatisConfiguration();cfg.setMapUnderscoreToCamelCase(true);cfg.addMapper(AuthOperationTransportMapper.class);cfg.addMapper(AuthOperationDirectClaimMapper.class);
  MybatisSqlSessionFactoryBean f=new MybatisSqlSessionFactoryBean();f.setDataSource(pool);f.setConfiguration(cfg);f.setMapperLocations(new org.springframework.core.io.Resource[]{new ClassPathResource("mapper/AuthOperationTransportMapper.xml"),new ClassPathResource("mapper/AuthOperationDirectClaimMapper.xml")});SqlSessionTemplate sql=new SqlSessionTemplate(f.getObject());phases=sql.getMapper(AuthOperationTransportMapper.class);
  DataSourceTransactionManager tm=new DataSourceTransactionManager(pool);tx=new TransactionTemplate(tm);tx.setTimeout(8);ProxyFactory proxy=new ProxyFactory(new AuthOperationDirectClaimService(sql.getMapper(AuthOperationDirectClaimMapper.class)));proxy.setProxyTargetClass(true);proxy.addAdvice(new TransactionInterceptor(tm,new AnnotationTransactionAttributeSource()));claims=(AuthOperationDirectClaimService)proxy.getProxy();
 }
 @After public void cleanup(){if(jdbc==null||instance==null)return;jdbc.update("DELETE FROM SMT_AUTH_DIRECT_CLAIM WHERE FIRST_PHASE_ID IN (SELECT ID FROM SMT_AUTH_TRANSPORT_PHASE WHERE INSTANCE_ID=?)",instance);jdbc.update("DELETE FROM SMT_AUTH_TRANSPORT_PHASE WHERE INSTANCE_ID=?",instance);Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_DIRECT_CLAIM WHERE INSTANCE_ID=?",Integer.class,instance));Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_TRANSPORT_PHASE WHERE INSTANCE_ID=?",Integer.class,instance));}
 @AfterClass public static void close(){if(pool!=null){pool.close();Assert.assertTrue(pool.isClosed());System.out.println("directClaim poolClosed="+pool.isClosed());}}
 @Test public void phaseRoundTripPreservesTypedClobAndLegacyNulls(){SmtAuthTransportPhase p=save(AuthDirectClaimFoundationTest.vehicle(1,"8123456789012345678","粤B12345"));SmtAuthTransportPhase read=phases.byId(p.getId());Assert.assertEquals(p.getCredentialSnapshot(),read.getCredentialSnapshot());Assert.assertEquals(Integer.valueOf(1),read.getCredentialVersion());Assert.assertNotNull(AuthTransportCredentials.fromPhase(read));SmtAuthTransportPhase legacy=AuthDirectClaimFoundationTest.phase(2);legacy.setSubjectType("STAFF");legacy.setCredentialVersion(null);legacy.setCredentialSnapshot(null);legacy=save(legacy);Assert.assertNull(phases.byId(legacy.getId()).getCredentialVersion());Assert.assertNotNull(AuthTransportCredentials.fromPhase(phases.byId(legacy.getId())));}
 @Test public void mandatoryClaimCannotRunOutsideCallerTransaction(){SmtAuthTransportPhase p=save(AuthDirectClaimFoundationTest.phase(1));try{claims.claim(Collections.singletonList(p));Assert.fail();}catch(org.springframework.transaction.IllegalTransactionStateException expected){}Assert.assertEquals(0,count());}
 @Test public void concurrentFamilyCollisionProducesExactlyOneOwner() throws Exception {SmtAuthTransportPhase a=save(AuthDirectClaimFoundationTest.phase(1));SmtAuthTransportPhase b=AuthDirectClaimFoundationTest.phase(2);b.setSubjectType("VISITOR_FELLOW");b=save(b);final SmtAuthTransportPhase other=b;CountDownLatch go=new CountDownLatch(1);ExecutorService e=Executors.newFixedThreadPool(2);try{Future<Boolean> x=e.submit(()->race(a,go)),y=e.submit(()->race(other,go));go.countDown();Assert.assertNotEquals(x.get(15,TimeUnit.SECONDS),y.get(15,TimeUnit.SECONDS));Assert.assertEquals(1,count());}finally{e.shutdownNow();Assert.assertTrue(e.awaitTermination(15,TimeUnit.SECONDS));}}
 @Test public void secondVehicleKeyConflictRollsBackAllNewKeys(){SmtAuthTransportPhase a=save(AuthDirectClaimFoundationTest.vehicle(1,"71","粤B12345"));tx.execute(x->{claims.claim(Collections.singletonList(a));return null;});SmtAuthTransportPhase b=save(AuthDirectClaimFoundationTest.vehicle(2,"72","粤B12345"));try{tx.execute(x->{claims.claim(Collections.singletonList(b));return null;});Assert.fail();}catch(IllegalArgumentException expected){}Assert.assertEquals(2,count());Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_DIRECT_CLAIM WHERE INSTANCE_ID=? AND KEY_VALUE='72'",Integer.class,instance));}
 @Test public void callerRollbackRemovesClaimAndIntentTogether(){SmtAuthTransportPhase p=save(AuthDirectClaimFoundationTest.phase(1));try{tx.execute(x->{claims.claim(Collections.singletonList(p));jdbc.update("UPDATE SMT_AUTH_TRANSPORT_PHASE SET STATE='INTENT',REQUEST_KEY='test-intent' WHERE ID=?",p.getId());throw new IllegalStateException("回执之前的合成失败");});Assert.fail();}catch(IllegalStateException expected){}Assert.assertEquals(0,count());Assert.assertEquals("PREPARED",phases.byId(p.getId()).getState());}
 @Test public void sameOwnerReusesRowsAndDeviceScopeDoesNotResetOnParkChange(){SmtAuthTransportPhase p=save(AuthDirectClaimFoundationTest.phase(1));tx.execute(x->{claims.claim(Collections.singletonList(p));claims.claim(Collections.singletonList(p));return null;});Assert.assertEquals(1,count());SmtAuthTransportPhase q=AuthDirectClaimFoundationTest.phase(2);q.setParkId(2);q=save(q);final SmtAuthTransportPhase moved=q;try{tx.execute(x->{claims.claim(Collections.singletonList(moved));return null;});Assert.fail();}catch(IllegalArgumentException expected){}Assert.assertEquals(1,count());}
 private boolean race(SmtAuthTransportPhase p,CountDownLatch go)throws Exception{go.await(5,TimeUnit.SECONDS);try{tx.execute(x->{claims.claim(Collections.singletonList(p));return null;});return true;}catch(IllegalArgumentException expected){return false;}}
 private int count(){return jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_DIRECT_CLAIM WHERE INSTANCE_ID=?",Integer.class,instance);}
 private SmtAuthTransportPhase save(SmtAuthTransportPhase p){long id=IdWorker.getId();p.setId(id);p.setTargetId(id);p.setAttemptId(id);p.setTaskId(String.valueOf(id));p.setInstanceId(instance);p.setDeviceId("device-"+instance);Assert.assertEquals(1,phases.insert(p));return p;}
}
