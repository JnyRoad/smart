package com.tce.smart.platform.core.service.impl;

import com.tce.smart.platform.core.dto.authversion.AuthVersion.*;
import com.tce.smart.platform.core.entity.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.stream.Collectors;
import com.tce.smart.platform.core.mapper.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mockito;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** 来源稳定身份、完整幂等意图和删除门禁的行为测试。真实行锁另由 Oracle 用例验证。 */
public class AuthOperationVersionTest {
    private AuthOperationVersionService service;
    private Map<String, SmtAuthSourceCoord> rows;

    @Before
    public void setUp() {
        rows = new HashMap<>();
        SmtAuthSourceCoordMapper source = Mockito.mock(SmtAuthSourceCoordMapper.class);
        Mockito.when(source.selectById(Mockito.anyString())).thenAnswer(i -> rows.get(i.getArgument(0)));
        Mockito.when(source.identity(Mockito.anyString())).thenAnswer(i -> rows.get(i.getArgument(0)));
        Mockito.when(source.lock(Mockito.anyString())).thenAnswer(i -> rows.get(i.getArgument(0)));
        Mockito.when(source.insert(Mockito.any())).thenAnswer(i -> {
            SmtAuthSourceCoord row = i.getArgument(0); rows.put(row.getId(), row); return 1;
        });
        Mockito.when(source.update(Mockito.any())).thenAnswer(i -> {
            SmtAuthSourceCoord row = i.getArgument(0); rows.put(row.getId(), row); return 1;
        });
        service = new AuthOperationVersionService(Mockito.mock(SmtAuthSubjectCoordMapper.class),source, Mockito.mock(SmtAuthResourceCoordMapper.class),
            Mockito.mock(SmtAuthSourceResourceMapper.class), Mockito.mock(SmtAuthIdentityAliasMapper.class));
    }

    @Test
    public void identicalIntentDoesNotConsumeAnotherGeneration() {
        SourceVersion first = service.reserveSourceIntent(intent());
        SourceVersion retry = service.reserveSourceIntent(intent());
        Assert.assertEquals(1L, first.getGeneration());
        Assert.assertEquals(1L, retry.getGeneration());
        Assert.assertTrue(retry.isIdempotent());
    }

    @Test
    public void sameIntentKeyCannotChangeRowWindowActionOrBatch() {
        service.reserveSourceIntent(intent());
        reject(() -> service.reserveSourceIntent(intent().toBuilder().sourceRowId("replacement").build()));
        reject(() -> service.reserveSourceIntent(intent().toBuilder().action("DELETE").build()));
        reject(() -> service.reserveSourceIntent(intent().toBuilder().batchId(999L).build()));
        reject(() -> service.reserveSourceIntent(intent().toBuilder().clearWindows()
            .window(Window.builder().from(LocalDateTime.of(2030,1,1,0,0))
                .to(LocalDateTime.of(2030,1,8,0,0)).build()).build()));
    }

    @Test
    public void deletingStableSourceRejectsRecreatedPhysicalRow() {
        SourceVersion deleted = service.reserveSourceIntent(intent().toBuilder().action("DELETE").build());
        reject(() -> service.reserveSourceIntent(intent().toBuilder().intentKey("new")
            .sourceRowId("new-physical-row").build()));
        Assert.assertEquals("REVOKING", rows.get(deleted.getSourceId()).getState());
        for(String state:new String[]{"VERIFYING","FAILED"}) {
            rows.get(deleted.getSourceId()).setState(state);
            reject(() -> service.reserveSourceIntent(intent().toBuilder().intentKey("new").sourceRowId("new-row").build()));
        }
    }

    @Test
    public void tombstoneAllowsNextGenerationWithoutReusingPhysicalRowIdentity() {
        SourceVersion old = service.reserveSourceIntent(intent());
        rows.get(old.getSourceId()).setState("TOMBSTONE");
        SourceVersion replacement = service.reserveSourceIntent(intent().toBuilder().intentKey("new")
            .sourceRowId("replacement").sourceFingerprint("fp-new").build());
        Assert.assertEquals(old.getSourceId(), replacement.getSourceId());
        Assert.assertEquals(2L, replacement.getGeneration());
    }

    @Test
    public void changedSubjectCannotTakeOverAnotherStableSource() {
        service.reserveSourceIntent(intent());
        reject(() -> service.reserveSourceIntent(intent().toBuilder().intentKey("new").subjectType("VISITOR").build()));
    }

