package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author sunfujian
 * @date 2021/8/16 15:43
 */
@Data
public class ReleaseApplyThingDetail extends BaseDTO {
	private Long id;
	/**
	 * 申请人工号
	 */
	@ApiModelProperty(value = "申请人工号")
	private String badge = "";
	/**
	 * 资产编码
	 */
	@ApiModelProperty(value = "资产编码")
	private String wpbm = "";

	/**
	 * 名称
	 */
	@ApiModelProperty(value = "名称")
	private String wpmc = "";

	/**
	 * 单位
	 */
	@ApiModelProperty(value = "单位")
	private String wpdw = "";

	/**
	 * 数量
	 */
	@ApiModelProperty(value = "数量")
	private String wpsl = "";

	/**
	 * 物品流向
	 */
	@ApiModelProperty(value = "物品流向")
	private String wplx = "";

	/**
	 * 接收单位
	 */
	@ApiModelProperty(value = "接收单位")
	private String jsdw = "";

	/**
	 * 备注(原因)
	 */
	@ApiModelProperty(value = "备注(原因)")
	private String bz = "";

	/**
	 * 运输方式
	 */
	@ApiModelProperty(value = "运输方式")
	private String ysfs = "";

	/**
	 * 姓名
	 */
	@ApiModelProperty(value = "姓名")
	private String xm = "";
	private String name;

	/**
	 * 车牌号
	 */
	@ApiModelProperty(value = "车牌号")
	private String cph = "";

	/**
	 * 放行日期
	 */
	@ApiModelProperty(value = "放行日期")
	private String fxrq = "";

	/**
	 * 是否返厂
	 */
	@ApiModelProperty(value = "是否返厂")
	private String wpsffc = "";

	/**
	 * 返厂日期
	 */
	@ApiModelProperty(value = "返厂日期")
	private String wpfcrq = "";

	/**
	 * 返厂时间
	 */
	@ApiModelProperty(value = "返厂时间")
	private String wpfcsj = "";
}
