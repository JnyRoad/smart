package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtVehicleApply;
import com.tce.smart.platform.core.entity.SmtXcVehicle;
import com.tce.smart.platform.core.model.VehicleList;
import com.tce.smart.platform.core.model.XcVehicleList;
import com.tce.smart.platform.core.vo.VehicleVO;
import com.tce.smart.platform.core.vo.XcVehicleVO;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtVehicleApplyService;
import com.tce.smart.tool.constant.VehicleApplyConstants;
import com.tce.smart.tool.enums.VehicleTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 */
@Component
@AllArgsConstructor
public class XcVehicleListWrapper extends BaseWrapper<SmtXcVehicle, XcVehicleList> {

	@Autowired
	private SmtParkService smtParkService;

    @Override
    protected XcVehicleList warp(SmtXcVehicle vehicle) throws IOException {
		XcVehicleList vehicleList = new XcVehicleList();
	BeanUtil.copyProperties(vehicle, vehicleList);
		SmtPark smtPark = smtParkService.getById(vehicle.getParkId());
		if(Objects.nonNull(smtPark)){
			vehicleList.setParkName(smtPark.getParkName());
		}
		return vehicleList;
    }
}
