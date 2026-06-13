package com.tce.smart.platform.api.dto.resp;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LeaveHandoverItemJjrRespDTO extends LeaveHandoverDepItemRespDTO{

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
