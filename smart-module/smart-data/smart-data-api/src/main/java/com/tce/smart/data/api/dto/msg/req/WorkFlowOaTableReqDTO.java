package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * OA-webservcie创建工作流接口基本字段
 *
 * @author mckaywu
 * @date 2019-06-19 17:32:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkFlowOaTableReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 195491468997023072L;

	/**
	 * 申请人-OA系统独有字段
	 */
	private String SQR;

	/**
	 * Bu公司-OA系统独有字段
	 */
	private String GS;

	/**
	 * 部门-OA系统独有字段
	 */
	private String BM;

	/**
	 * 岗位-OA系统独有字段
	 */
	private String GW;
}
