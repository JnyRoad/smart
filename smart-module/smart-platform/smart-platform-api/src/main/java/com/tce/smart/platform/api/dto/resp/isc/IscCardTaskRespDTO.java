package com.tce.smart.platform.api.dto.resp.isc;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IscCardTaskRespDTO {

	private Long id;

	private Integer action;

	private String actionDesc;

	private Integer priority;

	private Integer status;

	private String statusDesc;

	private Integer parkId;

	private String parkName;

	private String sourceType;

	private Long sourceId;

	private String badge;

	private String name;

	private String personId;

	private String cardNo;

	private Integer code;

	private String remark;

	private Long consume;

	private Integer times;

	private Long overTime;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private String optUser;
}
