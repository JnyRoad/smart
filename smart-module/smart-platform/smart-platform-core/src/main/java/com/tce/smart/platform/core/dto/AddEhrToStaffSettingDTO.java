package com.tce.smart.platform.core.dto;

import java.util.List;

import lombok.Data;

@Data
public class AddEhrToStaffSettingDTO {


	private List<String> compIds;

	private Integer time;

	private String timeUnit;
}
