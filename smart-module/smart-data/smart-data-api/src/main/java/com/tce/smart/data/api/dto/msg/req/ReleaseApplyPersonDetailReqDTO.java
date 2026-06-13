package com.tce.smart.data.api.dto.msg.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 人员放行明细
 * @date: 2021/4/1 0001 17:21
 * @author: wuling
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReleaseApplyPersonDetailReqDTO {
	/**
	 * 姓名
	 */
	private String xm;

	/**
	 * 工号
	 */
	private String gh;

	/**
	 * 离厂事由
	 */
	private String lcsy;

	/**
	 * 离厂日期
	 */
	private String lcrq;

	/**
	 * 离厂时间
	 */
	private String lcsj;

	/**
	 * 返厂日期
	 */
	private String fcrq;

	/**
	 * 返厂时间
	 */
	private String fcsj;

	/**
	 * 级别
	 */
	private String jb;
}
