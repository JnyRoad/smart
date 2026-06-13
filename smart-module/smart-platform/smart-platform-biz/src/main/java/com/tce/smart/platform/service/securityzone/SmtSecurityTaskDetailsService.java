package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityApplyPersonReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.UpdateFaceImgReqDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityTaskDetails;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:17
 */
public interface SmtSecurityTaskDetailsService extends IService<SmtSecurityTaskDetails> {

	/**
	 * 下发任务初始化
	 * @param personReq
	 * @param applyId
	 * @return
	 */
	Boolean initTask(List<SecurityApplyPersonReqDTO> personReq, Long applyId);

	/**
	 * 同步设备下发状态
	 * @param taskId
	 * @return
	 */
	Boolean syncTaskStatus(Long taskId);

	/**
	 * 获得下发状态数量
	 * @param applyId
	 * @param status
	 * @return
	 */
	Integer getCount(Long applyId, Integer status);

	/**
	 * 获得列表
	 * @param taskId
	 * @return
	 */
	List<SmtSecurityTaskDetails> getList(Long taskId);

	/**
	 * 获得列表
	 * @param req
	 * @return
	 */
	Boolean reloadImg(UpdateFaceImgReqDTO req);

	/**
	 * 设备下发
	 * @param applyId
	 * @param badge
	 * @return
	 */
	Boolean downDevice(Long applyId, String badge);
}
