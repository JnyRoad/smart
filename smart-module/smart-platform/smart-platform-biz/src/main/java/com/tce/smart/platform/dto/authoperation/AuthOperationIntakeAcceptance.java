package com.tce.smart.platform.dto.authoperation;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import java.util.Map;
import java.util.Set;
/** 仅供服务端首次受理回调返回锁内已验证的实际范围与批次。 */
@Value @Builder
public class AuthOperationIntakeAcceptance {
 String operationKey;
 String outcome;
 @Singular Set<Integer> scopeParkIds;
 @Singular("batch") Map<Long,Integer> batchParks;
}
