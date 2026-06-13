package com.tce.smart.platform.api.dto.req.badge;


import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 厂牌补领设置
 * @author fushiping
 * @date 2020/7/8 11:44
 **/
@Data
public class EditBadgeApplyReqDTO extends BaseDTO {

	/**
	 * 主键
	 */
	@ApiModelProperty("主键")
	private Long id;

	/**
	 * 员工工号
	 */
	@ApiModelProperty("员工工号")
	private String staffNo;
	/**
	 * 园区
	 */
	@ApiModelProperty("园区")
	private Integer parkId;
	/**
	 * 申请原因
	 */
	@ApiModelProperty("申请原因")
	private Integer reason;
	/**
	 * 办理状态
	 */
	@ApiModelProperty("办理状态")
	private Integer state;
	/**
	 * 价格
	 */
	@ApiModelProperty("价格")
	private BigDecimal price;
	/**
	 * 领取地址
	 */
	@ApiModelProperty("领取地址")
	private String address;
	/**
	 * 备注
	 */
	@ApiModelProperty("备注")
	private String remark;
	/**
	 * 拒绝原因
	 */
	@ApiModelProperty("拒绝原因")
	private String refuseReason;

}
