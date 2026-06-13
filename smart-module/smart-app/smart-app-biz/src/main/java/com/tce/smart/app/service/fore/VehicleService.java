package com.tce.smart.app.service.fore;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.app.ao.fore.AuthParkAo;
import com.tce.smart.app.vo.fore.AuthDetailVo;
import com.tce.smart.app.vo.fore.ColorTypeVo;
import com.tce.smart.app.vo.fore.VehicleTypeVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.AddVehicleReqDTO;
import com.tce.smart.platform.api.dto.req.ApplyAuthReqDTO;

import java.util.List;
import java.util.Map;

/**
 * 员工车辆管理接口
 * @author qipei
 *
 */
public interface VehicleService {

	IPage<?> getVehicleList(Map<String, Object> params);

	List<?> getAuthPark(AuthParkAo ao);

	AuthDetailVo getAuthDetail(AuthParkAo ao);

	Result addAuthApply(ApplyAuthReqDTO applyAuthDTO);

	Result addVehicle(AddVehicleReqDTO addVehicleDTO);

	List<ColorTypeVo> getColorType();

	List<VehicleTypeVo> getVehicleType();

	Result delete(AuthParkAo ao);
}
