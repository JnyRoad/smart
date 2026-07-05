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
	 * CAS 抢占 OA 终态：回调 handler 与对账任务共用同一入口，只有把 oa_status 从
	 * PENDING(0) 抢先置为终态（AGREE/REFUSE）的一方才允许触发后续下发，避免并发重复下发（spec §3.1.1）。
	 * @param applyId 申请单ID
	 * @param finalOaStatus 终态（ApproveListStateEnum.AGREE/REFUSE）
	 * @return 是否抢到（true=本次调用方是唯一推进终态的一方）
	 */
	boolean claimOaFinalStatus(Long applyId, Integer finalOaStatus);

	/**
	 * 触发设备下发：委托明细服务下发，仅当下发过程未抛异常才把主表 device_status
	 * 由 WAIT(0) CAS 置为 ALRAEDY(4)；下发异常时保持主表现值，由对账任务重试（修 D4，spec §3.1.3）。
	 * @param authApply 申请单（需已带最新 oaStatus）
	 * @return 是否成功触发并推进主表下发状态
	 */
	boolean triggerDownDevice(SmtSecurityAuthApply authApply);

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

	/**
	 * 保密门禁 OA 对账任务（PR2 核心补偿逻辑）：
	 * 场景1——回调丢失：扫描 oa_status=PENDING 且已有 processId 的申请单，主动向 OA 查询终态并补齐；
	 * 场景2——审批已过但下发未执行（含 D4 中间态与场景1下发失败的重试）：直接重新触发下发。
	 * 详见 spec §3.1.3/§3.1.4。
	 */
	void updateOaStatusTask();
}
