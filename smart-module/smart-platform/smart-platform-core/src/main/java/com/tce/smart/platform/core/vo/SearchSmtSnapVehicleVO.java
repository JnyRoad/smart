package com.tce.smart.platform.core.vo;
import java.util.Date;

import lombok.Data;

/**
 * 查询出入车辆抓拍记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:20
 */
@Data
public class SearchSmtSnapVehicleVO{

    /**
   * 主键
   */
    private Integer id;
    /**
     * 车牌号
     */
    private String vehiclePlate;
    /**
   *
   */
    private String areaName;
    /**
   * 车辆图片ID
   */
    private String snapPhotoId;
    /**
   *
   */
    private Integer eventType;
    /**
   * 通过时间
   */
    private Date snapTime;
    /**
   *
   */
    private String driverName;
    /**
   *
   */
    private String driverPhone;
    /**
     * 方式
     */
    private String company;

	/**
	 * bu名称
	 */
    private String compName;


	/**
	 * 是否放行 0-未放行;1-放行;2-未知
	 */
	private Integer letPass;

	/**
	 * 权限 0:没有；1：有
	 */
	private Integer authority;

	/**
	 * 所属园区
	 */
	private String parkName;


}
