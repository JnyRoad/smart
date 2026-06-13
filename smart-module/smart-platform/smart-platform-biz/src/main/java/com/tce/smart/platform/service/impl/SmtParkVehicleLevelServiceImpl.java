package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtParkVehicleLevel;
import com.tce.smart.platform.core.mapper.SmtParkVehicleLevelMapper;
import com.tce.smart.platform.service.SmtParkVehicleLevelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 园区车辆入园职层表
 *
 * @author mckaywu
 * @date 2019-11-20 10:36:48
 */
@Service
public class SmtParkVehicleLevelServiceImpl extends ServiceImpl<SmtParkVehicleLevelMapper, SmtParkVehicleLevel> implements SmtParkVehicleLevelService {

	@Override
	public List<SmtParkVehicleLevel> listByParkId(Integer parkId) {
		return this.list(Wrappers.<SmtParkVehicleLevel>query().lambda().eq(SmtParkVehicleLevel::getParkId, parkId));
	}

	@Override
	public Boolean removeByParkId(Integer parkId) {
		return this.remove(Wrappers.<SmtParkVehicleLevel>query().lambda().eq(SmtParkVehicleLevel::getParkId, parkId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveParkVehicleLevel(Integer parkId, List<String> jcheList) {
		this.removeByParkId(parkId);
		SmtParkVehicleLevel smtParkVehicleLevel;
		for (String element : jcheList) {
			smtParkVehicleLevel = new SmtParkVehicleLevel();
			smtParkVehicleLevel.setParkId(parkId);
			smtParkVehicleLevel.setJcheId(element);
			smtParkVehicleLevel.setCreateTime(LocalDateTime.now());
			smtParkVehicleLevel.insert();
		}
		return Boolean.TRUE;
	}
}
