package com.tce.smart.platform.service.admittance;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.admittance.AdmittanceFellowReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceFellowRespDTO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;

import java.util.List;

/**
 * 入厂申请预约随行人员表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:13
 */
public interface SmtAdmittanceFellowService extends IService<SmtAdmittanceFellow> {

	/**
	 * 保存随行人员
	 * @param fellowReq
	 * @param applyId
	 * @return
	 */
	Boolean saveFellow(List<AdmittanceFellowReqDTO> fellowReq, Long applyId);

	/**
	 * 根据预约id获得随行人员
	 * @param applyId
	 * @return
	 */
	List<SmtAdmittanceFellow> getByApplyId(Long applyId);

	/**
	 * 判断人员id是否存在于随行人员中
	 * @param personId
	 * @return
	 */
	Boolean isExistFellow(Long personId);

	/**
	 * 根据预约id获得随行人员
	 * @param applyId
	 * @return
	 */
	List<AdmittanceFellowRespDTO> getRespByApplyId(Long applyId);

}
