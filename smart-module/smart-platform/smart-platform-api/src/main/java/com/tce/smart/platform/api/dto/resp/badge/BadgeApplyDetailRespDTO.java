package com.tce.smart.platform.api.dto.resp.badge;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 厂牌补领详情（APP端）
 * @author fushiping
 * @date 2020/7/8 11:44
 **/
@Data
public class BadgeApplyDetailRespDTO extends BaseDTO {
	/**
	 * ID
	 */
	@ApiModelProperty("ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	/**
	 * 员工工号
	 */
	@ApiModelProperty("员工工号")
	private String badge;
	/**
	 * 员工姓名
	 */
	@ApiModelProperty("员工姓名")
	private String name;
	/**
	 * BU名
	 */
	@ApiModelProperty("BU名")
	private String compName;
	/**
	 * 部门名
	 */
	@ApiModelProperty("拒绝原因")
	private String depName;
	/**
	 * 园区名
	 */
	@ApiModelProperty("园区名")
	private String parkName;
	/**
	 * 申请时间
	 */
	@ApiModelProperty("申请时间")
	private LocalDateTime createTime;
	/**
	 * 申请原因
	 */
	@ApiModelProperty("申请原因")
	private String reason;
	/**
	 * 办理状态
	 */
	@ApiModelProperty("办理状态")
	private Integer state;
	/**
	 * 办理状态描述
	 */
	@ApiModelProperty("拒绝原因")
	private String stateDesc;
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
	 * 价格
	 */
	@ApiModelProperty("价格")
	private BigDecimal price;
	/**
	 * 拒绝原因
	 */
	@ApiModelProperty("拒绝原因")
	private String refuseReason;

	@ApiModelProperty("流程列表")
	private List<BadgeRecordRespDTO> operaList;

}
