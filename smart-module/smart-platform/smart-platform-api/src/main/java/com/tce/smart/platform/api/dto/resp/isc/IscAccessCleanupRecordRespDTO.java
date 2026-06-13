package com.tce.smart.platform.api.dto.resp.isc;

import lombok.Data;

import java.util.Date;

@Data
public class IscAccessCleanupRecordRespDTO {

	private Long downRecordId;

	private Integer parkId;

	private String parkName;

	private String deviceCode;

	private String deviceName;

	private String cardNo;

	private String badge;

	private String personId;

	private String personName;

	private Integer serviceType;

	private String serviceTypeDesc;

	private Date startTime;

	private Date overTime;

	private String personType;

	private String personTypeDesc;

	private String cleanupStatus;

	private String cleanupStatusDesc;

	private String deleteTaskStatus;

	private String deleteTaskStatusDesc;

	private Long deleteTaskId;

	private String reason;
}
