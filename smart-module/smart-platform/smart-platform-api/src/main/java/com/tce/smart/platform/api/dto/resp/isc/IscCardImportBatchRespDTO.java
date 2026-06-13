package com.tce.smart.platform.api.dto.resp.isc;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IscCardImportBatchRespDTO {

	private Long id;

	private String mode;

	private String status;

	private String staffScope;

	private String staffScopeDesc;

	private Integer parkId;

	private String parkName;

	private Integer dispatcherParkId;

	private String dispatcherParkName;

	private Integer totalCount;

	private Integer successCount;

	private Integer skipCount;

	private Integer conflictCount;

	private Integer failCount;

	private Long consume;

	private String paramsJson;

	private String remark;

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private String optUser;
}
