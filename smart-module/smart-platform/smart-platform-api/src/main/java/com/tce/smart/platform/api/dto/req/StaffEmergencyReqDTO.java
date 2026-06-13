package com.tce.smart.platform.api.dto.req;

import lombok.Data;

/**
 * 紧急联系人修改
 * @author qipei
 *
 */
@Data
public class StaffEmergencyReqDTO {

	private String badge;

	private String relation;

	private String emergencyName;

	private String emergencyPhone;

}
