package com.tce.smart.platform.service.print;
import com.tce.smart.platform.core.entity.print.*;
import com.tce.smart.platform.core.mapper.PrintTemplateMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.context.request.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
/** 原响应、业务状态和审计同事务提交；并发相同键等待数据库唯一约束。 */
@Component public class PrintJobTransactions {
 final TransactionTemplate transaction; final PrintTemplateMapper records;
 public PrintJobTransactions(PrintTemplateMapper records,PlatformTransactionManager manager){this.records=records;transaction=new TransactionTemplate(manager);}
 public <T>T atomic(Supplier<T> task){return transaction.execute(s->task.get());}
 public PrintMutationResult command(String principal,String key,String action,Object body,Supplier<Map<String,Object>> work){
  if(key==null||!key.matches("[\\x20-\\x7e]{1,128}"))throw error(422,"IDEMPOTENCY_KEY_REQUIRED");
  String hash=PrintJson.hash(Arrays.asList(action,body));PrintOperation prior=records.findOperation(principal,key);if(prior!=null)return replay(prior,hash);
  try{return atomic(()->{PrintOperation op=new PrintOperation();op.setOperationId(id());op.setPrincipalId(principal);op.setIdempotencyKey(key);op.setBodyHash(hash);op.setCreatedAt(now());records.insertOperation(op);Map<String,Object> result=work.get();op.setResponseJson(PrintJson.canonical(result));records.completeOperation(op);return new PrintMutationResult(result,false);});}
  catch(DuplicateKeyException ex){prior=records.findOperation(principal,key);if(prior!=null)return replay(prior,hash);throw error(409,"PRINT_CONCURRENT_MODIFICATION");}
 }
 private PrintMutationResult replay(PrintOperation op,String hash){if(!hash.equals(op.getBodyHash()))throw error(409,"IDEMPOTENCY_KEY_REUSED");if(op.getResponseJson()==null)throw error(409,"PRINT_OPERATION_IN_PROGRESS");return new PrintMutationResult(PrintJson.map(PrintJson.read(op.getResponseJson())),true);}
 public void audit(String actor,String park,String action,String object,Map<String,Object> summary){Map<String,Object> data=new LinkedHashMap<>(summary);RequestAttributes attrs=RequestContextHolder.getRequestAttributes();Object trace=attrs==null?null:attrs.getAttribute("print.requestId",RequestAttributes.SCOPE_REQUEST);data.put("requestId",trace instanceof String?trace:id());PrintAudit row=new PrintAudit();row.setAuditId(id());row.setParkId(park);row.setActorId(actor);row.setAction(action);row.setObjectId(object);row.setDetailsJson(PrintJson.canonical(data));row.setCreatedAt(now());records.insertAudit(row);}
 public static String id(){return UUID.randomUUID().toString();}
 public static Timestamp now(){return Timestamp.from(Instant.now());}
 public static PrintApiException error(int status,String code){return new PrintApiException(status,code,"打印操作未满足安全状态或资料要求");}
}
