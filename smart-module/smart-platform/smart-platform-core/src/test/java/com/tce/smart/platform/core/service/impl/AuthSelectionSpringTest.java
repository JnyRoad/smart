package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.*;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.EmployeeAuthOperationMapper;
import org.junit.*;
import org.mockito.Mockito;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.*;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.*;

/** 真实 Spring 容器验证唯一 Bean、继承事务拦截和 handler 无环依赖；不连接数据库。 */
public class AuthSelectionSpringTest {
 @Configuration @EnableTransactionManagement(proxyTargetClass=true)
 @Import({EmployeeAuthOperationService.class,EmployeeAuthSourceHandler.class,AuthSourceConvergenceRegistry.class})
 public static class Wiring {
  @Bean public EmployeeAuthOperationMapper mapper(){return Mockito.mock(EmployeeAuthOperationMapper.class);}
  @Bean public AuthOperationWorkflowService workflow(){return Mockito.mock(AuthOperationWorkflowService.class);}
  @Bean public RecordingTransactions transactionManager(){return new RecordingTransactions();}
 }
 public static class RecordingTransactions extends AbstractPlatformTransactionManager {
  static class Tx { boolean active; }
  int commits,rollbacks;private final ThreadLocal<Tx> current=new ThreadLocal<>();
  protected Object doGetTransaction(){return current.get()==null?new Tx():current.get();}
  protected boolean isExistingTransaction(Object tx){return ((Tx)tx).active;}
  protected void doBegin(Object tx,TransactionDefinition definition){((Tx)tx).active=true;current.set((Tx)tx);}
  protected Object doSuspend(Object tx){current.remove();return tx;}
  protected void doResume(Object tx,Object suspended){current.set((Tx)suspended);}
  protected void doCommit(DefaultTransactionStatus status){commits++;}
  protected void doRollback(DefaultTransactionStatus status){rollbacks++;}
  protected void doCleanupAfterCompletion(Object tx){((Tx)tx).active=false;if(current.get()==tx)current.remove();}
 }
 @Test public void genericTypeInjectsOneEmployeeProxyAndInheritedStageRollsBack() {
  try(AnnotationConfigApplicationContext context=new AnnotationConfigApplicationContext(Wiring.class)) {
   AuthSelectionService service=context.getBean(AuthSelectionService.class);Assert.assertTrue(AopUtils.isAopProxy(service));Assert.assertEquals(1,context.getBeansOfType(AuthSelectionService.class).size());
   Assert.assertSame(service,context.getBean(EmployeeAuthOperationService.class));
   EmployeeAuthOperationMapper mapper=context.getBean(EmployeeAuthOperationMapper.class);
   Mockito.when(mapper.verificationReason(1L)).thenAnswer(c->{Assert.assertTrue(TransactionSynchronizationManager.isActualTransactionActive());throw new IllegalStateException("freeze failure");});
   try{service.stageNext(1L);Assert.fail("失败必须传播");}catch(IllegalStateException expected){}
   RecordingTransactions transactions=context.getBean(RecordingTransactions.class);Assert.assertEquals(1,transactions.rollbacks);Assert.assertEquals(0,transactions.commits);
  }
 }
 @Test public void completionFailureRollsBackEmployeeCasInInheritedApplyTransaction() {
  try(AnnotationConfigApplicationContext context=new AnnotationConfigApplicationContext(Wiring.class)) {
   AuthSelectionService service=context.getBean(AuthSelectionService.class);EmployeeAuthOperationMapper mapper=context.getBean(EmployeeAuthOperationMapper.class);
   SmtAuthSelectionSource row=new SmtAuthSelectionSource();row.setBatchId(1L);row.setOrdinal(1L);row.setSubjectId("10");row.setSourceRowId("5");row.setStableKey("2:101:9");row.setFingerprint("fingerprint");row.setDesiredAction("DELETE");row.setState("PENDING");
   Mockito.when(mapper.exactSource("s",1)).thenReturn(row);
   Mockito.when(mapper.deleteExact(row)).thenAnswer(c->{Assert.assertTrue(TransactionSynchronizationManager.isActualTransactionActive());return 1;});
   try {service.apply(SourceSnapshot.builder().sourceId("s").generation(1).sourceKind("STAFF_AUTH").subjectId("10").sourceRowId("5").stableKey(row.getStableKey()).fingerprint("fingerprint").build());Assert.fail("完成标记丢失应回滚业务CAS");}catch(IllegalArgumentException expected){}
   Mockito.verify(mapper).deleteExact(row);RecordingTransactions tx=context.getBean(RecordingTransactions.class);Assert.assertEquals(1,tx.rollbacks);Assert.assertEquals(0,tx.commits);
  }
 }
 @Test public void requiresNewStageCommitSurvivesSelectionRollbackAndReplaysSameFrozenShard() {
  try(AnnotationConfigApplicationContext context=new AnnotationConfigApplicationContext(Wiring.class)) {
   AuthSelectionService service=context.getBean(AuthSelectionService.class);EmployeeAuthOperationMapper mapper=context.getBean(EmployeeAuthOperationMapper.class);
   AuthOperationWorkflowService workflow=context.getBean(AuthOperationWorkflowService.class);
   AuthOperationWorkflowService raw=org.springframework.test.util.AopTestUtils.getTargetObject(workflow);
   Assert.assertTrue(AopUtils.isAopProxy(workflow));
   SmtAuthSelectionSource source=new SmtAuthSelectionSource();source.setBatchId(1L);source.setOrdinal(1L);source.setParkId(1);source.setSubjectId("10");source.setAuthId("9");source.setStableKey("2:101:9");source.setDesiredAction("DELETE");source.setOperationKey("op");source.setSourceRowId("5");source.setFingerprint("fingerprint");
   SmtAuthSelectionResource resource=new SmtAuthSelectionResource();resource.setBatchId(1L);resource.setOrdinal(1L);resource.setSourceOrdinal(1L);resource.setParkId(1);resource.setSubjectId("10");resource.setDeviceId("door");resource.setAccessType("DIRECT");resource.setResourceType("PERSON");resource.setResourceId("10");resource.setServiceType("1");resource.setCredentialChannel("FACE");resource.setParticipation("EXCLUDE");
   Mockito.when(mapper.source(1L,1)).thenReturn(source);Mockito.when(mapper.resources(1L,0,200)).thenReturn(java.util.Collections.singletonList(resource));
   Mockito.when(raw.stage(Mockito.any())).thenAnswer(c->{Assert.assertTrue(TransactionSynchronizationManager.isActualTransactionActive());return Expanded.builder().source(com.tce.smart.platform.core.dto.authversion.AuthVersion.SourceVersion.builder().sourceId("s").generation(1).build()).binding(com.tce.smart.platform.core.dto.authversion.AuthVersion.Binding.builder().resourceId("r").build()).build();});
   try{service.stageNext(1L);Assert.fail("来源绑定失败");}catch(IllegalArgumentException expected){}
   RecordingTransactions tx=context.getBean(RecordingTransactions.class);Assert.assertEquals(1,tx.commits);Assert.assertEquals(1,tx.rollbacks);
   Mockito.when(mapper.bindSource(1L,1,"s",1)).thenReturn(1);Mockito.when(mapper.bindResource(1L,1,"r")).thenReturn(1);Assert.assertTrue(service.stageNext(1L));
   org.mockito.ArgumentCaptor<Shard> capture=org.mockito.ArgumentCaptor.forClass(Shard.class);Mockito.verify(raw,Mockito.times(2)).stage(capture.capture());
   Assert.assertEquals(capture.getAllValues().get(0),capture.getAllValues().get(1));Assert.assertEquals("EMPLOYEE_SELECTION:1:1",capture.getValue().getSource().getPayloadSnapshot());
   Assert.assertEquals(3,tx.commits);Assert.assertEquals(1,tx.rollbacks);
  }
 }
}
