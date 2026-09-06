package com.tce.smart.platform.dto.authoperation;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import java.util.Map;
/** 请求的原始受理结果；批次ID用字符串保留Long精度，不表示设备已完成。 */
@Value @Builder
public class AuthOperationIntakeReceipt {
 String requestKey;
 String operationKey;
 String mode;
 boolean submitted;
 boolean replayed;
 @Singular("batch") Map<String,Integer> batchParks;
}
