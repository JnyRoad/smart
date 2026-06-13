package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 流程审批记录表
 *
 * @author梁圆
 * @date 2019-05-05 11:34:58
 */
@Data
public class ProcessRecordFlowDTO implements Serializable {
	private static final long serialVersionUID = 2302729111628949456L;


	/**
	 * 审批时间 yyyy-MM-dd
	 */
	private String processDate;
	/**
	 * 节点名称
	 */
	private String nodeName;
	/**
	 * 节点状态
	 */
	private String nodeState;
	/**
     * 审批备注
     */
    private String processDesc;

    /**
     * 审批备注
     */
    private String remark;

	/**
	 * 姓名
	 */
	private String staffName;
}
