package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.dto.TaskDownRecordDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtTaskDownRecord;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtDeviceTaskMapper;
import com.tce.smart.platform.core.mapper.SmtTaskDownRecordMapper;
import com.tce.smart.platform.core.model.TaskDownRecordPark;
import com.tce.smart.platform.core.vo.TaskDownRecordVO;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务下发记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Service
@Slf4j
@AllArgsConstructor
public class SmtTaskDownRecordServiceImpl extends ServiceImpl<SmtTaskDownRecordMapper, SmtTaskDownRecord> implements SmtTaskDownRecordService {

	private final SmtDeviceMapper smtDeviceMapper;

	private final StaffDeviceAuthSyncService staffDeviceAuthSyncService;

	@Override
	public IPage<TaskDownRecordVO> getVehicle(Page page, TaskDownRecordDTO taskDownRecordDTO) {
		return this.baseMapper.getVehicle(page,taskDownRecordDTO);
	}

	@Override
	public IPage<TaskDownRecordVO> getPerson(Page page, TaskDownRecordDTO taskDownRecordDTO) {
		return this.baseMapper.getPerson(page,taskDownRecordDTO);
	}

	@Override
	public List<TaskDownRecordPark> getTree(List<Integer> parkIds, Integer type) {
		List<TaskDownRecordPark> list = this.baseMapper.getPark(parkIds);
		for (TaskDownRecordPark areaTree : list) {
			areaTree.setChildren(this.baseMapper.getDevice(areaTree.getValue(), type));
		}
		return list;
	}

	@Transactional
	@Override
	public void handleTaskDownRecord(SmtDeviceTask smtDeviceTask) {

		SmtTaskDownRecord taskDownRecord = this.getOne(new LambdaQueryWrapper<SmtTaskDownRecord>()
				.eq(SmtTaskDownRecord::getDeviceCode, smtDeviceTask.getDeviceCode())
				.eq(SmtTaskDownRecord::getCardNo, smtDeviceTask.getCardNo())
		);
		if(null == taskDownRecord
				&&
				(smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DOWN.getCode())
				|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DOWN.getCode())
						|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.UPDATE.getCode())
						|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_UPDATE.getCode()))){
			//下发成功操作
			addDownRecord(smtDeviceTask);
		} else if(null != taskDownRecord
				&&
				(smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DEL.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DEL.getCode())
			)){
			//删除
			this.removeById(taskDownRecord.getId());
			staffDeviceAuthSyncService.syncAfterDelete(taskDownRecord);
		} else if(null == taskDownRecord
				&&
				(smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DEL.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DEL.getCode())
			)){
			staffDeviceAuthSyncService.syncAfterDelete(smtDeviceTask.getDeviceCode(), smtDeviceTask.getCardNo(),
					smtDeviceTask.getGeneral(), smtDeviceTask.getDeviceType(), smtDeviceTask.getServiceType());
		} else if(null != taskDownRecord
				&&
				(smtDeviceTask.getAction().equals(DeviceTaskActionEnum.UPDATE.getCode())
						|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_UPDATE.getCode())
				)){
			//修改 先删除原来的记录 再添加新记录
			this.removeById(taskDownRecord.getId());
			addDownRecord(smtDeviceTask);
		}
	}

	private void addDownRecord(SmtDeviceTask smtDeviceTask){
		SmtTaskDownRecord taskDownRecord = new SmtTaskDownRecord();
		BeanUtil.copyProperties(smtDeviceTask,taskDownRecord);
		taskDownRecord.setImageId(smtDeviceTask.getImageId());
		taskDownRecord.setTaskId(smtDeviceTask.getId());
		taskDownRecord.setCreateTime(LocalDateTime.now());
		taskDownRecord.setStartTime(DateUtil.date(smtDeviceTask.getStartTime() * 1000));
		taskDownRecord.setOverTime(DateUtil.date(smtDeviceTask.getOverTime() * 1000));
		//下发记录表 只保存下发成功且未删除的记录
		taskDownRecord.setAction(DeviceTaskActionEnum.DOWN.getCode());
		SmtDevice smtDevice = smtDeviceMapper.selectById(smtDeviceTask.getDeviceCode());
		taskDownRecord.setParkId(smtDevice.getParkId());
		taskDownRecord.setServiceType(smtDeviceTask.getServiceType());
		taskDownRecord.setTaskType(DeviceTaskStatusEnum.SUCCESS.getCode());
		taskDownRecord.setRemark("");
		this.save(taskDownRecord);
	}
}
