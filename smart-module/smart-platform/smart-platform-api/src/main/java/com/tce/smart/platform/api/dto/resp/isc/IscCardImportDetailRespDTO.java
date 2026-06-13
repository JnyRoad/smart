package com.tce.smart.platform.api.dto.resp.isc;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IscCardImportDetailRespDTO {

	private Long id;

	private Long batchId;

	private Long staffId;

	private String badge;

	private String name;

	private Integer parkId;

	private Integer dispatcherParkId;

	private String personId;

	private String iscCardNo;

	private String localCardNo;

	private String resultCode;

	private String resultDesc;

	private String reason;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;
}
