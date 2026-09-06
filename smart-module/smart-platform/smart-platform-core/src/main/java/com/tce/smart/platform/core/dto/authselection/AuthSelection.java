package com.tce.smart.platform.core.dto.authselection;

import com.tce.smart.platform.core.dto.authversion.AuthVersion.Window;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.ResourceInput;
import lombok.*;
import java.util.*;

/** 仅供服务器业务 handler 使用的选择契约；不接收表名、SQL 或客户端多态 JSON。 */
public final class AuthSelection {
 private AuthSelection() {}
 public enum SourceKind { STAFF_AUTH, VEHICLE_APPLY, VISITOR, VISITOR_FELLOW, VISITOR_VEHICLE, ADMITTANCE_FELLOW, ADMITTANCE_VEHICLE, SECURITY_RELATION }
 public enum SubjectType { STAFF, VEHICLE, VISITOR, VISITOR_FELLOW, VISITOR_VEHICLE, ADMITTANCE_FELLOW, ADMITTANCE_VEHICLE }
 /** 每种来源的具体 DTO 必须由注册 handler 显式白名单声明。 */
 public interface BusinessSnapshot {}
 public interface Credential {}
 @Data public static class PersonCredential implements Credential {
  private String taskCardNo; private Integer taskDeviceType; private Integer taskServiceType; private String cardType;
  private String certificateNo; private String personId; private String name; private String imageId; private String badge;
 }
 @Data public static class VehicleCredential implements Credential {
  private String taskCardNo; private Integer taskDeviceType; private Integer taskServiceType; private String plate; private String personId;
  /** 0为临时车辆、1为固定车辆；旧v1缺失保留null，表示冻结证据不足。 */
  private String cardType;
 }
 @Value @Builder public static class SelectedResource {
  ResourceInput input; Credential credential;
 }
 @Value @Builder public static class SourceSelection<B extends BusinessSnapshot> {
  /** 仅由服务器核实的证据缺失；原始事实仍保存在强类型业务快照中。 */
  public enum VerificationReason { MISSING_RESOURCE_EVIDENCE, MISSING_CREDENTIAL_EVIDENCE, INCOMPLETE_HISTORY_EVIDENCE }
  VerificationReason verificationReason;
  Integer parkId; SourceKind sourceKind; SubjectType subjectType; String subjectId; String authId;
  String stableKey; String sourceRowId; String action; String sourceType; String parentKind; String parentRowId;
  int snapshotVersion; B business;
  @Singular List<Window> windows;
  @Singular List<SelectedResource> resources;
 }
}
