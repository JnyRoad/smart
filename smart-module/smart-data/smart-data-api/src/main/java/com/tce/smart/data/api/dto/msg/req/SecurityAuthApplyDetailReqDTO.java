package com.tce.smart.data.api.dto.msg.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 授权人员明细表
 * @date: 2021/4/1 0001 17:21
 * @author: wuling
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityAuthApplyDetailReqDTO {
	/**
	 * 授权人工号
	 */
	private String sqrgh;

	/**
	 * 授权人姓名
	 */
	private String sqrxm;

	/**
	 * 申请事由
	 */
	private String sqsy;

	/**
	 * 授权人职务
	 */
	private String sqrzw;

	/**
	 * 授权人部门
	 */
	private String sqrbm;
}
