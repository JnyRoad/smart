package com.tce.smart.schedule.service.platform.impl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.tce.smart.platform.core.dto.authselection.*;
import com.tce.smart.platform.core.dto.authselection.VisitorAuthSnapshot.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.VisitorAuthOperationMapper;
import com.tce.smart.platform.core.service.impl.*;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.*;
import static org.junit.Assert.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.Timestamp;
import java.util.*;
/** 候选真实Oracle行锁/精确核验测试：不执行DDL、不调用设备、不使用Mock CAS。 */
public class AuthOperationVisitorCoreOracleTest {
 private HikariDataSource pool;private JdbcTemplate jdbc;private VisitorAuthOperationMapper mapper;private TransactionTemplate tx;
 private boolean fixtureReady;private long parentId,fellowId,batch;private int park;private String device;
 @Before public void setup() throws Exception {
  String url=System.getenv("SMART_AUTH_ORACLE_URL");Assume.assumeTrue(url!=null);
  assertEquals("jdbc:oracle:thin:@//127.0.0.1:32768/FREEPDB1",url);assertEquals("SMART_AUTH_TEST",System.getenv("SMART_AUTH_ORACLE_USER"));
  pool=new HikariDataSource();pool.setJdbcUrl(url);pool.setUsername(System.getenv("SMART_AUTH_ORACLE_USER"));pool.setPassword(System.getenv("SMART_AUTH_ORACLE_PASSWORD"));pool.setMaximumPoolSize(4);pool.setMinimumIdle(0);pool.setPoolName("visitor-core-candidate");
  jdbc=new JdbcTemplate(pool);park=1000000+(int)Math.abs(UUID.randomUUID().getMostSignificantBits()%400000000);parentId=910000000000000000L+park;fellowId=parentId+1;batch=parentId+2;device="visitor-core-"+park;
  for(String table:Arrays.asList("SMT_VISITOR","SMT_FELLOW_VISITOR","SMT_SNAP_PERSON","SMT_SNAP_VEHICLE","SMT_AUTH_SELECTION_SOURCE"))assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME=?",Integer.class,table));
  fixtureReady=true;
  MybatisConfiguration cfg=new MybatisConfiguration();cfg.setMapUnderscoreToCamelCase(true);cfg.addMapper(VisitorAuthOperationMapper.class);
  MybatisSqlSessionFactoryBean factory=new MybatisSqlSessionFactoryBean();factory.setDataSource(pool);factory.setConfiguration(cfg);factory.setMapperLocations(new org.springframework.core.io.Resource[]{new ClassPathResource("mapper/VisitorAuthOperationMapper.xml")});
  mapper=new SqlSessionTemplate(factory.getObject()).getMapper(VisitorAuthOperationMapper.class);tx=new TransactionTemplate(new DataSourceTransactionManager(pool));
  jdbc.update("INSERT INTO SMT_VISITOR(ID,PARK_ID,CREATE_TIME,START_TIME,END_TIME,STATUS,DEL_FLAG,CAUSE,CERT_TYPE,CERT_NO,VISITOR_NAME,VISITOR_PHOTO_ID,IS_VEHICLE,VEHICLE_PLATE) VALUES(?,?,TIMESTAMP '2025-01-01 00:00:00.123456',TIMESTAMP '2025-02-01 00:00:00.123456',TIMESTAMP '2099-01-01 00:00:00.456123',0,0,1,0,'test-cert','合成访客','test-photo',1,'合成车牌')",parentId,park);
  jdbc.update("INSERT INTO SMT_FELLOW_VISITOR(ID,VISITOR_ID,CERT_TYPE,CERT_NO,FELLOW_NAME,FELLOW_PHOTO_ID) VALUES(?,?,0,NULL,'合成随行','fellow-photo')",fellowId,parentId);
  jdbc.update("INSERT INTO SMT_DEVICE(ID,PARK_ID) VALUES(?,?)",device,park);
 }
 @After public void cleanup(){
  try { if(jdbc!=null && fixtureReady && parentId!=0){
   jdbc.update("DELETE FROM SMT_AUTH_SELECTION_SOURCE WHERE BATCH_ID=?",batch);
   jdbc.update("DELETE FROM SMT_SNAP_PERSON WHERE ID=? AND PARK_ID=?",park,park);
   jdbc.update("DELETE FROM SMT_SNAP_VEHICLE WHERE ID=? AND PARK_ID=?",parentId,park);
   jdbc.update("DELETE FROM SMT_FELLOW_VISITOR WHERE ID=? AND VISITOR_ID=?",fellowId,parentId);
   jdbc.update("DELETE FROM SMT_VISITOR WHERE ID=? AND PARK_ID=?",parentId,park);
   jdbc.update("DELETE FROM SMT_DEVICE WHERE ID=? AND PARK_ID IN (?,?)",device,park,park+1);
   assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_SOURCE WHERE BATCH_ID=?",Integer.class,batch));
  }
  } finally {if(pool!=null)pool.close();}
 }
 private VisitorAuthSnapshot snapshot(){
  return tx.execute(status->{SmtVisitor p=mapper.lockParent(park,parentId);VisitorAuthSnapshot b=new VisitorAuthSnapshot();ParentFacts f=new ParentFacts();org.springframework.beans.BeanUtils.copyProperties(p,f);f.setCreateTime(((Timestamp)p.getCreateTime()).toLocalDateTime().toString());f.setStartTime(((Timestamp)p.getStartTime()).toLocalDateTime().toString());f.setEndTime(((Timestamp)p.getEndTime()).toLocalDateTime().toString());b.setParent(f);Evidence e=new Evidence();e.setTrigger(Trigger.APPROVAL);e.setApprovalBasis(ApprovalBasis.DATABASE_PARENT_STATUS_ZERO);e.setRawStatus(0);e.setRawDelFlag(0);e.setLeadHours(2);b.setEvidence(e);return b;});
 }
 private SmtAuthSelectionSource freeze(String kind,VisitorAuthSnapshot b){
  long id="VISITOR_FELLOW".equals(kind)?fellowId:parentId;String stable=part(parentId)+("VISITOR_FELLOW".equals(kind)?part(fellowId):"");
  String json=AuthSelectionSnapshots.business(b,Collections.emptyList());String action=b.getEvidence().getTrigger()==Trigger.APPROVAL?"ADD":"DELETE";
  jdbc.update("INSERT INTO SMT_AUTH_SELECTION_SOURCE(BATCH_ID,ORDINAL,OPERATION_KEY,PARK_ID,SUBJECT_ID,STABLE_KEY,SOURCE_ROW_ID,FINGERPRINT,DESIRED_ACTION,STATE,SOURCE_KIND,SUBJECT_TYPE,SNAPSHOT_VERSION,BUSINESS_SNAPSHOT,PARENT_KIND,PARENT_ROW_ID) VALUES(?,1,?,?,?,?,?,'oracle-proof',?,'PENDING',?,?,1,?,'VISITOR',?)",batch,"visitor-core-"+park,park,Long.toString(id),stable,Long.toString(id),action,kind,kind,json,Long.toString(parentId));
  return mapper.frozenSource(batch,1L);
 }
 private static String part(long id){String s=Long.toString(id);return s.length()+":"+s;}
 @Test public void realParentArrivalAndMicrosecondCasPreserveHistory(){
  VisitorAuthSnapshot b=snapshot();SmtAuthSelectionSource s=freeze("VISITOR",b);VisitorAuthSourceHandler h=new VisitorAuthSourceHandler(mapper);
  jdbc.update("UPDATE SMT_VISITOR SET STATUS=5,DEL_FLAG=1 WHERE ID=?",parentId);
  assertTrue(tx.execute(t->h.applyExact(s,b)));
  jdbc.update("UPDATE SMT_VISITOR SET END_TIME=TIMESTAMP '2099-01-01 00:00:00.456124' WHERE ID=?",parentId);
  assertFalse(tx.execute(t->h.applyExact(s,b)));
  assertEquals(Integer.valueOf(5),jdbc.queryForObject("SELECT STATUS FROM SMT_VISITOR WHERE ID=?",Integer.class,parentId));
  assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_FELLOW_VISITOR WHERE ID=?",Integer.class,fellowId));
 }
 @Test public void realFellowParentCasAndExpiryDoNotDeleteHistory(){
  jdbc.update("UPDATE SMT_VISITOR SET END_TIME=TIMESTAMP '2025-02-02 00:00:00.456123',STATUS=4 WHERE ID=?",parentId);
  VisitorAuthSnapshot b=snapshot();FellowFacts f=new FellowFacts();f.setId(fellowId);f.setVisitorId(parentId);f.setCertType(0);f.setFellowName("合成随行");f.setFellowPhotoId("fellow-photo");b.setFellow(f);b.getEvidence().setTrigger(Trigger.EXPIRY);b.getEvidence().setApprovalBasis(ApprovalBasis.NOT_APPLICABLE);b.getEvidence().setRawStatus(4);
  SmtAuthSelectionSource s=freeze("VISITOR_FELLOW",b);VisitorFellowAuthSourceHandler h=new VisitorFellowAuthSourceHandler(mapper);assertTrue(tx.execute(t->h.applyExact(s,b)));
  jdbc.update("UPDATE SMT_FELLOW_VISITOR SET CERT_NO='new-cert' WHERE ID=?",fellowId);assertFalse(tx.execute(t->h.applyExact(s,b)));
  assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_VISITOR WHERE ID=?",Integer.class,parentId));
 }
 @Test public void actualExitEvidenceChecksDeviceParkAndFamily(){
  VisitorAuthSnapshot b=snapshot();b.getEvidence().setTrigger(Trigger.VEHICLE_EXIT);b.getEvidence().setApprovalBasis(ApprovalBasis.NOT_APPLICABLE);b.getEvidence().setEventId(parentId);
  jdbc.update("INSERT INTO SMT_SNAP_VEHICLE(ID,PARK_ID,DEVICE_ID,DRIVER_ID,DRIVER_TYPE,VEHICLE_ASCRIPTION,CARD_NO,VEHICLE_PLATE,EVENT_TYPE,SNAP_TIME) VALUES(?,?,?,?,2,2,?,'合成车牌',2,TIMESTAMP '2025-02-01 00:01:00.123456')",parentId,park,device,parentId,Long.toString(parentId));
  SmtAuthSelectionSource s=freeze("VISITOR_VEHICLE",b);VisitorVehicleAuthSourceHandler h=new VisitorVehicleAuthSourceHandler(mapper);assertTrue(tx.execute(t->h.applyExact(s,b)));
  jdbc.update("UPDATE SMT_DEVICE SET PARK_ID=? WHERE ID=?",park+1,device);assertFalse(tx.execute(t->h.applyExact(s,b)));jdbc.update("UPDATE SMT_DEVICE SET PARK_ID=? WHERE ID=?",park,device);
 }
 @Test public void businessStatusAndFrozenSourceShareOuterRollback(){
  VisitorAuthSnapshot b=snapshot();try{tx.execute(t->{jdbc.update("UPDATE SMT_VISITOR SET STATUS=5 WHERE ID=?",parentId);freeze("VISITOR",b);throw new IllegalStateException("合成事务失败");});fail();}catch(IllegalStateException expected){}
  assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT STATUS FROM SMT_VISITOR WHERE ID=?",Integer.class,parentId));assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM SMT_AUTH_SELECTION_SOURCE WHERE BATCH_ID=?",Integer.class,batch));
 }

 @Test public void actualLockedDatabaseApprovalIsRequiredForAdmission(){VisitorAuthSnapshot b=snapshot();com.tce.smart.platform.core.dto.authselection.AuthSelection.SourceSelection<VisitorAuthSnapshot> source=com.tce.smart.platform.core.dto.authselection.AuthSelection.SourceSelection.<VisitorAuthSnapshot>builder().parkId(park).sourceKind(com.tce.smart.platform.core.dto.authselection.AuthSelection.SourceKind.VISITOR).subjectType(com.tce.smart.platform.core.dto.authselection.AuthSelection.SubjectType.VISITOR).subjectId(Long.toString(parentId)).sourceRowId(Long.toString(parentId)).stableKey(part(parentId)).parentKind("VISITOR").parentRowId(Long.toString(parentId)).action("ADD").snapshotVersion(1).business(b).window(com.tce.smart.platform.core.dto.authversion.AuthVersion.Window.builder().from(java.time.LocalDateTime.of(2025,1,31,22,0,0,123456000)).to(java.time.LocalDateTime.of(2099,1,1,0,0,0,456123000)).build()).build();VisitorAuthSourceHandler h=new VisitorAuthSourceHandler(mapper);tx.execute(t->{h.lockAndValidate(source);return null;});jdbc.update("UPDATE SMT_VISITOR SET STATUS=2 WHERE ID=?",parentId);try{tx.execute(t->{h.lockAndValidate(source);return null;});fail("数据库待审不能被DTO伪批准");}catch(IllegalArgumentException expected){}}
 @Test public void actualFellowExitRequiresSpecificChildAndDevicePark(){VisitorAuthSnapshot b=snapshot();FellowFacts f=new FellowFacts();f.setId(fellowId);f.setVisitorId(parentId);f.setCertType(0);f.setFellowName("合成随行");f.setFellowPhotoId("fellow-photo");b.setFellow(f);b.getEvidence().setTrigger(Trigger.FELLOW_EXIT);b.getEvidence().setApprovalBasis(ApprovalBasis.NOT_APPLICABLE);b.getEvidence().setEventId((long)park);jdbc.update("INSERT INTO SMT_SNAP_PERSON(ID,PARK_ID,DEVICE_ID,PERSON_ID,PERSON_TYPE,EVENT_TYPE,SNAP_TIME) VALUES(?,?,?,?,2,2,TIMESTAMP '2025-02-01 00:01:00.123456')",park,park,device,fellowId);SmtAuthSelectionSource row=freeze("VISITOR_FELLOW",b);VisitorFellowAuthSourceHandler h=new VisitorFellowAuthSourceHandler(mapper);assertTrue(tx.execute(t->h.applyExact(row,b)));jdbc.update("UPDATE SMT_SNAP_PERSON SET PERSON_ID=? WHERE ID=?",parentId,park);assertFalse(tx.execute(t->h.applyExact(row,b)));}
}
