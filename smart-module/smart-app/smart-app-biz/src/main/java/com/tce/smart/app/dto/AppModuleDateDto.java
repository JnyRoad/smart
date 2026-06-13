package com.tce.smart.app.dto;

import lombok.Data;

@Data
public class AppModuleDateDto {
	private Integer id;
	private Integer parentModule;
	private String  moduleIcon;
	private String  moduleName;
	private String  moduleUrl;
}
