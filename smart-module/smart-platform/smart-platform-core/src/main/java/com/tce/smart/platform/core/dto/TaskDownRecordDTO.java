package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 设备绑定车辆信息
 * @author Administrator
 *
 */
@Data
public class TaskDownRecordDTO extends Model<TaskDownRecordDTO> {

    private static final long serialVersionUID = 1L;

    private String cardNo;

    private String general;

    private String personName;

	private Integer taskType;

	private String deviceCode;

	private String deviceName;

	private Integer areaId;
	/**
	 * 1：下发；2：删除
	 */
	private Integer action;

	private String badge;

	private Integer serviceType;

	/**
	 * 下发开始时间
	 */
	private String startTime;

	/**
	 * 下发结束时间
	 */
	private String endTime;

	private List<Integer> parkIds;

	/**
	 * 设备类型
	 */
	private Integer deviceType;
}
