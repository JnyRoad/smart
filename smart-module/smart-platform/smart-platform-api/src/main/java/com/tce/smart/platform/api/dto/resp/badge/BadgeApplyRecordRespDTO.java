package com.tce.smart.platform.api.dto.resp.badge;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 厂牌补领记录（APP端）
 * @author fushiping
 * @date 2020/7/8 11:44
 **/
@Data
public class BadgeApplyRecordRespDTO extends BaseDTO {
	/**
	 * ID
	 */
	@ApiModelProperty("ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	/**
	 * 员工姓名
	 */
	@ApiModelProperty("员工姓名")
	private String name;
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
	@ApiModelProperty("办理状态描述")
	private String stateDesc;
	/**
	 * 领取地址
	 */
	@ApiModelProperty("领取地址")
	private String address;
	/**
	 * 拒绝原因
	 */
	@ApiModelProperty("拒绝原因")
	private String refuseReason;
}
