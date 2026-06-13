package com.tce.smart.platform.core.dto;

import java.time.LocalDateTime;
import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.platform.core.entity.SmtVehicleApply;

import cn.hutool.core.date.DateTime;
import lombok.Data;

/**
 * 员工车辆申请添加
 * @author dell
 *
 */
@Data
public class ApplyVehicleDTO  extends SmtVehicleApply{



	/**
	 * 员工号
	 */
	private Long staffBadge;


	private String parkId;
    /**
   * 车牌号
   */
    private String vehiclePlate;
    /**
   * 车辆品牌
   */
    private String vehicleBrand;
    /**
   * 车辆颜色
   */
    private Integer vehicleColor;
    /**
   * 车辆类型:(区分轿车、货车等)
   */
    private Integer vehicleType;

    /**
   * 车辆归属分类：0:园区车辆；1：员工车辆；2：访客车辆；3：物流车辆
   */
    private Integer vehicleAscription;
    /**
   * 是否删除：0未删；1：删除
   */
    private Integer isDelete;
    /**
   * 创建时间
   */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime createTime;

	/**
	 * 驾驶证图片
	 */
	  @NotBlank(message = "驾驶证图片不能为空")
	  private String driverLicense;
	  /**
	 * 行驶证图片
	 */
	  @NotBlank(message = "行驶证图片不能为空")
	  private String drivinglLicense;


}
