package com.tce.smart.platform.api.dto.resp.manage;

import com.tce.smart.common.core.dto.BaseDTO;
import com.tce.smart.common.core.vo.BaseVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 考勤签单详情
 * @author fushiping
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AttendanceSignDetailRespDTO extends BaseDTO {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Integer id;

	/**
	 * 员工号
	 */
	@ApiModelProperty("员工工号")
	private String badge;

	/**
	 * 员工姓名
	 */
	@ApiModelProperty("员工姓名")
	private String name;

	/**
	 * 部门名称
	 */
	@ApiModelProperty("部门名称")
	private String depName;

	/**
	 * bu
	 */
	@ApiModelProperty("bu名")
	private String compName;

	/**
	 * 考勤月份
	 */
	@ApiModelProperty("考勤月份")
	private String checkDate;

	/**
	 * 签名照
	 */
	@ApiModelProperty("签名照")
	private String signImg;

	/**
	 * 签单时间
	 */
	@ApiModelProperty("签单时间")
	private String signDate;

	/**
	 * 所属园区
	 */
	@ApiModelProperty("所属园区")
	private String parkName;

	@ApiModelProperty("签收状态code")
	private Integer signStatus;

	@ApiModelProperty("签收状态desc")
	private String signStatusDesc;

	/**
	 * 是否有异议
	 */
	@ApiModelProperty("是否有异议")
	private Integer isObjection;

	/**
	 * 异议
	 */
	@ApiModelProperty("异议")
	private String objection;

	@ApiModelProperty("异议状态")
	private String isObjectionDesc;

	@ApiModelProperty("考勤汇总")
	private AvaGetskyPayRespDTO avaGetskyPayYSHRDTO;

	private String noticeStatus;

}
