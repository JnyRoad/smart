package com.tce.smart.platform.api.dto.resp.isc;

import lombok.Data;

@Data
public class IscAccessCleanupExecuteRespDTO {

	private Integer totalCount = 0;

	private Integer createdCount = 0;

	private Integer updatedCount = 0;

	private Integer skipCount = 0;

	private Integer failCount = 0;
}
