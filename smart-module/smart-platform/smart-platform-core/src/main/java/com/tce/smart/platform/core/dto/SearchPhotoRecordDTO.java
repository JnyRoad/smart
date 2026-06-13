package com.tce.smart.platform.core.dto;

import lombok.Data;

@Data
public class SearchPhotoRecordDTO {


	private String badge;

	private String name;

	private String startTime;

	private String endTime;

	private Integer status;

	private Integer parkId;
}
