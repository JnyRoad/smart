package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.AddSnapVehicleDTO;
import com.tce.smart.platform.core.dto.LogisticsAppointmentDTO;
import com.tce.smart.platform.core.entity.SmtLogisticsAppointment;

import java.util.List;

/**
 * 物流车预约信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:27
 */
public interface SmtLogisticsAppointmentService extends IService<SmtLogisticsAppointment> {

	/**
	 * 获取物流车预约统计信息
	 *
	 * @param smtLogisticsAppointment 车辆人员信息
	 * @return 返回保存结果
	 */
	IPage getLogisticsAppointment(Page page, SmtLogisticsAppointment smtLogisticsAppointment, List<Integer> parkIds);

	/**
	 * 抓拍车辆如果是物流车则补全车辆记录信息，否则不处理
	 * @param entity 抓拍车辆信息
	 * @return boolean
	 */
	void logisticsAppointmentHandle(AddSnapVehicleDTO entity);

	/**
	 * 添加物流车预约信息
	 * @param entity 物流车预约信息
	 * @return boolean
	 */
	boolean saveLogisticsAppointment(LogisticsAppointmentDTO entity);


	/**
	 * 手动进厂
	 * @param id 物流车预约信息
	 * @return  boolean
	 */
	boolean manualEnter(Long id);

	/**
	 * 返回预约
	 * @param id 物流车预约信息
	 * @return boolean
	 */
	boolean goOrder(Long id);

	/**
	 * 手动离厂
	 * @param id 物流车预约信息
	 * @return  boolean
	 */
	boolean manualLeave(Long id);

	/**
	 * 取消预约
	 * @param id 物流车预约信息
	 * @return  boolean
	 */
	boolean cancelOrder(Long id);

	/**
	 * 返回在厂
	 * @param id 物流车预约信息
	 * @return  boolean
	 */
	boolean goIn(Long id);

	/**
	 * 更新超时状态
	 * @return 返回结果
	 */
	boolean updateStatus();
}
