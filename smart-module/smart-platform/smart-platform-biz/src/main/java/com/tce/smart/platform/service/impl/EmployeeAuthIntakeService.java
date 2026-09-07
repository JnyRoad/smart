package com.tce.smart.platform.service.impl;

import com.tce.smart.platform.core.entity.SmtAuthOperationBatch;
import com.tce.smart.platform.core.entity.SmtAuthRequestIntake;
import com.tce.smart.platform.core.mapper.AuthRequestIntakeMapper;
import com.tce.smart.platform.dto.authoperation.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.SQLException;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** header、首次业务冻结和完整子批次在一个物理事务中提交；重放只读取持久受理事实。 */
@Service
public class EmployeeAuthIntakeService {
 public interface FirstAcceptance {AuthOperationIntakeAcceptance accept(String operationKey);}
 /** 调用层按代码区分请求冲突、记录不完整和未受理，不把不确定结果自动转旧写入口。 */
 public static class IntakeException extends IllegalStateException {
  private final String code;
  public IntakeException(String code){super(code);this.code=code;}
  public String getCode(){return code;}
 }
 private static final Pattern HEADER_UNIQUE=Pattern.compile("\\bUK_AUTH_INTAKE_ACTOR_KEY\\b",Pattern.CASE_INSENSITIVE);
 private final AuthRequestIntakeMapper mapper;
 private final TransactionTemplate write;
 private final TransactionTemplate read;
 public EmployeeAuthIntakeService(AuthRequestIntakeMapper mapper,PlatformTransactionManager transactions) {
  this.mapper=Objects.requireNonNull(mapper,"受理Mapper不能为空");
  this.write=new TransactionTemplate(Objects.requireNonNull(transactions,"事务管理器不能为空"));
  this.read=new TransactionTemplate(transactions);this.read.setReadOnly(true);
 }
 /** actor和allowedParks仅由服务器认证上下文传入；回调是本库受理代码，不得执行外部请求。 */
 public AuthOperationIntakeReceipt submit(AuthOperationIntakeCommand command,Integer actorId,Set<Integer> allowedParks,FirstAcceptance acceptance) {
  if(TransactionSynchronizationManager.isActualTransactionActive())throw new IllegalStateException("请求协调必须在外层写事务之外调用");
  if(actorId==null || actorId<=0 || allowedParks==null || allowedParks.isEmpty() || allowedParks.stream().anyMatch(p->p==null || p<=0))throw new SecurityException("缺少明确的登录用户与园区范围");
  Set<Integer> allowed=Collections.unmodifiableSet(new HashSet<>(allowedParks));
  String fingerprint=AuthOperationIntakeCanonical.fingerprint(command);
  AuthOperationIntakeReceipt known=read.execute(status->{SmtAuthRequestIntake header=mapper.find(actorId,command.getRequestKey());return header==null?null:replay(header,command,actorId,allowed,fingerprint);});
  if(known!=null)return known;
  Objects.requireNonNull(acceptance,"首次受理回调不能为空");
  try {
   return write.execute(status->{
    SmtAuthRequestIntake concurrent=mapper.find(actorId,command.getRequestKey());
    if(concurrent!=null)return replay(concurrent,command,actorId,allowed,fingerprint);
    SmtAuthRequestIntake header=new SmtAuthRequestIntake();header.setOperationKey(UUID.randomUUID().toString());header.setActorId(actorId);header.setRequestKey(command.getRequestKey());
    header.setRequestKind(command.getRequestKind());header.setFingerprintVersion(1);header.setRequestFingerprint(fingerprint);header.setCreateTime(LocalDateTime.now(ZoneOffset.UTC));
    try {complete(mapper.insert(header)==1);}catch(DuplicateKeyException duplicate){if(headerCollision(duplicate))throw new HeaderCollision(duplicate);throw duplicate;}
    AuthOperationIntakeAcceptance accepted=acceptance.accept(header.getOperationKey());
    complete(accepted!=null && header.getOperationKey().equals(accepted.getOperationKey()));
    Set<Integer> scope=validScope(accepted.getScopeParkIds());authorize(scope,allowed);
    List<SmtAuthOperationBatch> children=children(header.getOperationKey(),scope);
    Map<Long,Integer> actual=new LinkedHashMap<>();for(SmtAuthOperationBatch child:children)actual.put(child.getId(),child.getParkId());
    complete(actual.equals(accepted.getBatchParks()) && mapper.invalidSelections(header.getOperationKey())==0);
    String outcome=accepted.getOutcome();complete("ACCEPTED".equals(outcome)?!children.isEmpty():"NO_CHANGE".equals(outcome) && children.isEmpty());
    header.setAuthScope(encodeScope(scope));header.setOutcome(outcome);header.setChildCount(children.size());header.setChildManifestHash(manifest(children));header.setAcceptedAt(LocalDateTime.now(ZoneOffset.UTC));
    complete(mapper.finish(header)==1);
    return receipt(header,children,false);
   });
  } catch(HeaderCollision collision) {
   // execute已退出并完成回滚；只对请求自然键冲突另开新读事务，不在rollback-only事务返回成功。
   return read.execute(status->{SmtAuthRequestIntake winner=mapper.find(actorId,command.getRequestKey());if(winner==null)throw collision.original;return replay(winner,command,actorId,allowed,fingerprint);});
  }
 }
 private AuthOperationIntakeReceipt replay(SmtAuthRequestIntake header,AuthOperationIntakeCommand command,Integer actor,Set<Integer> allowed,String fingerprint) {
  if(!Objects.equals(actor,header.getActorId()) || !Objects.equals(command.getRequestKey(),header.getRequestKey()))throw new SecurityException("受理归属不匹配");
  Set<Integer> scope=decodeScope(header.getAuthScope());authorize(scope,allowed);
  complete(header.getAcceptedAt()!=null && header.getCreateTime()!=null && Integer.valueOf(1).equals(header.getFingerprintVersion()));
  if(!Objects.equals(command.getRequestKind(),header.getRequestKind()) || !Objects.equals(fingerprint,header.getRequestFingerprint()))throw new IntakeException("KEY_PAYLOAD_CONFLICT");
  List<SmtAuthOperationBatch> children=children(header.getOperationKey(),scope);
  complete(Objects.equals(header.getChildCount(),children.size()) && Objects.equals(header.getChildManifestHash(),manifest(children)));
  complete("ACCEPTED".equals(header.getOutcome())?!children.isEmpty():"NO_CHANGE".equals(header.getOutcome()) && children.isEmpty());
  return receipt(header,children,true);
 }
 private List<SmtAuthOperationBatch> children(String operationKey,Set<Integer> scope) {
  complete(operationKey!=null && !operationKey.isEmpty());
  List<SmtAuthOperationBatch> rows=mapper.children(operationKey);complete(rows!=null);
  List<SmtAuthOperationBatch> sorted=new ArrayList<>(rows);Set<Long> seen=new HashSet<>();
  for(SmtAuthOperationBatch b:sorted)complete(b!=null && b.getId()!=null && b.getId()>0 && seen.add(b.getId()) && scope.contains(b.getParkId())
    && "1".equals(b.getSourceType()) && operationKey.equals(b.getSourceId()) && ("ADD".equals(b.getAction()) || "DELETE".equals(b.getAction()))
    && b.getIdempotencyKey()!=null && !b.getIdempotencyKey().isEmpty() && b.getPayloadFingerprint()!=null && b.getPayloadFingerprint().matches("[0-9a-f]{64}") && b.getExpectedCount()!=null && b.getExpectedCount()>=0);
  sorted.sort(Comparator.comparing(SmtAuthOperationBatch::getParkId).thenComparing(SmtAuthOperationBatch::getId));return sorted;
 }
 private static String manifest(List<SmtAuthOperationBatch> children){StringBuilder value=new StringBuilder(AuthOperationIntakeCanonical.tuple("INTAKE_CHILD_V1",children.size()));for(SmtAuthOperationBatch b:children)value.append(AuthOperationIntakeCanonical.tuple(b.getParkId(),b.getId(),b.getSourceType(),b.getSourceId(),b.getAction(),b.getIdempotencyKey(),b.getPayloadFingerprint(),b.getExpectedCount()));return AuthOperationIntakeCanonical.hash(value.toString());}
 private static AuthOperationIntakeReceipt receipt(SmtAuthRequestIntake header,List<SmtAuthOperationBatch> children,boolean replayed){boolean submitted="ACCEPTED".equals(header.getOutcome());AuthOperationIntakeReceipt.AuthOperationIntakeReceiptBuilder result=AuthOperationIntakeReceipt.builder().requestKey(header.getRequestKey()).operationKey(submitted?header.getOperationKey():null).mode(submitted?"RELIABLE":"NO_CHANGE").submitted(submitted).replayed(replayed);for(SmtAuthOperationBatch b:children)result.batch(String.valueOf(b.getId()),b.getParkId());return result.build();}
 private static Set<Integer> validScope(Set<Integer> scope){complete(scope!=null && !scope.isEmpty() && scope.stream().allMatch(p->p!=null && p>0));return new TreeSet<>(scope);}
 private static String encodeScope(Set<Integer> scope){String encoded=scope.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));if(encoded.length()>4000)throw new IntakeException("INTAKE_SCOPE_TOO_LARGE");return encoded;}
 private static Set<Integer> decodeScope(String encoded){complete(encoded!=null && !encoded.isEmpty());Set<Integer> scope=new TreeSet<>();for(String park:encoded.split(",",-1)){complete(park.matches("[1-9][0-9]*"));try{complete(scope.add(Integer.valueOf(park)));}catch(NumberFormatException e){throw new IntakeException("INTAKE_INCOMPLETE");}}complete(encoded.equals(encodeScope(scope)));return scope;}
 private static void authorize(Set<Integer> scope,Set<Integer> allowed){if(!allowed.containsAll(scope))throw new SecurityException("完整操作包含当前未授权园区");}
 private static void complete(boolean condition){if(!condition)throw new IntakeException("INTAKE_INCOMPLETE");}
 private static boolean headerCollision(Throwable failure){for(Throwable cause=failure;cause!=null;cause=cause.getCause())if(cause instanceof SQLException && ((SQLException)cause).getErrorCode()==1 && cause.getMessage()!=null && HEADER_UNIQUE.matcher(cause.getMessage()).find())return true;return false;}
 private static class HeaderCollision extends RuntimeException {final DuplicateKeyException original;HeaderCollision(DuplicateKeyException original){super(original);this.original=original;}}
}
