package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;
import java.util.Date;

import javax.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import cn.hutool.core.date.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工车辆申请表
 * @author 齐佩
 *
 */
@Data
@TableName("smt_vehicle_apply")
@EqualsAndHashCode(callSuper = true)
public class SmtVehicleApply  extends Model<SmtVehicleApply> {

	private static final long serialVersionUID = 1L;


	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;


	/**
	 * 车辆id
	 */
	private Long vehicleId;

	/**
	 * 车牌号
	 */
	private String vehiclePlate;


	/**
	 * 园区id
	 */
	private String parkId;


	/**
	* 申请时间
	*/
	private LocalDateTime createTime;


	/**
	 * 申请状态0-审批中   1-已审批   2-已拒绝
	 */
	private Integer status;

	/**
	 * 拒绝原因
	 */
	private String reason;

	/**
	 * 审批人
	 */
	private String approver;

	/**
	 * 审批时间
	 */
	private LocalDateTime updateTime;


	/**
	 *  车辆的权限ID
	 */
	private Integer authorityId;

}