    @Test
    public void crossBatchOwnerRetryDoesNotPointBackToFollower() {
        VersionHarness h=new VersionHarness(); h.setup();
        SourceVersion a=h.v.reserveSourceIntent(h.intent("a",1)), b=h.v.reserveSourceIntent(h.intent("b",2));
        ResourceDecision d=h.stage(a,11);h.stage(b,22);
        Binding ba=h.bind(a,d,11,1001),bb=h.bind(b,d,22,1002);
        Assert.assertEquals("READY",h.v.bindTarget(ba).getOutcome());
        Assert.assertEquals(Long.valueOf(1001),h.v.bindTarget(bb).getReuseTargetId());
        Assert.assertEquals("READY",h.v.bindTarget(ba).getOutcome());
        Assert.assertEquals(Long.valueOf(1001),h.v.bindTarget(bb).getReuseTargetId());
    }

    @Test
    public void windowBudgetRejectsBeforeAnyPayloadIsLoaded() {
        VersionHarness h=new VersionHarness();h.setup();
        SourceVersion a=h.v.reserveSourceIntent(h.intent("a",1));ResourceDecision d=h.stage(a,11);
        SmtAuthSourceResource first=Mockito.spy(h.cm.values().iterator().next());
        first.setWindowCount(6000L);first.setWindowLength(500000L);
        SmtAuthSourceResource second=Mockito.spy(h.cm.values().iterator().next());
        second.setWindowCount(6000L);second.setWindowLength(500000L);
        doThrow(new AssertionError("越预算前不能先加载贡献CLOB")).when(first).getWindows();
        doThrow(new AssertionError("越预算前不能先加载贡献CLOB")).when(second).getWindows();
        when(h.c.currentForResource(anyString())).thenReturn(Arrays.asList(first,second));
        doThrow(new AssertionError("越预算前不能访问窗口内容")).when(h.c).windowById(anyString());
        try {h.v.currentDesired(d.getResourceId());Assert.fail("总窗口超过10000必须可见地拒绝");}
        catch(IllegalArgumentException expected){Assert.assertTrue(expected.getMessage().contains("窗口"));}
    }

