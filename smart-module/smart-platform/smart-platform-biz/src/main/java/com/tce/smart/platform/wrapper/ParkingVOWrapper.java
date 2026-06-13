package com.tce.smart.platform.wrapper;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtParking;
import com.tce.smart.platform.core.model.ParkingVO;
import com.tce.smart.platform.service.SmtParkService;

import lombok.AllArgsConstructor;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class ParkingVOWrapper extends BaseWrapper<SmtParking, ParkingVO> {
	 private final SmtParkService parkService;
    @Override
    protected ParkingVO warp(SmtParking parking) throws IOException {
	ParkingVO parkingVO = new ParkingVO();
	parkingVO.setId(parking.getId());
	parkingVO.setName(parking.getName());
	parkingVO.setTotalCount(parking.getTotalCount());
	parkingVO.setParkId(parking.getParkId());
	parkingVO.setParkName(parkService.getById(parking.getParkId()).getParkName());
        return parkingVO;
    }
}
