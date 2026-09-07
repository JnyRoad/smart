package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.config.AuthOperationProperties;
import com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.HistoryEvidence;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.*;
import com.tce.smart.platform.core.service.impl.*;
import com.tce.smart.platform.service.impl.EmployeeAuthOperationAdapter;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import java.util.*;
import java.util.stream.Collectors;

/** 单独授权后验证历史发现与真实受理；不执行 DDL，不接设备，最终清理本例合成数据。 */
public class EmployeeAuthHistoryOracleTest {
 private HikariDataSource pool;private JdbcTemplate jdbc;private int park;private long subject;private int sequence;
 private EmployeeAuthOperationMapper mapper;private EmployeeAuthOperationService employee;private EmployeeAuthOperationAdapter adapter;
 private final List<String> deviceIds=new ArrayList<>();
 @Before public void setup() throws Exception {
  Assume.assumeTrue("true".equals(System.getenv("SMART_AUTH_EMPLOYEE_HISTORY_ORACLE_ENABLED")));
  Assert.assertEquals("jdbc:oracle:thin:@//127.0.0.1:32768/FREEPDB1",System.getenv("SMART_AUTH_ORACLE_URL"));
  Assert.assertEquals("SMART_AUTH_TEST",System.getenv("SMART_AUTH_ORACLE_USER"));
  pool=new HikariDataSource();pool.setJdbcUrl(System.getenv("SMART_AUTH_ORACLE_URL"));pool.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));pool.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));pool.setMaximumPoolSize(4);pool.setMinimumIdle(0);pool.setPoolName("employee-history-proof");
  jdbc=new JdbcTemplate(pool);jdbc.setQueryTimeout(30);
  Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME='SMT_AUTH_SELECTION_SOURCE' AND COLUMN_NAME='BUSINESS_SNAPSHOT' AND DATA_TYPE='CLOB'",Integer.class));
  park=650000000+(int)Math.abs(UUID.randomUUID().getLeastSignificantBits()%40000000);subject=9100000000L+park;
  MybatisConfiguration cfg=new MybatisConfiguration();cfg.setMapUnderscoreToCamelCase(true);cfg.setDefaultStatementTimeout(30);
  Class<?>[] types={SmtAuthOperationBatchMapper.class,SmtAuthDeleteRequestMapper.class,SmtAuthOperationTargetMapper.class,SmtAuthOperationAttemptMapper.class,SmtAuthResultEventMapper.class,SmtAuthSubjectCoordMapper.class,SmtAuthSourceCoordMapper.class,SmtAuthResourceCoordMapper.class,SmtAuthSourceResourceMapper.class,SmtAuthIdentityAliasMapper.class,AuthOperationWorkflowMapper.class,EmployeeAuthOperationMapper.class};
  List<Resource> xml=new ArrayList<>();for(Class<?> type:types){cfg.addMapper(type);xml.add(new ClassPathResource("mapper/"+type.getSimpleName()+".xml"));}
  MybatisSqlSessionFactoryBean factory=new MybatisSqlSessionFactoryBean();factory.setDataSource(pool);factory.setConfiguration(cfg);factory.setMapperLocations(xml.toArray(new Resource[0]));
  SqlSessionTemplate session=new SqlSessionTemplate(factory.getObject());DataSourceTransactionManager tm=new DataSourceTransactionManager(pool);
  AuthOperationService operations=proxy(new AuthOperationService(session.getMapper(SmtAuthOperationBatchMapper.class),session.getMapper(SmtAuthDeleteRequestMapper.class),session.getMapper(SmtAuthOperationTargetMapper.class),session.getMapper(SmtAuthOperationAttemptMapper.class),session.getMapper(SmtAuthResultEventMapper.class)),tm);
  AuthOperationVersionService versions=proxy(new AuthOperationVersionService(session.getMapper(SmtAuthSubjectCoordMapper.class),session.getMapper(SmtAuthSourceCoordMapper.class),session.getMapper(SmtAuthResourceCoordMapper.class),session.getMapper(SmtAuthSourceResourceMapper.class),session.getMapper(SmtAuthIdentityAliasMapper.class)),tm);
  AuthOperationWorkflowService workflow=proxy(new AuthOperationWorkflowService(operations,versions,session.getMapper(SmtAuthOperationBatchMapper.class),session.getMapper(SmtAuthOperationTargetMapper.class),session.getMapper(SmtAuthOperationAttemptMapper.class),session.getMapper(AuthOperationWorkflowMapper.class)),tm);
  mapper=session.getMapper(EmployeeAuthOperationMapper.class);employee=proxy(new EmployeeAuthOperationService(mapper,workflow),tm);
  AuthOperationProperties properties=new AuthOperationProperties();properties.setEnabled(true);properties.setEnabledParks(Collections.singleton(park));
  adapter=proxy(new EmployeeAuthOperationAdapter(properties,mapper,employee){@Override protected Set<Integer> allowedParks(){return Collections.singleton(park);}},tm);
  jdbc.update("INSERT INTO SMT_PARK_BU(ID,PARK_ID,COMP_ID) VALUES(?,?,?)",park,park,"history-"+park);
  for(int i=0;i<2;i++) {
   jdbc.update("INSERT INTO SMT_STAFF(ID,COMP_ID,STATUS,FACE_PIC_ID,NAME) VALUES(?,?,1,'history-image','合成员工')",subject+i,"history-"+park);
   jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY(ID,PARK_ID,TYPE) VALUES(?,?,1)",park+i,park);
   jdbc.update("INSERT INTO SMT_STAFF_DEVICE_AUTH(ID,STAFF_ID,AUTH_ID,CREATE_TIME,START_TIME,END_TIME,AUTH_TYPE) VALUES(?,?,?,TIMESTAMP '2026-09-01 09:00:00',TIMESTAMP '2026-09-01 00:00:00',TIMESTAMP '2026-09-30 00:00:00',2)",park+i,subject+i,park+i);
  }
 }
 @SuppressWarnings("unchecked") private <T>T proxy(T raw,DataSourceTransactionManager tm){ProxyFactory p=new ProxyFactory(raw);p.setProxyTargetClass(true);p.addAdvice(new TransactionInterceptor(tm,new AnnotationTransactionAttributeSource()));return (T)p.getProxy();}
 @After public void cleanup(){try {
  if(jdbc==null || park==0)return;
  for(String table:Arrays.asList("SMT_DEVICE_TASK","SMT_ISC_DEVICE_TASK","SMT_TASK_DOWN_RECORD","SMT_ISC_DOWN_RECORD"))jdbc.update("DELETE FROM "+table+" WHERE CARD_NO IN (?,?)",String.valueOf(subject),String.valueOf(subject+1));
  jdbc.update("DELETE FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=?",park);jdbc.update("DELETE FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=?",park);
  jdbc.update("DELETE FROM SMT_AUTH_RESULT_EVENT WHERE TARGET_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?)",park);
  jdbc.update("DELETE FROM SMT_AUTH_IDENTITY_ALIAS WHERE PARK_ID=?",park);
  jdbc.update("DELETE FROM SMT_AUTH_SOURCE_RESOURCE WHERE SOURCE_COORD_ID IN (SELECT ID FROM SMT_AUTH_SOURCE_COORD WHERE PARK_ID=?)",park);
  for(String table:Arrays.asList("SMT_AUTH_RESOURCE_COORD","SMT_AUTH_SOURCE_COORD","SMT_AUTH_SUBJECT_COORD"))jdbc.update("DELETE FROM "+table+" WHERE PARK_ID=?",park);
  jdbc.update("DELETE FROM SMT_AUTH_OPERATION_ATTEMPT WHERE TARGET_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?)",park);
  jdbc.update("DELETE FROM SMT_AUTH_OPERATION_TARGET WHERE PARK_ID=?",park);jdbc.update("DELETE FROM SMT_AUTH_DELETE_REQUEST WHERE PARK_ID=?",park);
  jdbc.update("DELETE FROM SMT_AUTH_WORKFLOW_SHARD WHERE BATCH_ID IN (SELECT ID FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?)",park);jdbc.update("DELETE FROM SMT_AUTH_OPERATION_BATCH WHERE PARK_ID=?",park);
  jdbc.update("DELETE FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID IN (?,?)",subject,subject+1);jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY_RELATION WHERE PARK_ID=?",park);jdbc.update("DELETE FROM SMT_DEVICE_AUTHORITY WHERE PARK_ID=?",park);
  for(String device:deviceIds)jdbc.update("DELETE FROM SMT_DEVICE WHERE ID=?",device);
  jdbc.update("DELETE FROM SMT_PARK_BU WHERE ID=?",park);jdbc.update("DELETE FROM SMT_STAFF WHERE ID IN (?,?)",subject,subject+1);
  for(String table:Arrays.asList("SMT_DEVICE_TASK","SMT_ISC_DEVICE_TASK","SMT_TASK_DOWN_RECORD","SMT_ISC_DOWN_RECORD"))Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE CARD_NO IN (?,?)",Integer.class,String.valueOf(subject),String.valueOf(subject+1)));
  for(String table:Arrays.asList("SMT_AUTH_SELECTION_SOURCE","SMT_AUTH_SELECTION_RESOURCE","SMT_AUTH_OPERATION_BATCH","SMT_AUTH_OPERATION_TARGET","SMT_AUTH_RESOURCE_COORD","SMT_AUTH_SOURCE_COORD"))Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE PARK_ID=?",Integer.class,park));
  System.out.println("EMPLOYEE_HISTORY_CLEANUP_VERIFIED park="+park+" rows=0");
 }finally{if(pool!=null)pool.close();}}
 @Test public void fourTablesKeepRawOneAndSevenButTwoIsOnlyRowEvidence() {
  String direct=device("direct",park,0),isc=device("isc",park,1);
  for(boolean isIsc:Arrays.asList(false,true))for(boolean task:Arrays.asList(false,true))for(int service:Arrays.asList(1,2,7))raw(isIsc,task,service,isIsc?isc:direct,park,null);
  List<SmtAuthSelectionResource> resources=mapper.historicalResources(Collections.singletonList(subject),Collections.singletonList(park));
  Assert.assertEquals(4,resources.size());Assert.assertEquals(new HashSet<>(Arrays.asList("DIRECT:1","DIRECT:7","ISC:1","ISC:7")),resources.stream().map(r->r.getAccessType()+":"+r.getServiceType()).collect(Collectors.toSet()));
  List<HistoryEvidence> rows=evidence();Assert.assertEquals(12,rows.size());
  for(String origin:Arrays.asList("DIRECT_TASK","DIRECT_DOWN_RECORD","ISC_TASK","ISC_DOWN_RECORD"))Assert.assertEquals(new HashSet<>(Arrays.asList("1","2","7")),rows.stream().filter(e->origin.equals(e.getOrigin())).map(HistoryEvidence::getServiceType).collect(Collectors.toSet()));
 }
 @Test public void taskOnlyAndDownOnlySevenRemainAfterCurrentAuthorityRelationRemoved() {
  String direct=device("task-only",park,0),isc=device("down-only",park,1);
  raw(false,true,7,direct,park,null);raw(true,false,7,isc,park,null);
  Assert.assertTrue(adapter.removeAuthority(park));
  List<SmtAuthSelectionResource> rows=jdbc.query("SELECT DEVICE_ID,SERVICE_TYPE,PARTICIPATION FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=?",new Object[]{park},(r,n)->{SmtAuthSelectionResource x=new SmtAuthSelectionResource();x.setDeviceId(r.getString(1));x.setServiceType(r.getString(2));x.setParticipation(r.getString(3));return x;});
  Assert.assertEquals(2,rows.size());for(SmtAuthSelectionResource r:rows){Assert.assertEquals("1",r.getServiceType());Assert.assertEquals("EXCLUDE",r.getParticipation());}
 }
 @Test public void completeV2RetainsRawColumnsAndUsesCanonicalExecution() {
  String direct=device("v2-direct",park,0),isc=device("v2-isc",park,1);
  for(boolean isIsc:Arrays.asList(false,true))for(boolean task:Arrays.asList(false,true)) {
   raw(isIsc,task,7,isIsc?isc:direct,park,null);String table=isIsc?(task?"SMT_ISC_DEVICE_TASK":"SMT_ISC_DOWN_RECORD"):(task?"SMT_DEVICE_TASK":"SMT_TASK_DOWN_RECORD");
   jdbc.update("UPDATE "+table+" SET IMAGE_ID='raw-image',GENERAL='raw-general',REMARK='raw-remark' WHERE CARD_NO=?",String.valueOf(subject));
   if(task)jdbc.update("UPDATE "+table+" SET START_TIME=1788220800,OVER_TIME=1790812799,TIMES=3,CODE=4,CONSUME=25 WHERE CARD_NO=?",String.valueOf(subject));
   else jdbc.update("UPDATE "+table+" SET START_TIME=TIMESTAMP '2026-09-01 00:00:00.123456',OVER_TIME=TIMESTAMP '2026-09-30 23:59:59.654321' WHERE CARD_NO=?",String.valueOf(subject));
  }
  jdbc.update("UPDATE SMT_DEVICE_TASK SET SERIAL_NO='raw-serial',CARD_TYPE=1 WHERE CARD_NO=?",String.valueOf(subject));
  jdbc.update("UPDATE SMT_ISC_DEVICE_TASK SET APPLY_ID=9007199254740993123,BATCH_ID=9007199254740993222,BADGE='raw-badge',OPT_USER='raw-operator' WHERE CARD_NO=?",String.valueOf(subject));
  List<HistoryEvidence> rows=evidence();Assert.assertEquals(4,rows.size());for(HistoryEvidence row:rows){Assert.assertEquals(Integer.valueOf(2),row.getEvidenceVersion());Assert.assertEquals("7",row.getServiceType());Assert.assertEquals("raw-image",row.getImageId());Assert.assertEquals("raw-general",row.getGeneral());Assert.assertEquals("raw-remark",row.getRemark());if(row.getOrigin().endsWith("_TASK")){Assert.assertEquals("EPOCH_SECONDS",row.getWindowEncoding());Assert.assertEquals("1788220800",row.getStartTime());Assert.assertEquals("3",row.getTimes());Assert.assertEquals("DEVICE_CURRENT",row.getParkOrigin());}else{Assert.assertEquals("RAW_RECORD",row.getParkOrigin());Assert.assertEquals("2026-09-01T00:00:00.123456000",row.getStartTime());Assert.assertNull(row.getUpdatedAt());}}
  Assert.assertEquals("9007199254740993123",rows.stream().filter(r->"ISC_TASK".equals(r.getOrigin())).findFirst().get().getApplyId());
  adapter.removeAuthority(park);SmtAuthSelectionSource source=frozen(subject);Assert.assertTrue(source.getBusinessSnapshot().contains("EMPLOYEE_HISTORY_EVIDENCE_V2"));Assert.assertEquals(4,com.tce.smart.platform.core.dto.employeeauth.EmployeeAuthOperation.decodeHistory(source.getBusinessSnapshot()).getRows().size());
  Assert.assertEquals(Integer.valueOf(2),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=? AND SERVICE_TYPE='1'",Integer.class,park));Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_RESOURCE WHERE PARK_ID=? AND SERVICE_TYPE='7'",Integer.class,park));
 }
 @Test public void unknownDeviceAndParkKeepEveryOriginalRowAndEveryReason() throws Exception {
  String nullPark=device("unknown-park",null,0);String known=device("known",park,1);
  raw(false,true,7,"deleted-"+park,park,null);raw(true,true,7,"deleted-isc-"+park,park,null);raw(false,true,7,nullPark,null,null);
  raw(false,false,7,null,null,null);raw(true,false,2,known,null,null);raw(true,false,2,known,park,null);
  Assert.assertEquals(6,evidence().size());Assert.assertEquals(2,mapper.unmappedTaskSubjects(Collections.singletonList(subject)).size());
  Assert.assertTrue(adapter.removeAuthority(park));SmtAuthSelectionSource source=frozen(subject);
  Assert.assertTrue(source.getVerificationReason().startsWith("APP_PERFECT_REVIEW"));Assert.assertTrue(source.getVerificationReason().contains("MISSING_DEVICE"));
  com.fasterxml.jackson.databind.JsonNode rows=new com.fasterxml.jackson.databind.ObjectMapper().readTree(source.getBusinessSnapshot()).get("rows");Assert.assertEquals(6,rows.size());
  Assert.assertEquals("VERIFYING",status(source.getBatchId()));Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_RESOURCE WHERE BATCH_ID=?",Integer.class,source.getBatchId()));
  Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",Integer.class,subject));
 }
 @Test public void duplicateAppRowsAndCoordRoundTripClobWithZeroResourcesWhileHealthyEmployeeStages() throws Exception {
  String appDevice=device("app",park,1),goodDevice=device("good",park,0);String external=String.join("",Collections.nCopies(200,"外"));
  raw(true,true,2,appDevice,park,external);raw(true,true,2,appDevice,park,"another-task");coord(appDevice,"2");
  jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",park,park+1,goodDevice,park);
  Assert.assertTrue(adapter.diff(Arrays.asList(subject,subject+1),Collections.emptyList(),Arrays.asList(park,park+1)));
  SmtAuthSelectionSource app=frozen(subject),good=frozen(subject+1);Assert.assertNotEquals(app.getBatchId(),good.getBatchId());
  Assert.assertEquals("VERIFYING",status(app.getBatchId()));Assert.assertEquals("PREPARING",status(good.getBatchId()));
  Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_RESOURCE WHERE BATCH_ID=?",Integer.class,app.getBatchId()));
  com.fasterxml.jackson.databind.JsonNode rows=new com.fasterxml.jackson.databind.ObjectMapper().readTree(app.getBusinessSnapshot()).get("rows");Assert.assertEquals(3,rows.size());
  Assert.assertTrue(app.getBusinessSnapshot().contains(external));for(com.fasterxml.jackson.databind.JsonNode row:rows)Assert.assertEquals("2",row.get("serviceType").asText());
  try{employee.stageNext(app.getBatchId());Assert.fail("APP核验批次不得展开");}catch(IllegalStateException expected){}
  Assert.assertTrue(employee.stageNext(good.getBatchId()));Assert.assertNotNull(mapper.source(good.getBatchId(),1).getSourceCoordId());
  Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?",Integer.class,app.getBatchId()));
  Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",Integer.class,subject));
 }

 @Test public void retainedOldRelationWithMovedDeviceIsolatesOnlyThatEmployee() {retainedOldRelationReview(park+2);}
 @Test public void retainedOldRelationWithNullDeviceParkIsolatesOnlyThatEmployee() {retainedOldRelationReview(null);}
 private void retainedOldRelationReview(Integer currentPark) {
  String oldDevice=device("retained-old",currentPark,0),goodDevice=device("retained-good",park,0);
  raw(false,false,7,oldDevice,park,null);
  jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",park,park,oldDevice,park);
  jdbc.update("INSERT INTO SMT_DEVICE_AUTHORITY_RELATION(ID,AUTHORITY_ID,DEVICE_ID,PARK_ID) VALUES(?,?,?,?)",park+1,park+1,goodDevice,park);
  Assert.assertTrue(adapter.diff(Arrays.asList(subject,subject+1),Collections.emptyList(),Arrays.asList(park,park+1)));
  SmtAuthSelectionSource bad=frozen(subject),good=frozen(subject+1);
  Assert.assertNotEquals(bad.getBatchId(),good.getBatchId());Assert.assertEquals("VERIFYING",status(bad.getBatchId()));Assert.assertEquals("PREPARING",status(good.getBatchId()));
  Assert.assertTrue(bad.getVerificationReason().startsWith("MISSING_DEVICE"));
  try {com.fasterxml.jackson.databind.JsonNode rows=new com.fasterxml.jackson.databind.ObjectMapper().readTree(bad.getBusinessSnapshot()).get("rows");Assert.assertEquals(1,rows.size());Assert.assertEquals("DIRECT_DOWN_RECORD",rows.get(0).get("origin").asText());Assert.assertEquals(park,rows.get(0).get("parkId").asInt());Assert.assertEquals("7",rows.get(0).get("serviceType").asText());Assert.assertEquals(oldDevice,rows.get(0).get("deviceId").asText());}catch(java.io.IOException error){throw new AssertionError(error);}
  Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_RESOURCE WHERE BATCH_ID=?",Integer.class,bad.getBatchId()));
  try{employee.stageNext(bad.getBatchId());Assert.fail("旧设备迁园核验不得展开");}catch(IllegalStateException expected){}
  Assert.assertTrue(employee.stageNext(good.getBatchId()));Assert.assertNotNull(mapper.source(good.getBatchId(),1).getSourceCoordId());
  Assert.assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_OPERATION_TARGET WHERE BATCH_ID=?",Integer.class,bad.getBatchId()));
  Assert.assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_STAFF_DEVICE_AUTH WHERE STAFF_ID=?",Integer.class,subject));
 }
 private List<HistoryEvidence> evidence(){return mapper.historicalReviewEvidence(Collections.singletonList(subject),Collections.singletonList(park));}
 private String status(Long batch){return jdbc.queryForObject("SELECT STATUS FROM SMT_AUTH_OPERATION_BATCH WHERE ID=?",String.class,batch);}
 private SmtAuthSelectionSource frozen(long id){Long batch=jdbc.queryForObject("SELECT BATCH_ID FROM SMT_AUTH_SELECTION_SOURCE WHERE PARK_ID=? AND SUBJECT_ID=?",Long.class,park,String.valueOf(id));return mapper.source(batch,1);}
 private String device(String name,Integer actualPark,int sync){String id="history-"+park+"-"+name;deviceIds.add(id);jdbc.update("INSERT INTO SMT_DEVICE(ID,PARK_ID,IS_SYNC) VALUES(?,?,?)",id,actualPark,sync);return id;}
 private void raw(boolean isc,boolean task,int service,String device,Integer historyPark,String external) {
  int id=park+100+(++sequence);String table=isc?(task?"SMT_ISC_DEVICE_TASK":"SMT_ISC_DOWN_RECORD"):(task?"SMT_DEVICE_TASK":"SMT_TASK_DOWN_RECORD");
  String columns="ID,DEVICE_TYPE,SERVICE_TYPE,DEVICE_CODE,CARD_NO,ACTION,CREATE_TIME";String values="?,1,?,?,?,1,SYSTIMESTAMP";List<Object> args=new ArrayList<>(Arrays.asList(id,service,device,String.valueOf(subject)));
  if(task){columns+=",STATUS,UPDATE_TIME";values+=",2,SYSTIMESTAMP";}else{columns+=",PARK_ID,TASK_ID,TASK_TYPE";values+=",?,?,2";args.add(historyPark);args.add(id+1000);}
  if(isc){columns+=",PERSON_ID";values+=",?";args.add("person-"+id);if(task){columns+=",ISC_TASK_ID";values+=",?";args.add(external==null?"external-"+id:external);}}
  jdbc.update("INSERT INTO "+table+"("+columns+") VALUES("+values+")",args.toArray());
 }
 private void coord(String device,String service){String id="history-coord-"+park;jdbc.update("INSERT INTO SMT_AUTH_RESOURCE_COORD(ID,PARK_ID,SUBJECT_TYPE,SUBJECT_ID,ACCESS_TYPE,DEVICE_ID,RESOURCE_TYPE,RESOURCE_ID,SERVICE_TYPE,CREDENTIAL_CHANNEL,RESOURCE_KEY,GENERATION,APPLIED_GENERATION,ACTION,WINDOWS,DESIRED_FINGERPRINT,BASIS_FINGERPRINT,CREATE_TIME,UPDATE_TIME) VALUES(?,?,'STAFF',?,'ISC',?,'PERSON',?,?,'FACE',?,1,0,'DELETE','#','history','history',SYSTIMESTAMP,SYSTIMESTAMP)",id,park,String.valueOf(subject),device,String.valueOf(subject),service,id);}
}
