package com.tce.smart.platform.api.dto.resp.isc;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IscParkConfigRespDTO {

	private Long id;

	private Integer parkId;

	private String parkName;

	private Integer dispatcherParkId;

	private String dispatcherParkName;

	private Integer cardSyncEnabled;

	private Integer delFlag;

	private String remark;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

	private String optUser;
}
