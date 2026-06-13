package com.tce.smart.data.api.dto.msg.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 保密区预约明细表
 * @date: 2021/4/1 0001 17:21
 * @author: wuling
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityAreaVisitDetailReqDTO {
	/**
	 * 姓名
	 */
	private String xingm;

	/**
	 * 身份证
	 */
	private String shenfz;

	/**
	 * 闸机人脸号
	 */
	private Integer zjrlh;

	/**
	 * 人脸登记状态	0 已登记，1 不需再录人脸 未登记，2 需录人脸
	 */
	private Integer djzt;
}
