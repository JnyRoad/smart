package com.tce.smart.platform.core.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 紧急联系人修改
 * @author qipei
 *
 */
@Data
public class StaffEmergencyDTO {

	private String badge;

	private String relation;

	private String emergencyName;

	private String emergencyPhone;

}
