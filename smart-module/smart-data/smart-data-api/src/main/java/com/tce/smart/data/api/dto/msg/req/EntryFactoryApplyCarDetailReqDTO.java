package com.tce.smart.data.api.dto.msg.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 车辆通行办理明细
 * @date: 2021/4/1 0001 17:21
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntryFactoryApplyCarDetailReqDTO {
	/**
	 * 司机姓名
	 */
	private String sjxm;

	/**
	 * 司机籍贯
	 */
	private String sjjg;

	/**
	 * 驾驶证号
	 */
	private String jszh;

	/**
	 * 紧急联络人
	 */
	private String jjllr;

	/**
	 * 紧急联络人方式
	 */
	private String llfs;

	/**
	 * 车型
	 */
	private String cx;

	/**
	 * 颜色
	 */
	private String ys;

	/**
	 * 车牌号
	 */
	private String cph;

	/**
	 * 车辆类型
	 */
	private String cllx;

	/**
	 * 证件类型
	 */
	private String zjlx;

	/**
	 * 相关证件
	 */
	private String xgzj;
}
