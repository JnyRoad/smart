package com.tce.smart.platform.core.ao;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;

import java.util.List;

/**
 *
 * @ClassName FlowOverAo.java
 * @Author mingkai.wu
 * @Date 2019-04-29 09:05
 * @Description 审核完成推送消息
 */
@Data
public class WorkFlowAO extends BaseAO {

	/**
	 * 流程ID
	 */
	private String requestid;


	/**
	 * 标识  1-归档 2-退回
	 */
	private String status;

	/**
	 * 流程
	 */
	private List<WorkFlowRecordAO> flowRecord;

}
