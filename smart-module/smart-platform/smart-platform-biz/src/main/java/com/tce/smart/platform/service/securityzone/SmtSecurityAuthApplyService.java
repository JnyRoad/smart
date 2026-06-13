package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthApplyPageQueryReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthApplyReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthApplyPageRespDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:31
 */
public interface SmtSecurityAuthApplyService extends IService<SmtSecurityAuthApply> {

	/**
	 * 权限申请
	 * @param reqDTO
	 * @return
	 */
	Boolean saveApply(SecurityAuthApplyReqDTO reqDTO);

	/**
	 * 根据OA流程号获得申请单
	 * @param processId
	 * @return
	 */
	SmtSecurityAuthApply getByProcessId(String processId);

	/**
	 * 获得分页列表
	 * @param page
	 * @param query
	 * @return
	 */
	IPage<SecurityAuthApplyPageRespDTO> getPage(Page page, SecurityAuthApplyPageQueryReqDTO query);

	/**
	 * OA状态更改
	 * @param authApply
	 * @return
	 */
	void updateStatus(SmtSecurityAuthApply authApply);


	/**
	 * 手动下发
	 * @param applyId
	 * @return
	 */
	Boolean downDevice(Long applyId);

	/**
	 * 发送权限下发提示短信
	 */
	void sendMessage();
}
