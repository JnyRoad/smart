package com.tce.smart.platform.dto.authoperation;

import lombok.Value;

/** 人员操作受理回执；受理与设备确认是不同事实。 */
@Value
public class AuthOperationReceipt {
 String mode;
 boolean submitted;
 String operationKey;

 public static AuthOperationReceipt reliable(String key) {
  if (key == null || key.trim().isEmpty()) throw new IllegalStateException("受理操作键缺失");
  if ("NO_CHANGE".equals(key)) return new AuthOperationReceipt("NO_CHANGE", false, null);
  return new AuthOperationReceipt("RELIABLE", true, key);
 }
 public static AuthOperationReceipt legacy(Boolean submitted) {
  return new AuthOperationReceipt("LEGACY", Boolean.TRUE.equals(submitted), null);
 }
}
