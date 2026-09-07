package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.dto.authselection.AuthSelection.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.Window;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.EmployeeAuthOperationMapper;
import com.tce.smart.platform.core.mapper.AuthSelectionMapper;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.service.impl.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.*;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.ResourceKey;
import org.mockito.Mockito;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import com.tce.smart.platform.core.service.impl.AuthSelectionSnapshots;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import java.time.LocalDateTime;
import java.util.*;

/** 仅在单独授权的迁移后 Oracle 窗口运行；不执行 DDL，每例合成数据最终回滚。 */
public class AuthSelectionOracleTest {
 private HikariDataSource pool;private DataSourceTransactionManager transactions;private TransactionStatus tx;
 private JdbcTemplate jdbc;private EmployeeAuthOperationMapper mapper;private AuthSelectionMapper genericMapper;private long batch;private int park;
 private final Set<String> laneCoordinates=new HashSet<>();private int laneSequence;
 public static class ProbeBusiness implements BusinessSnapshot {
  private String rowId;private String name;
  public String getRowId(){return rowId;}public void setRowId(String v){rowId=v;}
  public String getName(){return name;}public void setName(String v){name=v;}
 }
 @Before public void setup() throws Exception {
  // 独立开关防止其他 Oracle suite 授权被误当作本次迁移写入授权。
  Assume.assumeTrue("true".equals(System.getenv("SMART_AUTH_SELECTION_ORACLE_ENABLED")));
  Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:32768/FREEPDB1",System.getenv("SMART_AUTH_ORACLE_URL"));
  Assert.assertEquals("SMART_AUTH_TEST",System.getenv("SMART_AUTH_ORACLE_USER"));
  pool=new HikariDataSource();pool.setJdbcUrl(System.getenv("SMART_AUTH_ORACLE_URL"));pool.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));pool.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));pool.setMaximumPoolSize(4);pool.setMinimumIdle(0);pool.setPoolName("selection-projection-test");
  jdbc=new JdbcTemplate(pool);jdbc.setQueryTimeout(30);
  Assert.assertEquals("先人工核实并执行精确增量迁移，本测试不创建表",Integer.valueOf(6),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_SELECTION_SOURCE' AND COLUMN_NAME IN ('SOURCE_KIND','SUBJECT_TYPE','SNAPSHOT_VERSION','BUSINESS_SNAPSHOT','PARENT_KIND','PARENT_ROW_ID')",Integer.class));
  Assert.assertEquals("Y",jdbc.queryForObject("SELECT NULLABLE FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_SELECTION_SOURCE' AND COLUMN_NAME='AUTH_ID'",String.class));
  MybatisConfiguration configuration=new MybatisConfiguration();configuration.setDefaultStatementTimeout(30);configuration.setMapUnderscoreToCamelCase(true);configuration.addMapper(EmployeeAuthOperationMapper.class);configuration.addMapper(AuthSelectionMapper.class);
  MybatisSqlSessionFactoryBean factory=new MybatisSqlSessionFactoryBean();factory.setDataSource(pool);factory.setConfiguration(configuration);factory.setMapperLocations(new org.springframework.core.io.Resource[]{new ClassPathResource("mapper/EmployeeAuthOperationMapper.xml"),new ClassPathResource("mapper/AuthSelectionMapper.xml")});
  SqlSessionTemplate session=new SqlSessionTemplate(factory.getObject());mapper=session.getMapper(EmployeeAuthOperationMapper.class);genericMapper=session.getMapper(AuthSelectionMapper.class);
  transactions=new DataSourceTransactionManager(pool);tx=transactions.getTransaction(new DefaultTransactionDefinition());
  park=700000000+(int)(Math.abs(UUID.randomUUID().getLeastSignificantBits()%100000000));batch=800000000000000000L+park;
 }
 @After public void cleanup(){try{
  if(tx!=null)transactions.rollback(tx);
  if(park>0){
   for(String table:Arrays.asList("SMT_AUTH_SELECTION_SOURCE","SMT_AUTH_SELECTION_RESOURCE","SMT_AUTH_OPERATION_BATCH","SMT_AUTH_OPERATION_TARGET","SMT_AUTH_SOURCE_COORD","SMT_AUTH_RESOURCE_COORD"))
    Assert.assertEquals("合成园区数据必须已回滚："+table,Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE PARK_ID=?",Integer.class,park));
   Assert.assertEquals("来源贡献必须已回滚",Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SOURCE_RESOURCE WHERE ID LIKE ?",Integer.class,"selection-lane-source-"+park+"-%"));
   System.out.println("SELECTION_ROLLBACK_VERIFIED park="+park+" tables=7 rows=0");
  }
 }finally{if(pool!=null)pool.close();}}
 @Test public void nullAuthAndNineteenDigitIdRoundTripBothSnapshotsAndMicroseconds() {
  String id="9223372036854775807";SmtAuthSelectionSource source=source(1,"VISITOR","VISITOR",id,null);source.setParentKind("VISITOR");source.setParentRowId("");
  ProbeBusiness business=new ProbeBusiness();business.setRowId(id);business.setName(String.join("",Collections.nCopies(2048,"旅途合成访客")));LocalDateTime time=LocalDateTime.parse("2026-09-05T12:30:01.123456");
  source.setBusinessSnapshot(AuthSelectionSnapshots.business(business,Collections.singletonList(Window.builder().from(time).to(time.plusDays(1)).build())));
  Assert.assertEquals(1,mapper.insertSources(Collections.singletonList(source)));
  SmtAuthSelectionResource resource=new SmtAuthSelectionResource();resource.setBatchId(batch);resource.setOrdinal(1L);resource.setSourceOrdinal(1L);resource.setParkId(park);resource.setSubjectType("VISITOR");resource.setSubjectId(id);resource.setDeviceId("selection-probe-door");resource.setResourceId(id);resource.setResourceType("PERSON");resource.setAccessType("DIRECT");resource.setServiceType("3");resource.setCredentialChannel("FACE");resource.setParticipation("EXCLUDE");resource.setValidFrom(time);resource.setValidTo(time.plusDays(1));resource.setCredentialVersion(1);
  PersonCredential credential=new PersonCredential();credential.setTaskCardNo(id);credential.setTaskDeviceType(1);credential.setTaskServiceType(3);credential.setName("旅途合成访客");credential.setImageId("file-ref");resource.setCredentialSnapshot(AuthSelectionSnapshots.credential(credential));
  Assert.assertEquals(1,mapper.insertResources(Collections.singletonList(resource)));
  SmtAuthSelectionSource reloaded=mapper.source(batch,1);Assert.assertNull(reloaded.getAuthId());Assert.assertNull(reloaded.getParentRowId());Assert.assertEquals(id,reloaded.getSubjectId());Assert.assertEquals(business.getName(),AuthSelectionSnapshots.business(reloaded,ProbeBusiness.class).getName());
  SmtAuthSelectionResource rr=mapper.resources(batch,0,200).get(0);Assert.assertEquals(time,rr.getValidFrom());Assert.assertEquals(credential,AuthSelectionSnapshots.credential(rr.getCredentialVersion(),rr.getCredentialSnapshot()));
 }
 @Test public void sameNumericIdSeparatesStaffPendingFromVisitorAndKeepsV0() {
  SmtAuthSelectionSource visitor=source(1,"VISITOR","VISITOR","9223372036854775807",null);mapper.insertSources(Collections.singletonList(visitor));
  Assert.assertEquals(0,mapper.pendingSubject(park,visitor.getSubjectId()));Assert.assertEquals(1,mapper.pendingTypedSubject(park,"VISITOR",visitor.getSubjectId()));
  SmtAuthSelectionSource staff=source(2,"STAFF_AUTH","STAFF",visitor.getSubjectId(),"123");staff.setSnapshotVersion(0);staff.setBusinessSnapshot(null);staff.setNewAuthType(2);staff.setOldId(17);mapper.insertSources(Collections.singletonList(staff));
  Assert.assertEquals(1,mapper.pendingSubject(park,visitor.getSubjectId()));Assert.assertEquals(1,mapper.pendingSource(park,visitor.getSubjectId(),"123"));Assert.assertEquals(1,mapper.pendingTypedSubject(park,"VISITOR",visitor.getSubjectId()));
  SmtAuthSelectionSource reloaded=mapper.source(batch,2);Assert.assertEquals(0,AuthSelectionSnapshots.version(reloaded));Assert.assertEquals(Integer.valueOf(17),reloaded.getOldId());Assert.assertNull(reloaded.getBusinessSnapshot());
  // 直接调用员工 Mapper 同样不能将访客种类解释为员工 CAS。
  visitor.setOldId(17);visitor.setAuthId("123");Assert.assertEquals(0,mapper.deleteExact(visitor));Assert.assertEquals(0,mapper.updateExact(visitor));Assert.assertEquals(0,mapper.insertNew(visitor));
 }
 @Test public void subjectIdUsesUtf8ByteLimitWhileClobKeepsUnicode() {
  Assert.assertEquals(Integer.valueOf(256),jdbc.queryForObject("SELECT DATA_LENGTH FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_SELECTION_SOURCE' AND COLUMN_NAME='SUBJECT_ID'",Integer.class));
  String within=String.join("",Collections.nCopies(85,"访"));SmtAuthSelectionSource source=source(1,"VISITOR","VISITOR",within,null);mapper.insertSources(Collections.singletonList(source));Assert.assertEquals(within,mapper.source(batch,1).getSubjectId());
  source.setOrdinal(2L);source.setSubjectId(within+"访");source.setStableKey("over-limit");
  try{mapper.insertSources(Collections.singletonList(source));Assert.fail("258字节必须拒绝");}catch(org.springframework.dao.DataAccessException expected){Assert.assertNotNull(expected.getCause());}
 }
 @Test public void omittedNewColumnsBridgeOldEmployeeSqlButExplicitNullIsRejected() {
  jdbc.update("INSERT INTO SMT_AUTH_SELECTION_SOURCE(BATCH_ID,ORDINAL,OPERATION_KEY,PARK_ID,SUBJECT_ID,AUTH_ID,STABLE_KEY,SOURCE_ROW_ID,FINGERPRINT,DESIRED_ACTION,STATE) VALUES(?,1,?,?,?,'123','legacy-key','17','legacy-fingerprint','DELETE','PENDING')",batch,"selection-probe-"+batch,park,"9223372036854775807");
  jdbc.update("INSERT INTO SMT_AUTH_SELECTION_RESOURCE(BATCH_ID,ORDINAL,SOURCE_ORDINAL,PARK_ID,SUBJECT_ID,DEVICE_ID,ACCESS_TYPE,RESOURCE_TYPE,RESOURCE_ID,SERVICE_TYPE,CREDENTIAL_CHANNEL,PARTICIPATION) VALUES(?,1,1,?,?,'probe-door','DIRECT','PERSON',?,'1','FACE','EXCLUDE')",batch,park,"9223372036854775807","9223372036854775807");
  SmtAuthSelectionSource staff=mapper.source(batch,1);Assert.assertEquals("STAFF_AUTH",staff.getSourceKind());Assert.assertEquals("STAFF",staff.getSubjectType());Assert.assertEquals(Integer.valueOf(0),staff.getSnapshotVersion());
  SmtAuthSelectionResource resource=mapper.resources(batch,0,1).get(0);Assert.assertEquals("STAFF",resource.getSubjectType());Assert.assertEquals(Integer.valueOf(0),resource.getCredentialVersion());
  SmtAuthSelectionSource invalid=source(2,"VISITOR","VISITOR","10",null);invalid.setSourceKind(null);
  try{mapper.insertSources(Collections.singletonList(invalid));Assert.fail("新Mapper显式NULL不能默认为员工");}catch(org.springframework.dao.DataAccessException expected){Assert.assertNotNull(expected.getCause());}
  Assert.assertNull(mapper.source(batch,2));
 }
 @Test public void lanesExcludeEverySameBatchBindingRevisionAndDeduplicateSharedSources() {
  laneBatch(batch);laneBatch(batch+1);
  String[] keys={"a-current","b-history","c-stale","d-other","e-none","f-dangling","g-shared","g-shared","h-free","h-free",null};
  for(int i=0;i<keys.length;i++)laneSelection(i+1,keys[i]);
  long current=laneTarget(batch,1,"PREPARING"),historical=laneTarget(batch,2,"FAILED"),other=laneTarget(batch+1,3,"PREPARING");
  laneContribution("a-current",current,0);laneContribution("b-history",historical,17);laneContribution("c-stale",current,-1);
  laneContribution("d-other",other,0);laneContribution("e-none",null,0);laneContribution("f-dangling",batch+99999,0);
  laneContribution("g-shared",other,0);laneContribution("g-shared",current,23);laneContribution("z-not-selected",current,0);
  // 失败target与正负历史修订仍是原查询认可的绑定，不能在集合差右侧擅自增加状态过滤。
  assertLanes(null,200,"d-other","e-none","f-dangling","h-free");
  assertLanes(laneKey("e-none"),200,"f-dangling","h-free");
  assertLanes(null,1,"d-other");assertLanes(laneKey("z"),200);
  Assert.assertTrue(mapper.lanes(batch+2,null,200).isEmpty());
 }
 @Test public void lanesKeepLexicalKeysetAndLimitAfterSetDifferenceAcrossTwoHundredRows() {
  laneBatch(batch);
  laneSelection(1,"19");laneSelection(2,"2");laneSelection(3,"9223372036854775807");
  for(int i=0;i<205;i++){String key=String.format(Locale.ROOT,"p%03d",i);laneSelection(4+i,key);}
  laneSelection(209,"p004");laneSelection(210,null);
  long target=laneTarget(batch,1,"PREPARING");
  for(int i=0;i<4;i++)laneContribution(String.format(Locale.ROOT,"p%03d",i),target,i);
  assertLanes(null,1,"19");assertLanes(laneKey("19"),1,"2");
  assertLanes(laneKey("2"),1,"9223372036854775807");
  List<String> first=new ArrayList<>(Arrays.asList("19","2","9223372036854775807"));
  for(int i=4;i<=200;i++)first.add(String.format(Locale.ROOT,"p%03d",i));
  assertLanes(null,200,first.toArray(new String[0]));
  assertLanes(laneKey("p200"),200,"p201","p202","p203","p204");
  assertLanes(laneKey("p204"),200);
  // Java非null空串仍进入after谓词，Oracle空串为NULL，应保持原来的空结果。
  assertLanes("",200);
 }

 @Test public void reviewedFamilyPersistsEverySourceAndRealMissingCredentialCoordinate() throws Exception {
  AuthSelectionService service=reviewService(null);
  SourceSelection<ProbeBusiness> empty=reviewSource("empty",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).build();
  SourceSelection<ProbeBusiness> missing=reviewSource("missing",SourceSelection.VerificationReason.MISSING_CREDENTIAL_EVIDENCE).resource(reviewResource(null)).build();
  PersonCredential credential=new PersonCredential();credential.setTaskCardNo("review-subject");credential.setTaskDeviceType(1);credential.setTaskServiceType(3);
  SourceSelection<ProbeBusiness> healthy=reviewSource("healthy",null).resource(reviewResource(credential)).build();
  String key="selection-review-"+park;
  Long accepted=service.acceptTyped(key,Arrays.asList(empty,missing,healthy),Collections.singleton(park)).getBatches().get(park).get(0);
  Assert.assertEquals("VERIFYING",jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",String.class,accepted));
  Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT EXPECTED_COUNT FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",Integer.class,accepted));
  Assert.assertTrue(jdbc.queryForObject("SELECT SELECTION_SNAPSHOT FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",String.class,accepted).startsWith("WF1:3:"));
  List<SmtAuthSelectionSource> rows=genericMapper.operation(key);Assert.assertEquals(3,rows.size());Assert.assertEquals(2,service.sourceVerificationCount(accepted));
  Assert.assertTrue(rows.stream().allMatch(r->"PENDING".equals(r.getState()) && r.getSourceCoordId()==null && "review-parent".equals(r.getParentRowId())));
  Assert.assertTrue(rows.stream().allMatch(r->"原始缺失事实".equals(AuthSelectionSnapshots.business(r,ProbeBusiness.class).getName())));
  List<SmtAuthSelectionResource> resources=genericMapper.resources(accepted,0,200);Assert.assertEquals(2,resources.size());
  SmtAuthSelectionResource incomplete=resources.stream().filter(r->r.getCredentialSnapshot()==null).findFirst().get();Assert.assertEquals(Integer.valueOf(1),incomplete.getCredentialVersion());Assert.assertEquals("review-door",incomplete.getDeviceId());
  Assert.assertEquals("MISSING_CREDENTIAL_EVIDENCE;MISSING_RESOURCE_EVIDENCE",genericMapper.verificationReason(accepted));
  Assert.assertEquals(3,genericMapper.pendingTypedSubject(park,"VISITOR","review-subject"));
  Assert.assertEquals(accepted,service.acceptTyped(key,Arrays.asList(empty,missing,healthy),Collections.singleton(park)).getBatches().get(park).get(0));
  Assert.assertTrue(service.pendingExpansionBatches(Collections.singletonList(park),null,100).isEmpty());
  Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?",Integer.class,park));
  Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_DELETE_REQUEST WHERE PARK_ID=?",Integer.class,park));
 }
 @Test public void zeroResourceReviewPersistsZeroExpectedWithoutVersionOrTarget() throws Exception {
  AuthSelectionService service=reviewService(null);
  Long accepted=service.acceptTyped("selection-review-zero-"+park,Collections.singletonList(reviewSource("empty",SourceSelection.VerificationReason.MISSING_RESOURCE_EVIDENCE).build()),Collections.singleton(park)).getBatches().get(park).get(0);
  Assert.assertEquals("VERIFYING",genericMapper.verificationReason(accepted)==null?null:jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",String.class,accepted));
  Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT EXPECTED_COUNT FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",Integer.class,accepted));
  Assert.assertEquals(1,genericMapper.unboundSelectionCount(accepted));Assert.assertTrue(genericMapper.resources(accepted,0,200).isEmpty());
  Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?",Integer.class,park));
  try{service.finish(accepted);Assert.fail("零资源未绑定来源不得封口");}catch(IllegalArgumentException expected){}
 }
 @Test public void failedReviewMarkRollsBackActualBatchAndAllSelectionWrites() throws Exception {assertReviewRollback("markVerification");}
 @Test public void failedReviewResourceWriteRollsBackActualBatchAndAllSelectionWrites() throws Exception {assertReviewRollback("insertResources");}
 private void assertReviewRollback(String fault) throws Exception {
  AuthSelectionService service=reviewService(fault);
  SourceSelection<ProbeBusiness> selected=reviewSource("missing",SourceSelection.VerificationReason.MISSING_CREDENTIAL_EVIDENCE).resource(reviewResource(null)).build();
  try{service.acceptTyped("selection-review-fault-"+park,Collections.singletonList(selected),Collections.singleton(park));Assert.fail("故障必须传播");}catch(IllegalArgumentException expected){}
  Assert.assertTrue("真实SQL已写入后才注入失败",reviewFaultReached);
  Assert.assertTrue("继承事务必须标记外层回滚",tx.isRollbackOnly());transactions.rollback(tx);tx=null;
  for(String table:Arrays.asList("SMT_AUTH_OPERATION_BATCH","SMT_AUTH_SELECTION_SOURCE","SMT_AUTH_SELECTION_RESOURCE"))Assert.assertEquals(table,Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE PARK_ID=?",Integer.class,park));
 }
 private boolean reviewFaultReached;
 private AuthSelectionService reviewService(String fault) throws Exception {
  MybatisConfiguration cfg=new MybatisConfiguration();cfg.setDefaultStatementTimeout(30);cfg.setMapUnderscoreToCamelCase(true);cfg.addMapper(SmtAuthOperationBatchMapper.class);
  MybatisSqlSessionFactoryBean factory=new MybatisSqlSessionFactoryBean();factory.setDataSource(pool);factory.setConfiguration(cfg);factory.setMapperLocations(new org.springframework.core.io.Resource[]{new ClassPathResource("mapper/SmtAuthOperationBatchMapper.xml")});
  SmtAuthOperationBatchMapper batches=new SqlSessionTemplate(factory.getObject()).getMapper(SmtAuthOperationBatchMapper.class);
  SmtAuthOperationTargetMapper targets=Mockito.mock(SmtAuthOperationTargetMapper.class);SmtAuthOperationAttemptMapper attempts=Mockito.mock(SmtAuthOperationAttemptMapper.class);
  AuthOperationService operations=reviewProxy(new AuthOperationService(batches,Mockito.mock(SmtAuthDeleteRequestMapper.class),targets,attempts,Mockito.mock(SmtAuthResultEventMapper.class)));
  AuthOperationWorkflowService workflow=reviewProxy(new AuthOperationWorkflowService(operations,Mockito.mock(AuthOperationVersionService.class),batches,targets,attempts,Mockito.mock(AuthOperationWorkflowMapper.class)));
  EmployeeAuthOperationMapper selected=mapper;
  if(fault!=null)selected=(EmployeeAuthOperationMapper)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{EmployeeAuthOperationMapper.class},(proxy,method,args)->{
   Object result;try{result=method.invoke(mapper,args);}catch(java.lang.reflect.InvocationTargetException ex){throw ex.getCause();}
   if(method.getName().equals(fault)) {
    Assert.assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=?",Integer.class,park)>0);
    Assert.assertTrue(jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=?",Integer.class,park)>0);reviewFaultReached=true;
    if("markVerification".equals(fault))return 0;
    throw new IllegalArgumentException("资源真实写入后的合成故障");
   }return result;
  });
  AuthSourceHandler<ProbeBusiness> handler=new AuthSourceHandler<ProbeBusiness>() {
   public SourceKind sourceKind(){return SourceKind.VISITOR;}public SubjectType subjectType(){return SubjectType.VISITOR;}public int snapshotVersion(){return 1;}public Class<ProbeBusiness> snapshotType(){return ProbeBusiness.class;}
   public void lockAndValidate(SourceSelection<ProbeBusiness> s){Assert.assertTrue(org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());Assert.assertEquals("review-parent",s.getParentRowId());}
   public boolean applyExact(SmtAuthSelectionSource s,ProbeBusiness business){throw new AssertionError("核验受理不能收敛业务");}
  };
  return reviewProxy(new EmployeeAuthOperationService(selected,workflow,new AuthSourceConvergenceRegistry(Collections.singletonList(handler))));
 }
 @SuppressWarnings("unchecked") private <T> T reviewProxy(T raw){ProxyFactory p=new ProxyFactory(raw);p.setProxyTargetClass(true);p.addAdvice(new TransactionInterceptor(transactions,new AnnotationTransactionAttributeSource()));return (T)p.getProxy();}
 private SourceSelection.SourceSelectionBuilder<ProbeBusiness> reviewSource(String stable,SourceSelection.VerificationReason reason) {
  ProbeBusiness business=new ProbeBusiness();business.setRowId("review-subject");business.setName("原始缺失事实");
  return SourceSelection.<ProbeBusiness>builder().parkId(park).sourceKind(SourceKind.VISITOR).subjectType(SubjectType.VISITOR).subjectId("review-subject").stableKey(stable).sourceRowId("review-subject").parentKind("VISITOR").parentRowId("review-parent").sourceType("3").action("DELETE").snapshotVersion(1).business(business).verificationReason(reason);
 }
 private SelectedResource reviewResource(Credential credential){return SelectedResource.builder().credential(credential).input(ResourceInput.builder().resource(ResourceKey.builder().parkId(park).subjectType("VISITOR").subjectId("review-subject").deviceId("review-door").accessType("DIRECT").resourceType("PERSON").resourceId("review-subject").serviceType("3").credentialChannel("FACE").build()).participation("EXCLUDE").build()).build();}
 private void assertLanes(String after,int limit,String... expectedKeys) {
  List<String> expected=new ArrayList<>();for(String key:expectedKeys)expected.add(laneKey(key));
  Assert.assertEquals("员工入口的集合、字典顺序与上限",expected,mapper.lanes(batch,after,limit));
  Assert.assertEquals("通用入口必须复用同一集合语义",expected,genericMapper.lanes(batch,after,limit));
  String original="SELECT RESOURCE_COORD_ID FROM (SELECT DISTINCT R.RESOURCE_COORD_ID FROM SMT_AUTH_SELECTION_RESOURCE R WHERE R.BATCH_ID=? AND R.RESOURCE_COORD_ID IS NOT NULL AND NOT EXISTS(SELECT 1 FROM SMT_AUTH_SOURCE_RESOURCE C JOIN SMT_AUTH_OPERATION_TARGET T ON T.ID=C.TARGET_ID WHERE C.RESOURCE_COORD_ID=R.RESOURCE_COORD_ID AND T.BATCH_ID=?)";
  List<Object> args=new ArrayList<>(Arrays.asList(batch,batch));if(after!=null){original+=" AND R.RESOURCE_COORD_ID>?";args.add(after);}
  original+=" ORDER BY RESOURCE_COORD_ID) WHERE ROWNUM<=?";args.add(limit);
  Assert.assertEquals("固定原SQL只作语义对照，期望值来自独立fixture",expected,jdbc.queryForList(original,args.toArray(),String.class));
 }
 private String laneKey(String key){return key==null?null:"selection-lane-"+park+"-"+key;}
 private void laneSelection(long ordinal,String key) {
  SmtAuthSelectionSource parent=source(ordinal,"STAFF_AUTH","STAFF","probe",Long.toString(ordinal));
  parent.setStableKey("lane-source-"+ordinal);parent.setSnapshotVersion(0);parent.setBusinessSnapshot(null);
  mapper.insertSources(Collections.singletonList(parent));
  jdbc.update("INSERT INTO SMT_AUTH_SELECTION_RESOURCE(BATCH_ID,ORDINAL,SOURCE_ORDINAL,PARK_ID,SUBJECT_ID,DEVICE_ID,ACCESS_TYPE,RESOURCE_TYPE,RESOURCE_ID,SERVICE_TYPE,CREDENTIAL_CHANNEL,PARTICIPATION,RESOURCE_COORD_ID) VALUES(?,?,?,?,'probe','probe-door','DIRECT','PERSON','probe','3','FACE','EXCLUDE',?)",batch,ordinal,ordinal,park,laneKey(key));
 }
 private void laneBatch(long id) {
  jdbc.update("INSERT INTO SMT_AUTH_OPERATION_BATCH(ID,PARK_ID,IDEMPOTENCY_KEY,ACTION,SOURCE_TYPE,SELECTION_SNAPSHOT,PAYLOAD_FINGERPRINT,EXPECTED_COUNT) VALUES(?,?,?,'DELETE','3','selection-lane-probe','selection-lane-probe',1000)",id,park,"selection-lane-"+id);
 }
 private long laneTarget(long targetBatch,int ordinal,String state) {
  long id=batch+100+ordinal;
  jdbc.update("INSERT INTO SMT_AUTH_OPERATION_TARGET(ID,BATCH_ID,PARK_ID,TARGET_KEY,SUBJECT_TYPE,SUBJECT_ID,RESOURCE_TYPE,DEVICE_ID,RESOURCE_ID,ACCESS_TYPE,OPERATION_QUEUE,ACTION,OPERATION_VERSION,STATE) VALUES(?,?,?,?,'STAFF','probe','PERSON','probe-door',?,'DIRECT','DELETE','DELETE',1,?)",id,targetBatch,park,"selection-lane-target-"+ordinal,"selection-lane-target-"+ordinal,state);return id;
 }
 private void laneContribution(String key,Long target,long revision) {
  String resource=laneKey(key),source="selection-lane-source-"+park+"-"+(++laneSequence);
  if(laneCoordinates.add(resource))jdbc.update("INSERT INTO SMT_AUTH_RESOURCE_COORD(ID,PARK_ID,SUBJECT_TYPE,SUBJECT_ID,ACCESS_TYPE,DEVICE_ID,RESOURCE_TYPE,RESOURCE_ID,SERVICE_TYPE,CREDENTIAL_CHANNEL,RESOURCE_KEY,GENERATION,APPLIED_GENERATION,ACTION,WINDOWS,DESIRED_FINGERPRINT,BASIS_FINGERPRINT,CREATE_TIME,UPDATE_TIME) VALUES(?,?,'STAFF','probe','DIRECT','probe-door','PERSON',?,'3','FACE',?,1,0,'DELETE','#','probe','probe',SYSTIMESTAMP,SYSTIMESTAMP)",resource,park,resource,resource);
  jdbc.update("INSERT INTO SMT_AUTH_SOURCE_COORD(ID,PARK_ID,SOURCE_KIND,STABLE_KEY,SUBJECT_TYPE,SUBJECT_ID,SOURCE_ROW_ID,SOURCE_FINGERPRINT,GENERATION,INTENT_KEY,INTENT_FINGERPRINT,BATCH_ID,ACTION,STATE,EXPANDED,WINDOWS,CREATE_TIME,UPDATE_TIME) VALUES(?,?,'STAFF_AUTH',?,'STAFF','probe',?,'probe',1,?,'probe',?,'DELETE','EXPANDING',1,'#',SYSTIMESTAMP,SYSTIMESTAMP)",source,park,source,source,source,batch);
  jdbc.update("INSERT INTO SMT_AUTH_SOURCE_RESOURCE(ID,SOURCE_COORD_ID,SOURCE_GENERATION,RESOURCE_COORD_ID,RESOURCE_GENERATION,SOURCE_ROW_ID,SOURCE_FINGERPRINT,WINDOWS,ACTION,STATE,REQUEST_ID,TARGET_ID,BINDING_REVISION,SOURCE_ACTION,INTENT_KEY,INTENT_FINGERPRINT,CREATE_TIME,UPDATE_TIME) VALUES(?,?,1,?,1,?,'probe','#','DELETE','PENDING_REMOVE',?,?,?,'DELETE',?,'probe',SYSTIMESTAMP,SYSTIMESTAMP)",source,source,resource,source,batch+999,target,revision,source);
 }
 private SmtAuthSelectionSource source(long ordinal,String kind,String subjectType,String id,String auth) {
  SmtAuthSelectionSource s=new SmtAuthSelectionSource();s.setBatchId(batch);s.setOrdinal(ordinal);s.setOperationKey("selection-probe-"+batch);s.setParkId(park);s.setSourceKind(kind);s.setSubjectType(subjectType);s.setSubjectId(id);s.setAuthId(auth);s.setStableKey(id);s.setSourceRowId(id);s.setFingerprint("selection-probe");s.setDesiredAction("DELETE");s.setState("PENDING");s.setSnapshotVersion(1);
  ProbeBusiness business=new ProbeBusiness();business.setRowId(id);s.setBusinessSnapshot(AuthSelectionSnapshots.business(business,Collections.emptyList()));return s;
 }
}
