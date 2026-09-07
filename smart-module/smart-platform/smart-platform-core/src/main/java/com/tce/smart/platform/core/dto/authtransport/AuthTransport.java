package com.tce.smart.platform.core.dto.authtransport;
import com.tce.smart.platform.core.entity.SmtAuthTransportPhase;
import lombok.*;
import java.util.*;
/** 定量调度返回值；UNKNOWN 只能核验，不能重新发送。 */
public final class AuthTransport {
 private AuthTransport() { }
 @Value @Builder public static class Run { String outcome; int processed; int httpUsed; Long nextCursor; int nextPage; @Singular List<Long> phaseIds; }
 /** 身份证明只赋予人员归属，不代表消费者照片已经处理。 */
 @Value @Builder public static class PersonIdentity {String outcome;String personId;Long proofPhaseId;String reason;}
 /** 发送前冻结的一组阶段，所有成员使用同一实际请求关联号。 */
 @Value @Builder public static class Group { String requestKey; @Singular List<SmtAuthTransportPhase> phases; }
}
