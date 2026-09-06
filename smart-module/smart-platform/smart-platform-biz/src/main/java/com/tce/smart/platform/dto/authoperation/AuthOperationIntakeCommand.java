package com.tce.smart.platform.dto.authoperation;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import java.util.List;
/** 原始请求语义，不能用当前来源或设备的冻结投影替代。 */
@Value @Builder(toBuilder=true)
public class AuthOperationIntakeCommand {
 String requestKey;
 String requestKind;
 Integer authId;
 Integer authorityType;
 @Singular List<Integer> rowIds;
}
