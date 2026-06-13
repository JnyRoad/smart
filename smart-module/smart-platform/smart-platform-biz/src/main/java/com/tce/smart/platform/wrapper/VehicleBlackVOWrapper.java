package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtVehicleBlack;
import com.tce.smart.platform.core.vo.VehicleBlackVO;
import com.tce.smart.platform.service.SmtParkService;
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
public class VehicleBlackVOWrapper extends BaseWrapper<SmtVehicleBlack, VehicleBlackVO> {
	private final SmtParkService smtParkService;
    @Override
    protected VehicleBlackVO warp(SmtVehicleBlack smtVehicleBlack) throws IOException {
		VehicleBlackVO vehicleBlackVO = new VehicleBlackVO();
		BeanUtil.copyProperties(smtVehicleBlack,vehicleBlackVO);
		SmtPark smtPark = smtParkService.getById(smtVehicleBlack.getParkId());
		if(ObjectUtil.isNotNull(smtPark)){
			vehicleBlackVO.setParkName(smtPark.getParkName());
		}
        return vehicleBlackVO;
    }
}
