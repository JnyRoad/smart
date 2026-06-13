package com.tce.smart.platform.api.dto.resp.outsrcapply;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/3 11:50
 */
@Data
@Builder
public class SmtOutSrcApplyDetailListDTO extends BaseDTO {

	/**
	 * 部门
	 */
	@ApiModelProperty("部门")
	private String depName;

	/**
	 * 员工工号
	 */
	@ApiModelProperty("员工工号")
	private String badge;
	/**
	 * 员工姓名
	 */
	@ApiModelProperty("员工姓名")
	private String name;
	/**
	 * 手机号
	 */
	@ApiModelProperty("手机号")
	private String phone;
	/**
	 * 身份证号
	 */
	@ApiModelProperty("身份证号")
	private String certno;
	/**
	 * 岗位名称
	 */
	@ApiModelProperty("岗位名称")
	private String jobName;
	/**
	 * 职层名称
	 */
	@ApiModelProperty("职层名称")
	private String jcheName;
	/**
	 * 入职日期
	 */
	@ApiModelProperty("入职日期")
	private String entryDate;
	/**
	 * 派遣渠道
	 */
	@ApiModelProperty("派遣渠道")
	private String dispatchChannel;
}
