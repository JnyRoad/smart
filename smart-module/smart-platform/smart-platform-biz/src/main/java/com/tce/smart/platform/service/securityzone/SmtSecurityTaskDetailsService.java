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

	/**
	 * 将申请单中所有尚未成功的人员明细重绑到最新批次，并重置为待领取状态。
	 */
	int rebindDispatchBatch(Long applyId, Long dispatchBatchId);

	/** 统计批次内去重后的受理人员数量。 */
	int countDispatchPeople(Long applyId, Long dispatchBatchId);

	/**
	 * 按明细ID、待领取状态和批次号原子领取任务，旧批次必须领取失败。
	 */
	boolean claimDispatchDetail(Long detailId, Long dispatchBatchId);

	/** 读取本轮异步 worker 的有限待处理候选。 */
	List<SmtSecurityTaskDetails> listPendingDispatchDetails(int limit);

	/** 在已锁定申请单的事务内领取并创建当前批次真实 ISC 任务。 */
	int dispatchCurrentBatchDetails(Long applyId, Long dispatchBatchId, String applyBadge, List<Long> staffIds);
}
