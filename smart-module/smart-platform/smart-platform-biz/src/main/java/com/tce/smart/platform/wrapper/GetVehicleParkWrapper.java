package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.VehicleApplyRespDTO;
import com.tce.smart.platform.core.vo.VehicleApplyVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class GetVehicleParkWrapper extends BaseWrapper<VehicleApplyVO, VehicleApplyRespDTO> {
	@Override
	protected VehicleApplyRespDTO warp(VehicleApplyVO vehicleApplyVO) throws IOException {
		VehicleApplyRespDTO vehicleApplyRespDTO = new VehicleApplyRespDTO();
		BeanUtil.copyProperties(vehicleApplyVO, vehicleApplyRespDTO);
		return vehicleApplyRespDTO;
	}
}
