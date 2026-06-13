package com.tce.smart.platform.core.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 撤销外宿补贴详情
 * @author QIPEI
 *
 */
@Data
public class CallowanceDetailVO {



	private String id;

	/**
	 * 员工号
	 */
	private String badge;

	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 补贴类型 10-外食补贴，11-外宿补贴
	 */
	private Integer xType;

	/**
	 * 补贴开始时间
	 */
	private String startTime;

	/**
	 * 补贴结束时间
	 */
	private String endTime;

	/**
	 * 补贴撤销时间
	 */
	private String backDate;

	/**
	 * 是否撤销  0-否  1-是
	 */
	private Integer ifCancel;

	/**
	 * bu
	 */
	private String compId;

	/**
	 * bu
	 */
	private String compName;
	/**
	 * 岗位id
	 */
	private String jobId;

	/**
	 * 岗位名称
	 */
	private String jobName;

	/**
	 * 部门id
	 */
	private String depId;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 申请id
	 */
	private Integer appid;

	/**
	 * 薪资区域
	 */
	private Integer pzid;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 流程号
	 */
	private String processId;

	/**
	 * 补贴金额
	 */
	private String amount;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 入职时间
	 */
	private String joinDate;

	/**
	 * 员工状态
	 */
	private String staffStatus;


	   /**
     * 补贴类型
     */
    private String allowanceType;


	/**
	 * 审批流程
	 */
    private List<FlowVO> flow;
}
