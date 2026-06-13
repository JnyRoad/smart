package com.tce.smart.platform.api.dto.resp.isc;

import lombok.Data;

@Data
public class IscAccessCleanupSummaryRespDTO {

	private Integer totalCount = 0;

	private Integer executableCount = 0;

	private Integer protectedCount = 0;

	private Integer visitorCount = 0;

	private Integer staffCount = 0;

	private Integer missingDeleteTaskCount = 0;

	private Integer retryDeleteTaskCount = 0;
}
