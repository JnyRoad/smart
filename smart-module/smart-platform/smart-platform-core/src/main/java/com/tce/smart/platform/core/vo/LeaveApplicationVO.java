package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class LeaveApplicationVO extends BaseVO{

    private static final long serialVersionUID = 1L;
    /**
   * 员工号
   */
    private String employeeId;
    /**
     * 员工名称
     */
    private String employeeName;
    /**
     * 公司名称
     */
    private String buName;
    /**
     * 部门名称
     */
    private String depName;
    /**
     * 岗位名称
     */
    private String jobName;
    /**
     * 入职时间 yyyy-MM-dd
     */
    private String entryTime;
    /**
     * 离职时间 yyyy-MM-dd
     */
    private String dismissionDate;
    /**
     * 离职类型
     */
    private String dismissionTypeDesc;

    /**
     * 离职原因
     */
    private String dismissionReasonDesc;
    /**
     * 剩余年假
     */
    private Double restDatCount;
    /**
     * 审批状态
     */
    private Integer approveState;
    /**
     * 审批状态描述
     */
    private String approveStateDesc;
}
