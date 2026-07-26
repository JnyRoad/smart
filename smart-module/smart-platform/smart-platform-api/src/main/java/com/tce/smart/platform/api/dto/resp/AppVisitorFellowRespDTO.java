package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

/**
 * App 自助访客详情中的随行人员最小展示信息。
 *
 * 不携带随行人员证件号、证件类型或证件图片，证件材料只能通过独立审批能力读取。
 */
@Data
public class AppVisitorFellowRespDTO {
	private Long id;
	private String fellowName;
	private String fellowPhoto;
}
