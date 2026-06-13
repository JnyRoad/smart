package com.tce.smart.data.api.dto.msg.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Oa审批EHR表字段基本字段
 *
 * @author mckaywu
 * @date 2019-06-19 17:32:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MainBaseTableReqDTO<T> extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -7335934894886898966L;

	/**
	 * 工号
	 */
	@JsonProperty("Badge")
	private String Badge;

	/**
	 * 姓名
	 */
	@JsonProperty("Name")
	private String Name;

	/**
	 * Bu公司
	 */
	@JsonProperty("Compid")
	private String Compid;

	/**
	 * 部门
	 */
	@JsonProperty("Depid")
	private String Depid;

	/**
	 * 岗位
	 */
	@JsonProperty("Jobid")
	private String Jobid;

}
