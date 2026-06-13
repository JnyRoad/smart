package com.tce.smart.platform.api.dto.resp.isc;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IscStaffCardRespDTO {

	private Long id;

	private Long staffId;

	private String badge;

	private Integer parkId;

	private String parkName;

	private Integer dispatcherParkId;

	private String dispatcherParkName;

	private String cardNo;

	private Integer delFlag;

	private String remark;

	private Integer syncStatus;

	private String syncStatusDesc;

	private Long lastTaskId;

	private Integer lastSyncCode;

	private String lastSyncRemark;

	private LocalDateTime lastSyncTime;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private String optUser;
}
