package com.tce.smart.platform.api.dto.req.isc;

import lombok.Data;

import java.util.List;

@Data
public class IscCardImportBatchPageReqDTO {

	private Integer parkId;

	private List<Integer> parkIds;

	private String mode;

	private String status;

	private String startTime;

	private String endTime;
}