 @Test public void currentDesiredRereadsResourceAfterWaitingForSubjectLock() throws Exception {
  VersionHarness h=new VersionHarness();h.setup();SourceVersion a=h.v.reserveSourceIntent(h.intent("cache",1));ResourceDecision d=h.stage(a,11);
  org.apache.ibatis.session.Configuration cfg=new org.apache.ibatis.session.Configuration();
  org.apache.ibatis.mapping.MappedStatement read=new org.apache.ibatis.mapping.MappedStatement.Builder(cfg,"resource.selectById",new org.apache.ibatis.builder.StaticSqlSource(cfg,"SELECT * FROM resource WHERE id = ?"),org.apache.ibatis.mapping.SqlCommandType.SELECT).build();
  org.apache.ibatis.mapping.MappedStatement fresh=new org.apache.ibatis.mapping.MappedStatement.Builder(cfg,"resource.lock",new org.apache.ibatis.builder.StaticSqlSource(cfg,"SELECT * FROM resource WHERE id = ? FOR UPDATE"),org.apache.ibatis.mapping.SqlCommandType.SELECT).flushCacheRequired(true).build();
  org.apache.ibatis.mapping.MappedStatement lock=new org.apache.ibatis.mapping.MappedStatement.Builder(cfg,"subject.lock",new org.apache.ibatis.builder.StaticSqlSource(cfg,"SELECT * FROM subject WHERE id = ? FOR UPDATE"),org.apache.ibatis.mapping.SqlCommandType.SELECT).build();
  org.apache.ibatis.executor.BaseExecutor executor=new org.apache.ibatis.executor.BaseExecutor(cfg,null) {
   protected int doUpdate(org.apache.ibatis.mapping.MappedStatement m,Object p){return 0;}
   protected java.util.List<org.apache.ibatis.executor.BatchResult> doFlushStatements(boolean rollback){return Collections.emptyList();}
   protected <E> org.apache.ibatis.cursor.Cursor<E> doQueryCursor(org.apache.ibatis.mapping.MappedStatement m,Object p,org.apache.ibatis.session.RowBounds b,org.apache.ibatis.mapping.BoundSql q){return null;}
   protected <E> java.util.List<E> doQuery(org.apache.ibatis.mapping.MappedStatement m,Object p,org.apache.ibatis.session.RowBounds b,org.apache.ibatis.session.ResultHandler handler,org.apache.ibatis.mapping.BoundSql q){
    if(m.getId().startsWith("resource.")){SmtAuthResourceCoord copy=new SmtAuthResourceCoord();org.springframework.beans.BeanUtils.copyProperties(h.rm.get(d.getResourceId()),copy);return (java.util.List<E>)Collections.singletonList(copy);}
    // 模拟主体锁等待期间另一事务把永久资源代次推进至2后提交。
    h.rm.get(d.getResourceId()).setGeneration(2L);SmtAuthSubjectCoord subject=new SmtAuthSubjectCoord();subject.setId(p.toString());subject.setParkId(7);subject.setSubjectType("STAFF");subject.setSubjectId("staff");return (java.util.List<E>)Collections.singletonList(subject);
   }
  };
  when(h.r.selectById(anyString())).thenAnswer(i->executor.query(read,i.getArgument(0),org.apache.ibatis.session.RowBounds.DEFAULT,null).get(0));
  when(h.r.lock(anyString())).thenAnswer(i->executor.query(fresh,i.getArgument(0),org.apache.ibatis.session.RowBounds.DEFAULT,null).get(0));
  com.tce.smart.platform.core.mapper.SmtAuthSubjectCoordMapper subjects=mock(com.tce.smart.platform.core.mapper.SmtAuthSubjectCoordMapper.class);
  when(subjects.lock(anyString())).thenAnswer(i->executor.query(lock,i.getArgument(0),org.apache.ibatis.session.RowBounds.DEFAULT,null).get(0));
  h.v=new AuthOperationVersionService(subjects,h.s,h.r,h.c,mock(com.tce.smart.platform.core.mapper.SmtAuthIdentityAliasMapper.class));
  ResourceDecision got=h.v.currentDesired(d.getResourceId());System.out.println("MyBatis scope="+cfg.getLocalCacheScope()+", database gen="+h.rm.get(d.getResourceId()).getGeneration()+", returned gen="+got.getGeneration());
  Assert.assertEquals("主体锁取得后必须读当前资源代次",2L,got.getGeneration());
 }

