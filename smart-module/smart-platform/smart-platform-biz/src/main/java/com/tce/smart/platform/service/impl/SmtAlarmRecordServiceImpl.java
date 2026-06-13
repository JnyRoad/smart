package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.enums.AlarmType;
import com.tce.smart.platform.core.dto.AlarmRecordDTO;
import com.tce.smart.platform.core.entity.SmtAlarmRecord;
import com.tce.smart.platform.core.mapper.SmtAlarmRecordMapper;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.service.SmtAlarmRecordService;
import com.tce.smart.platform.service.SmtDeviceAreaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 警报记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:38
 */
@Service
@Slf4j
@AllArgsConstructor
public class SmtAlarmRecordServiceImpl extends ServiceImpl<SmtAlarmRecordMapper, SmtAlarmRecord> implements SmtAlarmRecordService {
	private final SmtDeviceAreaService smtDeviceAreaService;
	private final SmtDeviceService smtDeviceService;
	private final SmtAlarmRecordMapper smtAlarmRecordMapper;
	@Override
	public boolean saveSmtAlarmRecord(SmtAlarmRecord entity) {
		log.debug("收到警报消息：{}",entity);
		//设备区域信息处理
		smtDeviceAreaService.areaHandle(entity);
		//设备信息处理
		smtDeviceService.deviceHandle(entity);
		entity.setCreateTime(DateUtil.date());
		entity.setAlarmName(AlarmType.desc(entity.getAlarmType()));
		return this.save(entity);
	}

	@Override
	public IPage getAlarmRecord(Page page, AlarmRecordDTO alarmRecordDTO,String[] alarmTime) {
		if(ArrayUtil.isNotEmpty(alarmTime) && alarmTime.length == 2) {
			alarmRecordDTO.setStartTime(alarmTime[0]);
			alarmRecordDTO.setEndTime(alarmTime[1]);
		}
		if(ObjectUtil.isNotNull(alarmRecordDTO.getParkId()) || ObjectUtil.isNotNull(alarmRecordDTO.getPid())) {
			List<Integer> list = smtAlarmRecordMapper.getAreaId(alarmRecordDTO.getParkId(), alarmRecordDTO.getPid());
			if(CollectionUtil.isNotEmpty(list)) {
				alarmRecordDTO.setList(list);
			}else {
				list = new ArrayList<>();
				list.add(-1);
				alarmRecordDTO.setList(list);
			}
		}
		return smtAlarmRecordMapper.getAlarmRecord(page, alarmRecordDTO);
	}
}
