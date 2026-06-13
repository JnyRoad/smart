package com.tce.smart.data.api.dto.msg.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 物品放行明细
 * @date: 2021/4/1 0001 17:21
 * @author: wuling
 * @version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReleaseApplyThingDetailReqDTO {
	/**
	 * 资产编码
	 */
	private String wpbm;

	/**
	 * 名称
	 */
	private String wpmc;

	/**
	 * 单位
	 */
	private String wpdw;

	/**
	 * 数量
	 */
	private String wpsl;

	/**
	 * 物品流向
	 */
	private String wplx;

	/**
	 * 接收单位
	 */
	private String jsdw;

	/**
	 * 备注(原因)
	 */
	private String bz;

	/**
	 * 运输方式
	 */
	private String ysfs;

	/**
	 * 姓名
	 */
	private String xm;

	/**
	 * 车牌号
	 */
	private String cph;

	/**
	 * 放行日期
	 */
	private String fxrq;

	/**
	 * 是否返厂
	 */
	private String wpsffc;

	/**
	 * 返厂日期
	 */
	private String wpfcrq;

	/**
	 * 返厂时间
	 */
	private String wpfcsj;
}