    @Test public void expectedPredecessorAllowsOnlyExactCompletedAddAndSameKeyReplay() {
        ExpectedFixture f=new ExpectedFixture();SourceIntent follow=f.follow();SourceVersion next=f.v.reserveSourceIntent(follow);
        Assert.assertEquals(2L,next.getGeneration());Assert.assertEquals("REVOKING",next.getState());
        Assert.assertEquals(2L,f.v.reserveSourceIntent(follow).getGeneration());
        verify(f.s,times(1)).update(any());
        org.mockito.InOrder order=inOrder(f.subjects,f.s);order.verify(f.subjects).lock(anyString());order.verify(f.s).lock(anyString());order.verify(f.s).update(any());
    }
    @Test public void expectedPredecessorRejectsEveryIdentityCoordinateWithoutChangingGeneration() {
        ExpectedFixture f=new ExpectedFixture();ExpectedPredecessor p=f.expected();
        for(ExpectedPredecessor bad:Arrays.asList(p.toBuilder().sourceId("another-source").build(),p.toBuilder().generation(9L).build(),p.toBuilder().sourceFingerprint("different").build(),p.toBuilder().sourceRowId("another-row").build(),p.toBuilder().intentKey("another-key").build(),p.toBuilder().batchId(99L).build())) {
            reject(()->f.v.reserveSourceIntent(f.follow().toBuilder().expectedPredecessor(bad).build()));
        }
        Assert.assertEquals(Long.valueOf(1),f.current.getGeneration());verify(f.s,never()).update(any());
    }
    @Test public void expectedPredecessorCannotCreateMissingSourceOrSkipIncompleteState() {
        ExpectedFixture absent=new ExpectedFixture();SourceIntent follow=absent.follow();when(absent.s.lock(anyString())).thenReturn(null);
        reject(()->absent.v.reserveSourceIntent(follow));verify(absent.s,never()).insert(any());
        for(String state:Arrays.asList("EXPANDING","REVOKING","VERIFYING","FAILED","TOMBSTONE")) {
            ExpectedFixture f=new ExpectedFixture();f.current.setState(state);reject(()->f.v.reserveSourceIntent(f.follow()));verify(f.s,never()).update(any());
        }
        ExpectedFixture unsealed=new ExpectedFixture();unsealed.current.setExpanded(0);reject(()->unsealed.v.reserveSourceIntent(unsealed.follow()));verify(unsealed.s,never()).update(any());
    }
    @Test public void expectedPredecessorChecksFreshLockedRowRatherThanObservedTerminalGeneration() {
        ExpectedFixture f=new ExpectedFixture();SourceIntent observed=f.follow();
        when(f.s.lock(anyString())).thenAnswer(call->{f.current.setGeneration(3L);f.current.setIntentKey("intervening");return f.current;});
        reject(()->f.v.reserveSourceIntent(observed));verify(f.s,never()).update(any());Assert.assertEquals(Long.valueOf(3),f.current.getGeneration());
    }
    @Test public void expectedPredecessorRequiresDeleteFromAddAndCompleteFrozenProof() {
        ExpectedFixture f=new ExpectedFixture();ExpectedPredecessor p=f.expected();
        for(ExpectedPredecessor bad:Arrays.asList(p.toBuilder().generation(null).build(),p.toBuilder().generation(0L).build(),p.toBuilder().sourceId(null).build(),p.toBuilder().sourceFingerprint(null).build(),p.toBuilder().sourceRowId(null).build(),p.toBuilder().intentKey(null).build(),p.toBuilder().batchId(null).build(),p.toBuilder().action("DELETE").build()))reject(()->f.v.reserveSourceIntent(f.follow().toBuilder().expectedPredecessor(bad).build()));
        reject(()->f.v.reserveSourceIntent(f.follow().toBuilder().action("ADD").window(Window.builder().from(LocalDateTime.of(2030,1,1,0,0)).to(LocalDateTime.of(2030,1,2,0,0)).build()).build()));
        reject(()->f.v.reserveSourceIntent(f.follow().toBuilder().sourceRowId("replacement").build()));verify(f.s,never()).update(any());
    }
    @Test public void sameExpectedIntentCannotChangeAnyPredecessorProofOnReplay() {
        ExpectedFixture f=new ExpectedFixture();SourceIntent accepted=f.follow();f.v.reserveSourceIntent(accepted);
        ExpectedPredecessor p=accepted.getExpectedPredecessor();
        for(ExpectedPredecessor bad:Arrays.asList(p.toBuilder().generation(2L).build(),p.toBuilder().sourceId("other").build(),p.toBuilder().sourceFingerprint("other").build(),p.toBuilder().sourceRowId("other").build(),p.toBuilder().intentKey("other").build(),p.toBuilder().batchId(2L).build()))reject(()->f.v.reserveSourceIntent(accepted.toBuilder().expectedPredecessor(bad).build()));
        reject(()->f.v.reserveSourceIntent(accepted.toBuilder().expectedPredecessor(null).build()));Assert.assertEquals(Long.valueOf(2),f.current.getGeneration());verify(f.s,times(1)).update(any());
    }
    @Test public void historicalExpectedIntentReplaysItsOldGenerationWithoutRequiringPredecessorCurrent() {
        ExpectedFixture f=new ExpectedFixture();SourceIntent follow=f.follow();f.v.reserveSourceIntent(follow);
        SmtAuthSourceResource history=new SmtAuthSourceResource();history.setSourceGeneration(2L);history.setSourceRowId(f.current.getSourceRowId());history.setSourceFingerprint(f.current.getSourceFingerprint());history.setSourceAction("DELETE");history.setIntentFingerprint(f.current.getIntentFingerprint());
        when(f.c.historicalIntent(f.current.getId(),follow.getIntentKey())).thenReturn(history);
        f.current.setGeneration(3L);f.current.setIntentKey("later-add");f.current.setIntentFingerprint("later-fp");f.current.setAction("ADD");f.current.setState("EXPANDING");
        SourceVersion replay=f.v.reserveSourceIntent(follow);Assert.assertEquals("HISTORICAL",replay.getState());Assert.assertEquals(2L,replay.getGeneration());Assert.assertTrue(replay.isIdempotent());Assert.assertEquals(Long.valueOf(3),f.current.getGeneration());
        reject(()->f.v.reserveSourceIntent(follow.toBuilder().expectedPredecessor(follow.getExpectedPredecessor().toBuilder().batchId(88L).build()).build()));verify(f.s,times(1)).update(any());
    }
    @Test public void absentExpectedPredecessorKeepsOriginalIntentFingerprintBytes() {
        SourceVersion first=service.reserveSourceIntent(intent());Assert.assertEquals("3f08cb8622e5c7abc1a98da45121da9df4c38a2d1339a92a022b4f3455a60fdc",rows.get(first.getSourceId()).getIntentFingerprint());
    }
    private class ExpectedFixture {
        final SmtAuthSubjectCoordMapper subjects=mock(SmtAuthSubjectCoordMapper.class);final SmtAuthSourceCoordMapper s=mock(SmtAuthSourceCoordMapper.class);final SmtAuthSourceResourceMapper c=mock(SmtAuthSourceResourceMapper.class);
        final AuthOperationVersionService v=new AuthOperationVersionService(subjects,s,mock(SmtAuthResourceCoordMapper.class),c,mock(SmtAuthIdentityAliasMapper.class));SmtAuthSourceCoord current;
        ExpectedFixture(){when(s.lock(anyString())).thenAnswer(call->current);when(s.insert(any())).thenAnswer(call->{current=call.getArgument(0);return 1;});when(s.update(any())).thenReturn(1);v.reserveSourceIntent(intent());current.setState("ACTIVE");current.setExpanded(1);clearInvocations(subjects,s,c);}
        ExpectedPredecessor expected(){return ExpectedPredecessor.builder().sourceId(current.getId()).generation(current.getGeneration()).sourceFingerprint(current.getSourceFingerprint()).sourceRowId(current.getSourceRowId()).intentKey(current.getIntentKey()).batchId(current.getBatchId()).action("ADD").build();}
        SourceIntent follow(){return intent().toBuilder().action("DELETE").clearWindows().intentKey("following-delete").batchId(2L).sourceFingerprint("following-fingerprint").expectedPredecessor(expected()).build();}
    }

