package com.tce.smart.platform.core.vo;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 员工外宿申请详情
 * @author QIPEI
 *
 */
@Data
public class OutDormitoryDetailVO {


	 /**
	   * 员工工号
	   */
	    private String staffBadge;

	    private String name;

	    private String processId;

	    private Date createTime;

	    /**
	     * 外宿地址
	     */
	    private String outAddress;



	    /**
	     * 补贴开始时间
	     */
	    private String startTime;

	    /**
	     * 补贴结束时间
	     */
	    private String endTime;

	    /**
	     * 补贴类型
	     */
	    private String allowanceType;

	    /**
	     * 补贴金额
	     */
	    private String amount;
	    /**
	     * 计算规则
	     */
	    private String  computaionRule;

	    /**
	     * 补贴说明
	     */
	    private String  explain;

	    /**
	     * 备注
	     */
	    private String remark;


		/**
		 * 审批流程
		 */
	    private List<FlowVO> flow;


		/**
		 * bu名称
		 */
		private String compName;

		/**
		 * 部门名称
		 */
		private String depName;

		/**
		 * 岗位名称
		 */
		private String jobName;

}
