package com.tce.smart.platform.core.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 车辆记录查询
 *
 * @author 王艳勇
 * @date 2019-04-13 18:19:30
 */
@Data
public class SnapVehicleDTO implements Serializable {

	private static final long serialVersionUID = 1L;

    /**
   * 车牌号
   */
    private String vehiclePlate;

    /**
     * 园区ID
     */
    private Integer parkId;

    /**
   * 事件类型：1-进；2-出；
   */
    private Integer eventType;

    /**
     * 车主姓名
     */
    private String driverName;

	/**
	 * 1：全部记录；2：当前停车
	 */
	private Integer allFlag;

	/**
	 * 抓拍开始时间
	 */
	private String startTime;

	/**
	 * 抓拍结束时间
	 */
	private String endTime;

    /**
     * 停车场ID
     */
    private String parkingId;

	/**
	 * 园区ID
	 */
    private List<Integer> parkIds;
}
