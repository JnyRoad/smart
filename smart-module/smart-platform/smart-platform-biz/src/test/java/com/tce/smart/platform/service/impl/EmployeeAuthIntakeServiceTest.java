package com.tce.smart.platform.service.impl;

import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.AuthRequestIntakeMapper;
import com.tce.smart.platform.dto.authoperation.*;
import org.junit.*;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.*;
import java.sql.SQLException;
import java.util.*;

/** 隔离服务行为测试；事务存储替身不代替真实Oracle唯一键和回滚验收。 */
public class EmployeeAuthIntakeServiceTest {
 private Store store; private Tx tx; private EmployeeAuthIntakeService service; private int builds;
 @Before public void setup(){store=new Store();tx=new Tx(store);service=new EmployeeAuthIntakeService(store,tx);}
 private AuthOperationIntakeCommand request(){return AuthOperationIntakeCommand.builder().requestKey("persisted-request-key").requestKind("REMOVE_ROWS").authId(7).authorityType(1).rowId(19).build();}
 private Set<Integer> scope(){return new HashSet<>(Arrays.asList(1,2));}
 private AuthOperationIntakeAcceptance build(String key){
  Assert.assertTrue("受理回调必须在实际事务中",TransactionSynchronizationManager.isActualTransactionActive());builds++;
  List<SmtAuthOperationBatch> list=Arrays.asList(batch(9007199254740993L,1,key),batch(9007199254740995L,2,key));store.children.put(key,list);
  return AuthOperationIntakeAcceptance.builder().operationKey(key).outcome("ACCEPTED").scopeParkIds(scope()).batch(9007199254740993L,1).batch(9007199254740995L,2).build();
 }
 @Test public void lostResponseReplaysAllChildrenWithoutReadingDeletedBusinessSources(){
  AuthOperationIntakeReceipt first=service.submit(request(),8,scope(),this::build);Assert.assertNotNull("首次受理应返回持久结果",first);
  AuthOperationIntakeReceipt replay=service.submit(request(),8,scope(),key->{throw new AssertionError("来源已删，不可再次读取");});
  Assert.assertEquals(first.getOperationKey(),replay.getOperationKey());Assert.assertTrue(replay.isReplayed());Assert.assertEquals(1,builds);
  Assert.assertEquals(new HashSet<>(Arrays.asList("9007199254740993","9007199254740995")),replay.getBatchParks().keySet());
 }
 @Test public void differentOriginalPayloadWithSameKeyIsRejected(){
  Assert.assertNotNull(service.submit(request(),8,scope(),this::build));
  fails("KEY_PAYLOAD_CONFLICT",()->service.submit(request().toBuilder().authId(8).build(),8,scope(),key->{throw new AssertionError("冲突不能重新受理");}));
 }
 @Test public void losingOneParkRejectsWholeReplayRatherThanReturningVisibleSubset(){
  Assert.assertNotNull(service.submit(request(),8,scope(),this::build));
  try{service.submit(request(),8,Collections.singleton(1),key->{throw new AssertionError();});Assert.fail("不能只返回一个园区");}catch(SecurityException expected){}
 }
 @Test public void missingExtraReplacedOrChangedChildCannotBeCalledCompleteReplay(){
  AuthOperationIntakeReceipt first=service.submit(request(),8,scope(),this::build);Assert.assertNotNull(first);String key=first.getOperationKey();
  List<SmtAuthOperationBatch> original=store.children.get(key);
  List<List<SmtAuthOperationBatch>> mutations=Arrays.asList(Collections.singletonList(original.get(0)),Arrays.asList(original.get(0),original.get(1),batch(22,1,key)),Arrays.asList(original.get(0),batch(23,2,key)));
  for(List<SmtAuthOperationBatch> changed:mutations){store.children.put(key,changed);fails("INTAKE_INCOMPLETE",()->service.submit(request(),8,scope(),k->{throw new AssertionError();}));}
  store.children.put(key,original);original.get(1).setExpectedCount(9);
  fails("INTAKE_INCOMPLETE",()->service.submit(request(),8,scope(),k->{throw new AssertionError();}));
 }
 @Test public void mutableBatchProgressDoesNotInvalidateOriginalReceipt(){
  AuthOperationIntakeReceipt first=service.submit(request(),8,scope(),this::build);Assert.assertNotNull(first);
  for(SmtAuthOperationBatch b:store.children.get(first.getOperationKey())){b.setStatus("CONVERGED");b.setExpandedCount(1);}
  Assert.assertTrue(service.submit(request(),8,scope(),k->{throw new AssertionError();}).isReplayed());
 }
 @Test public void noChangeIsDurableAndDoesNotDeleteMembersAddedAfterwards(){
  AuthOperationIntakeCommand clear=request().toBuilder().requestKind("CLEAR_AUTHORITY").clearRowIds().build();
  AuthOperationIntakeReceipt first=service.submit(clear,8,scope(),key->AuthOperationIntakeAcceptance.builder().operationKey(key).outcome("NO_CHANGE").scopeParkId(1).build());
  Assert.assertNotNull(first);Assert.assertFalse(first.isSubmitted());Assert.assertNull(first.getOperationKey());
  Assert.assertEquals("NO_CHANGE",service.submit(clear,8,scope(),key->{throw new AssertionError("后加成员不应再次处理");}).getMode());
 }
 @Test public void firstAcceptanceFailureRollsBackReservationAndAllowsSameKeyAgain(){
  try{service.submit(request(),8,scope(),key->{build(key);throw new IllegalStateException("业务冻结失败");});Assert.fail("失败不可被吞掉");}catch(IllegalStateException expected){}
  Assert.assertTrue(store.rows.isEmpty());Assert.assertTrue(store.children.isEmpty());Assert.assertEquals(1,tx.rollbacks);
  Assert.assertNotNull(service.submit(request(),8,scope(),this::build));
 }
 @Test public void invalidSelectionOrIncompleteCallbackRollsBackAllChildren(){
  store.invalid=1;
  fails("INTAKE_INCOMPLETE",()->service.submit(request(),8,scope(),this::build));
  Assert.assertTrue(store.rows.isEmpty());Assert.assertTrue(store.children.isEmpty());
 }
 @Test public void headerUniqueCollisionReadsWinnerOnlyAfterRollback(){
  AuthOperationIntakeReceipt first=service.submit(request(),8,scope(),this::build);Assert.assertNotNull(first);store.hideReads=2;int rolled=tx.rollbacks;
  AuthOperationIntakeReceipt replay=service.submit(request(),8,scope(),key->{throw new AssertionError("失败竞争者不能执行回调");});
  Assert.assertTrue(replay.isReplayed());Assert.assertEquals(first.getOperationKey(),replay.getOperationKey());Assert.assertEquals(rolled+1,tx.rollbacks);Assert.assertTrue(store.lastReadOnly);
 }
 @Test public void unrelatedUniqueConstraintDoesNotBecomeSuccessfulReplay(){
  store.insertFailure=new DuplicateKeyException("其他约束",new SQLException("ORA-00001: unique constraint (X.PK_AUTH_INTAKE) violated","23000",1));
  try{service.submit(request(),8,scope(),this::build);Assert.fail("不能吞掉其他约束");}catch(DuplicateKeyException expected){}
  Assert.assertEquals(0,builds);
 }
 @Test public void existingOuterWriteTransactionCannotBeSilentlySuspended(){
  new TransactionTemplate(tx).execute(status->{
   try{service.submit(request(),8,scope(),this::build);Assert.fail("不能提前独立提交header");}catch(IllegalStateException expected){}
   return null;
  });Assert.assertEquals(0,builds);
 }
 @Test public void anotherActorCannotObtainTheOriginalActorsReceipt(){
  AuthOperationIntakeReceipt one=service.submit(request(),8,scope(),this::build);Assert.assertNotNull(one);
  AuthOperationIntakeReceipt two=service.submit(request(),9,scope(),this::build);
  Assert.assertNotEquals(one.getOperationKey(),two.getOperationKey());Assert.assertFalse(two.isReplayed());
 }
 private static void fails(String code,Runnable action){try{action.run();Assert.fail("应拒绝："+code);}catch(RuntimeException expected){Assert.assertTrue(expected.toString(),expected.getMessage().contains(code));}}
 private static SmtAuthOperationBatch batch(long id,int park,String key){SmtAuthOperationBatch b=new SmtAuthOperationBatch();b.setId(id);b.setParkId(park);b.setSourceId(key);b.setSourceType("1");b.setAction("DELETE");b.setIdempotencyKey(key+":"+park);b.setPayloadFingerprint(String.join("",Collections.nCopies(64,"a")));b.setExpectedCount(1);return b;}
 private static SmtAuthRequestIntake copy(SmtAuthRequestIntake row){SmtAuthRequestIntake copy=new SmtAuthRequestIntake();BeanUtils.copyProperties(row,copy);return copy;}
 private static class Store implements AuthRequestIntakeMapper {
  Map<String,SmtAuthRequestIntake> rows=new HashMap<>();Map<String,List<SmtAuthOperationBatch>> children=new HashMap<>();int invalid,hideReads;boolean lastReadOnly;DuplicateKeyException insertFailure;
  public SmtAuthRequestIntake find(Integer actor,String key){lastReadOnly=TransactionSynchronizationManager.isCurrentTransactionReadOnly();if(hideReads-->0)return null;SmtAuthRequestIntake r=rows.get(actor+":"+key);return r==null?null:copy(r);}
  public int insert(SmtAuthRequestIntake row){if(insertFailure!=null)throw insertFailure;String key=row.getActorId()+":"+row.getRequestKey();if(rows.containsKey(key))throw new DuplicateKeyException("竞争",new SQLException("ORA-00001: unique constraint (X.UK_AUTH_INTAKE_ACTOR_KEY) violated","23000",1));rows.put(key,copy(row));return 1;}
  public int finish(SmtAuthRequestIntake row){rows.put(row.getActorId()+":"+row.getRequestKey(),copy(row));return 1;}
  public List<SmtAuthOperationBatch> children(String key){return children.getOrDefault(key,Collections.emptyList());}
  public int invalidSelections(String key){return invalid;}
 }
 private static class Tx extends AbstractPlatformTransactionManager {
  final Store store;int rollbacks;Tx(Store s){store=s;}
  protected Object doGetTransaction(){return new Snapshot();}
  protected void doBegin(Object value,TransactionDefinition definition){Snapshot s=(Snapshot)value;for(Map.Entry<String,SmtAuthRequestIntake> e:store.rows.entrySet())s.rows.put(e.getKey(),copy(e.getValue()));s.children.putAll(store.children);}
  protected void doCommit(DefaultTransactionStatus status){}
  protected void doRollback(DefaultTransactionStatus status){rollbacks++;Snapshot s=(Snapshot)status.getTransaction();store.rows=s.rows;store.children=s.children;}
 }
 private static class Snapshot {Map<String,SmtAuthRequestIntake> rows=new HashMap<>();Map<String,List<SmtAuthOperationBatch>> children=new HashMap<>();}
}
