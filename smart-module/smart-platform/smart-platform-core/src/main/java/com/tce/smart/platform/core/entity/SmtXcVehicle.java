package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 许昌车辆信息表
 *
 */
@Data
@TableName("SMT_XC_VEHICLE")
@EqualsAndHashCode(callSuper = true)
public class SmtXcVehicle extends Model<SmtXcVehicle> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId(value = "id", type = IdType.ID_WORKER)
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long id;
    /**
   * 园区ID
   */
    private Integer parkId;
    /**
   * 车牌号
   */
    @NotBlank(message = "车牌号不能为空")
    @Pattern(regexp = "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$",message = "车牌号格式不正确")
    private String vehiclePlate;
    /**
   * 车辆品牌
   */
    @NotBlank(message = "车辆品牌不能为空")
    private String vehicleBrand;
    /**
   * 车辆颜色
   */
    @NotNull(message = "车辆颜色不能为空")
    private Integer vehicleColor;
    /**
   * 车辆类型:(区分轿车、货车等)
   */
    @NotNull(message = "车辆类型不能为空")
    private Integer vehicleType;

	/**
	 * 联系人工号
	 */
	private String staffBadge;

	/**
	 * 联系人
	 */
	private String contactsUser;

	/**
	 * 联系电话
	 */
	private String contactsPhone;

    /**
   * 是否删除：0未删；1：删除
   */
    private Integer isDelete;

	/**
	 * 有效开始时间
	 */
	private LocalDate startDate;

	/**
	 * 有效结束时间
	 */
	private LocalDate endDate;

	/**
	 * 车牌状态 0-挂失 1.过期
	 */
	private Integer cardState;

	/**
	 * 车牌类型  0-临时车牌 1-月租车牌 2-充值车牌 3-贵宾车牌 4-免费车牌 8-收费月租车牌
	 */
	private Integer ctId;

	/**
	 * 操作人
	 */
	private String optUser;

    /**
   * 创建时间
   */
    private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;
}
