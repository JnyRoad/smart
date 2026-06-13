package com.tce.smart.platform.api.dto.req.isc;

import lombok.Data;

import java.util.List;

@Data
public class IscAccessCleanupExecuteReqDTO {

	private List<Long> downRecordIds;

	private Integer parkId;

	private String personType;

	private String cleanupStatus;

	private String keyword;

	private String deviceCode;

	private String startTime;

	private String endTime;
}
