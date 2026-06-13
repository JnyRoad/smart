package com.tce.smart.platform.core.model;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程审批记录表
 *
 * @author梁圆
 * @date 2019-05-05 11:34:58
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProcessRecordFlow extends BaseVO {
	private static final long serialVersionUID = 1L;


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
