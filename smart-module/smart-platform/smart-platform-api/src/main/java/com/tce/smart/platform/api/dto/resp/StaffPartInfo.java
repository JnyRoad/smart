package com.tce.smart.platform.api.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 员工部分信息
 * @date: 2020-07-29 11:38
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StaffPartInfo {
	/**
	 * 员工工号
	 */
	private String badge;

	/**
	 * 员工信息拼接成字符串
	 */
	private String info;
}
