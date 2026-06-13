package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.SmtVehicleRespDTO;
import com.tce.smart.platform.core.entity.SmtVehicle;
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
public class GetMyVehicleWrapper extends BaseWrapper<SmtVehicle, SmtVehicleRespDTO> {
	@Override
	protected SmtVehicleRespDTO warp(SmtVehicle smtVehicle) throws IOException {
		SmtVehicleRespDTO smtVehicleRespDTO = new SmtVehicleRespDTO();
		BeanUtil.copyProperties(smtVehicle, smtVehicleRespDTO);
		return smtVehicleRespDTO;
	}
}
