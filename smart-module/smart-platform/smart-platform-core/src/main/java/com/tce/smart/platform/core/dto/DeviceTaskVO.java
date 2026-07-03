package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceTaskVO extends Model<DeviceTaskVO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 图片ID
	 */
	private String imageId;

	/**
	 * 卡片类型
	 */
	private Integer cardType;

	/**
	 * 公共字段
	 */
	private String general;

	/**
	 * 设备编码
	 */
	private String deviceCode;

	/**
	 * 卡片Id
	 */
	private String cardNo;

	/**
	 * 1：下发；2：删除
	 */
	private Integer action;
	/**
	 * 1：卡片
	 * 2：车辆
	 */
	private Integer deviceType;
	/**
	 * 开始时间（秒）
	 */
	private Long startTime;
	/**
	 * 截止时间（秒）
	 */
	private Long overTime;

	/**
	 * 人脸下发记录的业务类型：
	 * 1、导入员工照片
	 * 2、APP信息完善
	 * 3、访客预约
	 * 4、员工扫码登记
	 * <p>
	 * 车辆下发记录的业务类型：
	 * 1、员工车辆
	 * 2、公司车辆
	 * 3、非员工车辆
	 * 4、访客预约
	 * 5、物流车预约
	 */
	private Integer serviceType;

	private Integer status;

	private String serialNo;
	/**
	 * 申请人工号
	 */
	private String applyBadge;

	/**
	 * 入厂申请单ID（非入厂申请来源为NULL）
	 * 仅 ISC 设备任务路由分支（SmtIscDeviceTaskService.saveTask）会落库到 SmtIscDeviceTask，
	 * 非 ISC 分支（SmtDeviceTask）无对应字段，按现状忽略
	 */
	private Long applyId;

	/**
	 * 下发批次号（同一次 updateStatus 原子提交的任务集共享，见 SmtIscDeviceTask.batchId）
	 */
	private Long batchId;

}
