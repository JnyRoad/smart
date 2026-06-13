package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.TaskDownRecordDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtTaskDownRecord;
import com.tce.smart.platform.core.model.TaskDownRecordPark;
import com.tce.smart.platform.core.vo.TaskDownRecordVO;

import java.util.List;

/**
 * 任务下发记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
public interface SmtTaskDownRecordService extends IService<SmtTaskDownRecord> {

	IPage<TaskDownRecordVO> getVehicle(Page page, TaskDownRecordDTO taskDownRecordDTO);

	IPage<TaskDownRecordVO> getPerson(Page page, TaskDownRecordDTO taskDownRecordDTO);

	List<TaskDownRecordPark> getTree(List<Integer> parkIds, Integer type);

	/**
	 * 添加卡片下发记录
	 * @param smtDeviceTask
	 */
	void handleTaskDownRecord(SmtDeviceTask smtDeviceTask);
}
