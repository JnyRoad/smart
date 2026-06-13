package com.tce.smart.platform.core.vo;

import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.platform.core.entity.SmtOutDormitoryStaff;

import lombok.Data;

@Data
public class SearchOutDormitoryVO extends Model<SmtOutDormitoryStaff> {

	private Integer id;
    /**
   * 员工工号
   */
    private String staffBadge;

    /**
     * 外宿地址
     */
    private String outAddress;

    /**
     * 申请状态  0-审批中  1-已审批
     */
    private Integer status;


    /**
   * 申请时间
   */
    private Date createTime;

    /**
     * 流程编号
     */
    private String processId;

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

    private String name;


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
