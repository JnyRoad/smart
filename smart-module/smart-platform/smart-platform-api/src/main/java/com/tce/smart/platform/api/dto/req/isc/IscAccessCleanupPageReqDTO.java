package com.tce.smart.platform.api.dto.req.isc;

import lombok.Data;

import java.util.List;

@Data
public class IscAccessCleanupPageReqDTO {

	private Integer parkId;

	private List<Integer> parkIds;

	private String personType;

	private String cleanupStatus;

	private String keyword;

	private String deviceCode;

	private String startTime;

	private String endTime;
}
