package com.tce.smart.platform.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 物流车预约信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LogisticsAppointmentVO extends BaseVO {
    /**
   * 主键
   */
	@JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long id;
    /**
   * 园区主键
   */
    private Integer parkId;
    /**
   * 车牌号
   */
    private String vehiclePlate;
    /**
   * 司机名称
   */
    private String driverName;
    /**
   * 司机手机号
   */
    private String driverPhone;
    /**
   * 供应商名称
   */
    private String supplier;
    /**
     * 预约到达时间
     */
    private Date startTime;
	/**
	 * 预约离开时间
	 */
    private Date endTime;
    /**
     * 实际到达时间
     */
    private Date arrivalTime;
    /**
     * 实际离开时间
     */
    private Date leaveTime;
    /**
   * 排程编号
   */
    private String planCode;
    /**
   * 预约状态：1-已预约；2-已到达；3-已离开；4-已超时；5-取消预约
   */
    private Integer status;
    /**
   * 创建时间
   */
    private Date createTime;

	/**
	 * 园区名称
	 */
	private String parkName;

    private String snapPhotoId;


	private String areaName;
    private String eventTypeDesc;
    private Date snapTime;

}
