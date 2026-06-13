package com.tce.smart.platform.api.dto.req.isc;

import lombok.Data;

import java.util.List;

@Data
public class IscCardTaskPageReqDTO {

	private Integer parkId;

	private List<Integer> parkIds;

	private String badge;

	private String name;

	private String cardNo;

	private Integer action;

	private Integer status;

	private Integer code;

	private String startTime;

	private String endTime;
}
