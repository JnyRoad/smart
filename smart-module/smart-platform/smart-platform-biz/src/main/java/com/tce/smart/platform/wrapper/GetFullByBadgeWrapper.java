package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.StaffInfoRespDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.vo.StaffInfoVO;
import com.tce.smart.platform.service.SmtParkService;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class GetFullByBadgeWrapper extends BaseWrapper<StaffInfoVO, StaffInfoRespDTO> {

	@Override
	protected StaffInfoRespDTO warp(StaffInfoVO staffInfoVO) throws IOException {
		StaffInfoRespDTO staffInfoRespDTO = new StaffInfoRespDTO();
		BeanUtil.copyProperties(staffInfoVO, staffInfoRespDTO);
		if(Objects.nonNull(staffInfoVO.getSmtStaff())) {
			staffInfoRespDTO.setPqcompany(staffInfoVO.getSmtStaff().getPqcompany());
		}
		return staffInfoRespDTO;
	}
}
