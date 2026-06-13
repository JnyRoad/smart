package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;
import com.tce.smart.data.api.model.LeaveDetailTable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 离职申请Ao
 *
 * @author mingkai.wu
 * @date 2019-05-15 10:33:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendLeaveApplicationReqDTO extends BaseAO {


	/**
	 *
	 */
	private static final long serialVersionUID = 8883070498914261786L;

	/**
	 * 基本数据
	 */
	SendLeaveMainTableReqDTO MainTable;

	/**
	 * 评优记录
	 */
	List<LeaveDetailTable> DetailTable;

}
