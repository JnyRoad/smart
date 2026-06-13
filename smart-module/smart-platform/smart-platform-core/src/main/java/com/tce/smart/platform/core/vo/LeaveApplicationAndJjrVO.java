package com.tce.smart.platform.core.vo;

import com.tce.smart.platform.core.entity.SmtLeaveApplication;

import lombok.Data;

@Data
public class LeaveApplicationAndJjrVO extends SmtLeaveApplication{

    private static final long serialVersionUID = 1L;
    /**
     * 审批状态描述
     */
    private String Jjr;
}
