package com.tce.smart.app.ao.fore;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客审核
 *
 * @author ly
 * @date 2019-05-13 15:13:11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApproveVisitorAo extends BaseAO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 2872881423154160334L;

	/**
	 * 访客的id
	 */
	private String visitId;


	/**
	 * 审核的状态 0:已通过,1:已驳回
	 */
	private Integer approveVisitState;


	private String startTime;

	private String endTime;
	/**
	 * 拒绝原因
	 */
	private String refuseDes;
}
