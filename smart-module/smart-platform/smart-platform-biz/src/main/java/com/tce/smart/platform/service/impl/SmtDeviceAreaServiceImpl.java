package com.tce.smart.platform.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.dto.AddSnapVehicleDTO;
import com.tce.smart.platform.core.entity.SmtAlarmRecord;
import com.tce.smart.platform.core.entity.SmtArea;
import com.tce.smart.platform.core.entity.SmtDeviceArea;
import com.tce.smart.platform.core.mapper.SmtDeviceAreaMapper;
import com.tce.smart.platform.service.SmtDeviceAreaService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备区域关联
 *
 * @author 王艳勇
 * @date 2019-04-15 15:12:58
 */
@Service
@AllArgsConstructor
public class SmtDeviceAreaServiceImpl extends ServiceImpl<SmtDeviceAreaMapper, SmtDeviceArea> implements SmtDeviceAreaService {

	private final SmtDeviceAreaMapper smtDeviceAreaMapper;
	/**
	 * 车辆抓拍记录区域信息补充
	 *
	 * @param entity 抓拍车辆信息
	 */
	@Override
	public void areaHandle(AddSnapVehicleDTO entity) {
		SmtArea area = smtDeviceAreaMapper.queryByDeviceId(entity.getDeviceId());
		if(ObjectUtil.isNotNull(area)) {
			entity.setAreaId(area.getId());
			entity.setAreaName(area.getAreaName());
			entity.setParkId(area.getParkId());
		}
	}
	/**
	 * 警报记录区域信息补充
	 *
	 * @param entity 抓拍车辆信息
	 */
	@Override
	public Integer areaHandle(SmtAlarmRecord entity) {
		SmtArea area = smtDeviceAreaMapper.queryByDeviceId(entity.getDeviceId());
		entity.setAreaId(area.getId());
		entity.setAreaName(area.getAreaName());
		entity.setParkId(area.getParkId());
		return area.getParkId();
	}

	/**
	 * 保存设备区域信息
	 *
	 * @param entity 抓拍车辆信息
	 * @return 结果
	 */
	@Override
	public Boolean saveArea(SmtDeviceArea entity) {
		this.remove(Wrappers.<SmtDeviceArea>query().lambda().eq(SmtDeviceArea::getDeviceId, entity.getDeviceId()));
		//绑定区域信息
		SmtDeviceArea smtDeviceArea = new SmtDeviceArea();
		smtDeviceArea.setAreaId(entity.getAreaId());
		smtDeviceArea.setDeviceId(entity.getDeviceId());
		smtDeviceArea.setCreateTime(LocalDateTime.now());
		return this.save(smtDeviceArea);
	}

}
