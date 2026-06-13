package com.tce.smart.platform.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.dto.AlarmTemplateDTO;
import com.tce.smart.platform.core.entity.SmtAlarmRecever;
import com.tce.smart.platform.core.entity.SmtAlarmTemplate;
import com.tce.smart.platform.core.mapper.SmtAlarmTemplateMapper;
import com.tce.smart.platform.service.SmtAlarmReceverService;
import com.tce.smart.platform.service.SmtAlarmTemplateService;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;

/**
 * 警报信息记录
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@Service
@AllArgsConstructor
public class SmtAlarmTemplateServiceImpl extends ServiceImpl<SmtAlarmTemplateMapper, SmtAlarmTemplate> implements SmtAlarmTemplateService {
	private final SmtAlarmReceverService smtAlarmReceverService;
	@Override
	public boolean saveSmtAlarmRecever(AlarmTemplateDTO entity) {
		DateTime dateTime = DateUtil.date();
		entity.setCreateTime(dateTime);
		boolean result = this.save(entity);
		List<SmtAlarmRecever> smtAlarmReceverList = entity.getAlarmReceverList();
		smtAlarmReceverList.stream().forEach(smtAlarmRecever ->{
			smtAlarmRecever.setTemplateId(entity.getId());
			smtAlarmRecever.setCreateTime(dateTime);
        });
		result = smtAlarmReceverService.saveBatch(smtAlarmReceverList);
		return result;
	}

	@Override
	public SmtAlarmTemplate getAlarmTemplate() {
		List<SmtAlarmTemplate> alarmTemplateList = this.list(Wrappers.<SmtAlarmTemplate>query().lambda().orderByDesc(SmtAlarmTemplate::getCreateTime));
		AlarmTemplateDTO alarmTemplateDTO = null;
		if(CollectionUtil.isNotEmpty(alarmTemplateList)) {
			alarmTemplateDTO = new AlarmTemplateDTO();
			SmtAlarmTemplate alarmTemplate = alarmTemplateList.get(0);
			List<SmtAlarmRecever> alarmReceverList = smtAlarmReceverService.list(Wrappers.<SmtAlarmRecever> query().lambda().eq(SmtAlarmRecever::getTemplateId, alarmTemplate.getId()));
			BeanUtils.copyProperties(alarmTemplate, alarmTemplateDTO);
			alarmTemplateDTO.setAlarmReceverList(alarmReceverList);
		}
		return alarmTemplateDTO;
	}
}
