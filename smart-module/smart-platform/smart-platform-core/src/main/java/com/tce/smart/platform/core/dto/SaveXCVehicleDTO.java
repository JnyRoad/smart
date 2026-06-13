package com.tce.smart.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * 许昌车辆信息添加
 */
@Data
public class SaveXCVehicleDTO {

    /**
   * 车牌号
   */
    @NotBlank(message = "车牌号不能为空")
    @Pattern(regexp = "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳使领]))$",message = "车牌号格式不正确")
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
	 * 有效开始日期
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	/**
	 * 有效结束日期
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;


	/**
	 * 操作人
	 */
	private String optUser;

	/**
	 * 车牌状态 0-挂失 1.过期
	 */
	private Integer cardState;

	/**
	 * 车牌类型  0-临时车牌 1-月租车牌 2-充值车牌 3-贵宾车牌 4-免费车牌 8-收费月租车牌
	 */
	private Integer ctId;
}
