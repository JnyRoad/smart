package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 请假详情
 *
 * @author 梁圆
 * @date 2019-04-28 18:19:00
 */
@Data
public class SearchAskLeaveApplicationDetailRespDTO implements Serializable {
	private static final long serialVersionUID = -8455805325697491789L;

	/**
	 * 请假的员工信息
	 */
	private EmployeeAskLeaveRespDTO employee;

	/**
	 * 流程节点
	 */
	private List<FlowRespDTO> flow;

	private String processId;
}
