package com.tce.smart.platform.api.dto.req;

import lombok.Data;

@Data
public class ApplicationEmergencyReqDTO {


	private String applicationId;

	private String relation;

	private String emergencyName;

	private String phone;

	private Integer id;
}
