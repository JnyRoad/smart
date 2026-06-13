package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtVehicle;
import com.tce.smart.platform.core.model.NotStaffVehicle;
import com.tce.smart.platform.core.vo.NotStaffVehicleVO;
import com.tce.smart.platform.service.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class NotStaffVehicleWrapper extends BaseWrapper<NotStaffVehicleVO, NotStaffVehicle> {
	@Autowired
	private SmtNotStaffService smtNotStaffService;
	@Autowired
	private ImageService imageService;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private SmtVehicleService smtVehicleService;

	@Autowired
	private SmtDeviceAuthorityService smtDeviceAuthorityService;
    @Override
    protected NotStaffVehicle warp(NotStaffVehicleVO vehicle) throws IOException {
	NotStaffVehicle notStaffVehicle = new NotStaffVehicle();
	BeanUtil.copyProperties(vehicle, notStaffVehicle);
	if(ObjectUtil.isNotNull(vehicle.getDriverLicenseId())) {
		notStaffVehicle.setDriverLicenseId(imageService.buildImageUrl(vehicle.getDriverLicenseId()));
	}
	if(ObjectUtil.isNotNull(vehicle.getDrivinglLicenseId())) {
		notStaffVehicle.setDrivinglLicenseId(imageService.buildImageUrl(vehicle.getDrivinglLicenseId()));
	}

		List<Integer> parks = smtVehicleService.splitStringToInteger(vehicle.getParkId());
		List<SmtPark> parkList = smtParkService.list(Wrappers.<SmtPark>query().lambda().in(SmtPark::getId, parks));
		notStaffVehicle.setParkList(parkList);
		String parkName="";
		for (SmtPark smtPark : parkList) {
			parkName+=smtPark.getParkName()+",";
		}
		if(!parkName.equals("")) {
			parkName=parkName.substring(0,parkName.length()-1);
		}
		notStaffVehicle.setParkName(parkName);

		if(Objects.nonNull(vehicle.getAuthorityId())){
			SmtDeviceAuthority smtDeviceAuthority = smtDeviceAuthorityService.getBaseMapper().selectById(vehicle.getAuthorityId());
			notStaffVehicle.setAuthorityName(smtDeviceAuthority.getAuthorityName());
			notStaffVehicle.setAuthorityId(vehicle.getAuthorityId());
			List<SmtDeviceAuthority> authorities = new ArrayList<>();
			authorities.add(smtDeviceAuthority);
			notStaffVehicle.setAuths(authorities);
		}
        return notStaffVehicle;
    }
}
