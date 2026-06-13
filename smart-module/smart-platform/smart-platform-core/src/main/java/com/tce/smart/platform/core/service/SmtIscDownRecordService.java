package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.TaskDownRecordDTO;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.model.TaskDownRecordPark;
import com.tce.smart.platform.core.vo.TaskDownRecordVO;

import java.util.List;

/**
 * ISC任务下发记录表
 *
 * @author wuling
 * @date 2021-08-25 15:09:27
 */
public interface SmtIscDownRecordService extends IService<SmtIscDownRecord> {

	IPage<TaskDownRecordVO> getVehicle(Page page, TaskDownRecordDTO taskDownRecordDTO);

	IPage<TaskDownRecordVO> getPerson(Page page, TaskDownRecordDTO taskDownRecordDTO);

	List<TaskDownRecordPark> getTree(List<Integer> parkIds, Integer type);

	/**
	 * 添加卡片下发记录
	 * @param smtDeviceTask
	 */
	void handleTaskDownRecord(SmtIscDeviceTask smtDeviceTask);
}
