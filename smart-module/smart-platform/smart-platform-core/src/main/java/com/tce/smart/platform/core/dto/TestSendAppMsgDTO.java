package com.tce.smart.platform.core.dto;

import lombok.Data;

/**
 * 添加随行人员的添加数据
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
public class TestSendAppMsgDTO {

	private String bussiessId;
	private String badge;
	private String tempCode;
	private String url;
	private String extraPara;
}
