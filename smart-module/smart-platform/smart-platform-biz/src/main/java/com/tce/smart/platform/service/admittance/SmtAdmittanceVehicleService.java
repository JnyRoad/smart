package com.tce.smart.platform.service.admittance;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.admittance.AdmittanceVehicleReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceVehicleRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceVehicle;

import java.util.List;

/**
 * 入厂申请预约车辆表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:05
 */
public interface SmtAdmittanceVehicleService extends IService<SmtAdmittanceVehicle> {

	/**
	 * 添加车辆信息
	 * @param reqDTO
	 * @return
	 */
	Boolean saveVehicle(List<AdmittanceVehicleReqDTO> reqDTO, Long applyId);

	/**
	 * 根据预约id获得车辆信息
	 * @param applyId
	 * @return
	 */
	List<SmtAdmittanceVehicle> getByApplyId(Long applyId);

	/**
	 * 根据预约id获得车辆信息
	 * @param applyId
	 * @return
	 */
	List<AdmittanceVehicleRespDTO> getRespByApplyId(Long applyId);

}
