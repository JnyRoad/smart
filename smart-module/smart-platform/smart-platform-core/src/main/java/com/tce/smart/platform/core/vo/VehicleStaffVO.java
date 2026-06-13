package com.tce.smart.platform.core.vo;

import com.tce.smart.platform.core.entity.SmtStaff;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleStaffVO extends SmtStaff {

	private static final long serialVersionUID = 1L;
	/**
   * 车辆归属分类：0:园区车辆；1：员工车辆；2：访客车辆；3：物流车辆
   */
    private Integer vehicleAscription;

    /**
     *车辆品牌
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
}
