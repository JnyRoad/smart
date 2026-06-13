package com.tce.smart.platform.core.dto.commonsd;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description: 员工水电规则查询DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class StaffSDRuleReqDTO {

	/**
	 * 员工工号列表
	 */
	private List<String> staffBadgeList;

	/**
	 * 月份
	 */
	private Integer monthNum;
}
