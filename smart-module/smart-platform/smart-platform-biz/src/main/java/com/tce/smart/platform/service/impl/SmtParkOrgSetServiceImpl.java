package com.tce.smart.platform.service.impl;

import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.core.ao.ParkOrgSetSaveAO;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.entity.SmtParkLogistics;
import com.tce.smart.platform.core.entity.SmtParkVehicleLevel;
import com.tce.smart.platform.core.vo.ParkOrgSetEditVo;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtParkLogisticsService;
import com.tce.smart.platform.service.SmtParkOrgSetService;
import com.tce.smart.platform.service.SmtParkVehicleLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 园区BU关系表
 *
 * @author mckaywu
 * @date 2019-11-20 10:35:16
 */
@Service
public class SmtParkOrgSetServiceImpl implements SmtParkOrgSetService {

	@Autowired
	private SmtParkBuService smtParkBuService;

	@Autowired
	private SmtParkVehicleLevelService smtParkVehicleLevelService;

	@Autowired
	private SmtParkLogisticsService smtParkLogisticsService;

	@Override
	public ParkOrgSetEditVo viewParkOrg(Integer parkId) {
		ParkOrgSetEditVo parkOrgSetEditVo = null;
		if (Objects.nonNull(parkId)) {
			parkOrgSetEditVo = new ParkOrgSetEditVo();
			parkOrgSetEditVo.setParkId(parkId);

			//园区Bu关系
			List<SmtParkBu> parkBuList = smtParkBuService.listByParkId(parkId);
			if(CollectionUtils.isNotEmpty(parkBuList)) {
				parkOrgSetEditVo.setWorkCompList(parkBuList.stream().map(SmtParkBu::getCompId).collect(Collectors.toList()));
			}
			//园区车辆入园申请职层
			List<SmtParkVehicleLevel> parkVehicleLevelList = smtParkVehicleLevelService.listByParkId(parkId);
			if(CollectionUtils.isNotEmpty(parkVehicleLevelList)) {
				parkOrgSetEditVo.setJcheList(parkVehicleLevelList.stream().map(SmtParkVehicleLevel::getJcheId).sorted().collect(Collectors.toList()));

			}
			//园区物流中心
			SmtParkLogistics parkLogistics = smtParkLogisticsService.getByParkId(parkId);
			if(Objects.nonNull(parkLogistics)) {
				parkOrgSetEditVo.setLogisticId(parkLogistics.getCompanyId());
			}
		}
		return parkOrgSetEditVo;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveParkOrg(ParkOrgSetSaveAO parkOrgSetSaveAO) {
		//修改园区BU关系
		smtParkBuService.saveParkBu(parkOrgSetSaveAO.getParkId(), parkOrgSetSaveAO.getWorkCompList());

		//修改园区关联关系
		smtParkVehicleLevelService.saveParkVehicleLevel(parkOrgSetSaveAO.getParkId(), parkOrgSetSaveAO.getJcheList());

		smtParkLogisticsService.saveParkLogistics(parkOrgSetSaveAO.getParkId(), parkOrgSetSaveAO.getLogisticId());
		return Boolean.TRUE;
	}
}
