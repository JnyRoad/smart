package com.tce.smart.platform.core.model;

import com.tce.smart.platform.core.vo.LeaveHandoverDepItemVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LeaveHandoverItemJjrVO extends LeaveHandoverDepItemVO{

	private static final long serialVersionUID = 1L;

	/**
	 * 交接人工号
	 */
	private String receiverId;
	/**
	 * 交接人姓名
	 */
	private String receiverName;

}
