package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.dto.TaskDownRecordDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.mapper.SmtDeviceMapper;
import com.tce.smart.platform.core.mapper.SmtIscDownRecordMapper;
import com.tce.smart.platform.core.model.TaskDownRecordPark;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.core.vo.TaskDownRecordVO;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务下发记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Service
@Slf4j
@AllArgsConstructor
public class SmtIscDownRecordServiceImpl extends ServiceImpl<SmtIscDownRecordMapper, SmtIscDownRecord> implements SmtIscDownRecordService {

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
	public void handleTaskDownRecord(SmtIscDeviceTask smtDeviceTask) {
		if (isTemporaryAccessRecord(smtDeviceTask) && StrUtil.isBlank(smtDeviceTask.getPersonId())) {
			log.warn("临时人员ISC下发记录缺少personId，跳过记录维护，taskId={}, deviceCode={}, cardNo={}",
					smtDeviceTask.getId(), smtDeviceTask.getDeviceCode(), smtDeviceTask.getCardNo());
			return;
		}

		List<SmtIscDownRecord> taskDownRecords = this.list(buildDownRecordQuery(smtDeviceTask));
		if (taskDownRecords.isEmpty()) {
			if (smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DOWN.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DOWN.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.UPDATE.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_UPDATE.getCode())) {
				//下发成功操作
				addDownRecord(smtDeviceTask);
			} else if (smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DEL.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DEL.getCode())) {
				staffDeviceAuthSyncService.syncAfterDelete(smtDeviceTask.getDeviceCode(), smtDeviceTask.getCardNo(),
						smtDeviceTask.getGeneral(), smtDeviceTask.getDeviceType(), smtDeviceTask.getServiceType());
			}
		} else {
			if (smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DEL.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_DEL.getCode())) {
				//删除
				removeDownRecords(taskDownRecords);
				staffDeviceAuthSyncService.syncAfterDelete(taskDownRecords.get(0));
			} else if (smtDeviceTask.getAction().equals(DeviceTaskActionEnum.UPDATE.getCode())
					|| smtDeviceTask.getAction().equals(DeviceTaskActionEnum.DELAY_UPDATE.getCode())) {
				//修改 先删除原来的记录 再添加新记录
				removeDownRecords(taskDownRecords);
				addDownRecord(smtDeviceTask);
			}
		}
	}

	private void removeDownRecords(List<SmtIscDownRecord> taskDownRecords) {
		this.removeByIds(taskDownRecords.stream().map(SmtIscDownRecord::getId).collect(Collectors.toList()));
	}

	LambdaQueryWrapper<SmtIscDownRecord> buildDownRecordQuery(SmtIscDeviceTask smtDeviceTask) {
		LambdaQueryWrapper<SmtIscDownRecord> query = new LambdaQueryWrapper<SmtIscDownRecord>()
				.eq(SmtIscDownRecord::getDeviceCode, smtDeviceTask.getDeviceCode())
				.eq(SmtIscDownRecord::getDeviceType, smtDeviceTask.getDeviceType());
		if (isStaffCardFaceServiceType(smtDeviceTask)) {
			query.in(SmtIscDownRecord::getServiceType,
					DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskConstants.UPDATE_FACE);
		} else {
			query.eq(SmtIscDownRecord::getServiceType, downRecordServiceType(smtDeviceTask.getServiceType()));
		}
		if (isTemporaryAccessRecord(smtDeviceTask) && StrUtil.isNotBlank(smtDeviceTask.getPersonId())) {
			query.eq(SmtIscDownRecord::getPersonId, smtDeviceTask.getPersonId());
			if (smtDeviceTask.getParkId() != null) {
				query.eq(SmtIscDownRecord::getParkId, smtDeviceTask.getParkId());
			}
			return query;
		}
		return query.eq(SmtIscDownRecord::getCardNo, smtDeviceTask.getCardNo());
	}

	private Integer downRecordServiceType(Integer serviceType) {
		if (DeviceTaskConstants.UPDATE_FACE.equals(serviceType)) {
			return DeviceTaskConstants.CARD_STAFF_IMPORT;
		}
		return serviceType;
	}

	private boolean isStaffCardFaceServiceType(SmtIscDeviceTask smtDeviceTask) {
		return smtDeviceTask != null
				&& DeviceTaskConstants.CARD.equals(smtDeviceTask.getDeviceType())
				&& (DeviceTaskConstants.CARD_STAFF_IMPORT.equals(smtDeviceTask.getServiceType())
				|| DeviceTaskConstants.UPDATE_FACE.equals(smtDeviceTask.getServiceType()));
	}

	private boolean isTemporaryAccessRecord(SmtIscDeviceTask smtDeviceTask) {
		return smtDeviceTask != null
				&& DeviceTaskConstants.CARD.equals(smtDeviceTask.getDeviceType())
				&& (DeviceTaskConstants.CARD_VISITOR.equals(smtDeviceTask.getServiceType())
				|| DeviceTaskConstants.CARD_ADMITTANCE.equals(smtDeviceTask.getServiceType()));
	}

	private void addDownRecord(SmtIscDeviceTask smtDeviceTask){
		SmtIscDownRecord taskDownRecord = new SmtIscDownRecord();
		BeanUtil.copyProperties(smtDeviceTask,taskDownRecord);
		taskDownRecord.setPersonId(smtDeviceTask.getPersonId());
		taskDownRecord.setBadge(smtDeviceTask.getBadge());
		taskDownRecord.setImageId(smtDeviceTask.getImageId());
		taskDownRecord.setTaskId(smtDeviceTask.getId());
		taskDownRecord.setCreateTime(LocalDateTime.now());
		taskDownRecord.setStartTime(DateUtil.date(smtDeviceTask.getStartTime() * 1000));
		taskDownRecord.setOverTime(DateUtil.date(smtDeviceTask.getOverTime() * 1000));
		//下发记录表 只保存下发成功且未删除的记录
		taskDownRecord.setAction(DeviceTaskActionEnum.DOWN.getCode());
		SmtDevice smtDevice = smtDeviceMapper.selectById(smtDeviceTask.getDeviceCode());
		taskDownRecord.setParkId(smtDevice.getParkId());
		taskDownRecord.setServiceType(downRecordServiceType(smtDeviceTask.getServiceType()));
		taskDownRecord.setTaskType(DeviceTaskStatusEnum.SUCCESS.getCode());
		taskDownRecord.setRemark("");
		this.save(taskDownRecord);
	}
}
