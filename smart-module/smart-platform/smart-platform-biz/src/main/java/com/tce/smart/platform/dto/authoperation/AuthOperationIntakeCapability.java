package com.tce.smart.platform.dto.authoperation;
import lombok.Value;
/** 能力只决定新请求使用哪种契约，不替代跨园区受理校验。 */
@Value public class AuthOperationIntakeCapability { int intakeVersion; boolean reliableIntakeEnabled; }