    private SourceIntent intent() {
        return SourceIntent.builder().parkId(17).sourceKind("STAFF_AUTH").stableKey("staff-1:auth-1")
            .subjectType("STAFF").subjectId("staff-1").sourceRowId("physical-row-1")
            .sourceFingerprint("fp-1").intentKey("intent-1").batchId(1L).action("ADD")
            .payloadSnapshot("frozen-devices-v1")
            .window(Window.builder().from(LocalDateTime.of(2030,1,1,0,0))
                .to(LocalDateTime.of(2030,1,2,0,0)).build()).build();
    }

    private static void reject(Runnable action) {
        try { action.run(); Assert.fail("必须拒绝不一致或删除中的来源意图"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }
}

/** 只用于规则反例的持久Mapper内存替身，不代表Oracle隔离级别。 */
class VersionHarness {
  Map<String,SmtAuthSourceCoord> sm=new HashMap<>(); Map<String,SmtAuthResourceCoord> rm=new HashMap<>(); Map<String,SmtAuthSourceResource> cm=new HashMap<>();
  SmtAuthSourceCoordMapper s=mock(SmtAuthSourceCoordMapper.class); SmtAuthResourceCoordMapper r=mock(SmtAuthResourceCoordMapper.class); SmtAuthSourceResourceMapper c=mock(SmtAuthSourceResourceMapper.class);
  AuthOperationVersionService v;
  public void setup(){
    when(s.lock(anyString())).thenAnswer(i->sm.get(i.getArgument(0))); when(s.selectById(anyString())).thenAnswer(i->sm.get(i.getArgument(0)));when(s.identity(anyString())).thenAnswer(i->sm.get(i.getArgument(0)));
    when(s.insert(any())).thenAnswer(i->{SmtAuthSourceCoord x=i.getArgument(0);sm.put(x.getId(),x);return 1;}); when(s.update(any())).thenReturn(1);
    when(r.lock(anyString())).thenAnswer(i->rm.get(i.getArgument(0))); when(r.selectById(anyString())).thenAnswer(i->rm.get(i.getArgument(0)));
    when(r.insert(any())).thenAnswer(i->{SmtAuthResourceCoord x=i.getArgument(0);rm.put(x.getId(),x);return 1;}); when(r.update(any())).thenReturn(1);
    when(c.selectById(anyString())).thenAnswer(i->cm.get(i.getArgument(0)));
    when(c.insert(any())).thenAnswer(i->{SmtAuthSourceResource x=i.getArgument(0);cm.put(x.getId(),x);return 1;}); when(c.update(any())).thenReturn(1);
    when(c.currentForResource(anyString())).thenAnswer(i->cm.values().stream().filter(x->x.getBindingRevision()==0 && x.getResourceCoordId().equals(i.getArgument(0)) && x.getSourceGeneration().equals(sm.get(x.getSourceCoordId()).getGeneration()) && "ADD".equals(x.getAction()) && "ADD".equals(sm.get(x.getSourceCoordId()).getAction()) && !"TOMBSTONE".equals(sm.get(x.getSourceCoordId()).getState()))
      .sorted(Comparator.comparing(SmtAuthSourceResource::getSourceCoordId)).limit(1001).peek(x->{x.setWindowCount("#".equals(x.getWindows())?0L:(long)x.getWindows().split(";").length);x.setWindowLength((long)x.getWindows().length());x.setCurrentSourceRowId(sm.get(x.getSourceCoordId()).getSourceRowId());x.setCurrentSourceFingerprint(sm.get(x.getSourceCoordId()).getSourceFingerprint());}).collect(Collectors.toList()));
    when(c.windowById(anyString())).thenAnswer(i->cm.get(i.getArgument(0)).getWindows());
    when(c.executionOwner(anyString(),anyLong())).thenAnswer(i->cm.values().stream().filter(x->x.getBindingRevision()==0 && x.getResourceCoordId().equals(i.getArgument(0)) && x.getResourceGeneration()==(long)i.getArgument(1) && x.getTargetId()!=null).map(SmtAuthSourceResource::getTargetId).min(Long::compareTo).orElse(null));
    when(c.bindingSnapshot(any())).thenAnswer(i->{Binding b=i.getArgument(0);return cm.values().stream().filter(x->x.getBindingRevision()>=0 && x.getSourceCoordId().equals(b.getSourceId()) && x.getSourceGeneration()==b.getSourceGeneration() && x.getResourceCoordId().equals(b.getResourceId()) && x.getResourceGeneration()==b.getResourceGeneration() && Objects.equals(x.getTargetId(),b.getTargetId())).findFirst().orElse(null);});
    when(c.currentForSource(anyString(),anyLong())).thenAnswer(i->cm.values().stream().filter(x->x.getSourceCoordId().equals(i.getArgument(0))&&x.getSourceGeneration()==(long)i.getArgument(1)).collect(Collectors.toList()));
    when(c.pendingForSource(anyString(),anyLong(),any(),anyInt())).thenAnswer(i->cm.values().stream().filter(x->x.getSourceCoordId().equals(i.getArgument(0))&&x.getSourceGeneration()==(long)i.getArgument(1)&&!x.getState().equals("CONVERGED")).collect(Collectors.toList()));
    when(c.ownershipCount(any(),any(),any(),anyString(),any(),anyString(),anyBoolean())).thenReturn(1); when(c.targetWindowCount(anyLong(),any(),any())).thenReturn(1);
    v=new AuthOperationVersionService(mock(SmtAuthSubjectCoordMapper.class),s,r,c,mock(SmtAuthIdentityAliasMapper.class));
  }
  SourceIntent intent(String id,long batch){return SourceIntent.builder().parkId(7).sourceKind("STAFF_AUTH").stableKey(id).subjectType("STAFF").subjectId("staff").sourceRowId(id).sourceFingerprint(id).intentKey(id).batchId(batch).payloadSnapshot("devices").action("ADD").window(Window.builder().from(LocalDateTime.of(2030,1,1,0,0)).to(LocalDateTime.of(2030,1,2,0,0)).build()).build();}
  ResourceKey key(){return ResourceKey.builder().parkId(7).subjectType("STAFF").subjectId("staff").accessType("DIRECT").deviceId("dev").resourceType("CARD").resourceId("perm").serviceType("1").credentialChannel("CARD").build();}
  ResourceDecision stage(SourceVersion x,long req){return v.stageContribution(ContributionCommand.builder().sourceId(x.getSourceId()).sourceGeneration(x.getGeneration()).resource(key()).requestId(req).build());}
  Binding bind(SourceVersion x,ResourceDecision d,long req,long target){return Binding.builder().sourceId(x.getSourceId()).sourceGeneration(x.getGeneration()).resourceId(d.getResourceId()).resourceGeneration(d.getGeneration()).requestId(req).targetId(target).attemptId(target+100).build();}
}
