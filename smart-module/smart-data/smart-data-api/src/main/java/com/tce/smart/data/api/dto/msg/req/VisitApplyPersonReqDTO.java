package com.tce.smart.data.api.dto.msg.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 访客人员申请表
 * @date: 2021/4/1 0001 17:21
 * @author: fushiping
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitApplyPersonReqDTO {

	/**
	 * 姓名
	 */
	private String xm;

	/**
	 * 证件类型
	 */
	private String zjlx;

	/**
	 * 证件号码
	 */
	private String zjhm;

	/**
	 * 访客照片
	 */
	private String fkzp2;

	/**
	 * 证件照片
	 */
	private String zjzp2;

	/**
	 * 进入开始日期时间
	 */
	private String jrkssj;

	/**
	 * 进入结束日期时间
	 */
	private String jrjssj;

}
