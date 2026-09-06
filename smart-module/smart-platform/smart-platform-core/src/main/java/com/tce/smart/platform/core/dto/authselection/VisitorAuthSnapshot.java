package com.tce.smart.platform.core.dto.authselection;
import lombok.Data;
/** 普通访客权限事实与受理审计；不接受客户端可信标志。 */
@Data public class VisitorAuthSnapshot implements AuthSelection.BusinessSnapshot {
 private ParentFacts parent; private FellowFacts fellow; private Evidence evidence;
 /** 时间采用ISO本地时间文本，保留Oracle微秒；不经Date的毫秒JSON投影。 */
 @Data public static class ParentFacts {
  private Long id; private Integer parkId; private String createTime; private String startTime; private String endTime;
  private Integer cause; private Integer certType; private String certNo; private String visitorName; private String visitorPhotoId;
  private Integer isVehicle; private String vehiclePlate; private String processId; private String receptionistBadge; private String promoterBadge;
 }
 @Data public static class FellowFacts {
  private Long id; private Long visitorId; private Integer certType; private String certNo; private String fellowName; private String fellowPhotoId;
 }
 public enum Trigger { APPROVAL, EXPIRY, VEHICLE_EXIT, FELLOW_EXIT }
 public enum ApprovalBasis { DATABASE_PARENT_STATUS_ZERO, NOT_APPLICABLE }
 @Data public static class Evidence {
  private Trigger trigger; private ApprovalBasis approvalBasis; private Integer rawStatus; private Integer rawDelFlag; private Long eventId; private Integer leadHours;
 }
}
