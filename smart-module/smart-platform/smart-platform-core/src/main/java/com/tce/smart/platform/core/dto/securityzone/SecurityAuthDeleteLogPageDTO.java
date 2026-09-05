package com.tce.smart.platform.core.dto.securityzone;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 自动删除审计分页查询的数据库投影。
 *
 * <p>主键先以 Long 在服务内部承载，转换到 API 响应时统一序列化为字符串。</p>
 */
@Data
public class SecurityAuthDeleteLogPageDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Integer parkId;
	private LocalDateTime execTime;
	private Long staffId;
	private String staffBadge;
	private String staffName;
	private String department;
	private Integer authId;
	private String authName;
	private LocalDateTime lastSnapTime;
	private String triggerReason;
	private String result;
	private String remark;
	private Integer taskCount;
	private Integer successCount;
	private Integer failCount;
	private Integer pendingCount;
	private Integer unknownCount;
}
