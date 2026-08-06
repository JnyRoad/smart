package com.tce.smart.app.vo.fore;

import lombok.Data;

/**
 * 离职申请
 * @author Administrator
 *
 */
@Data
public class LeaveApplicationVO {

    private String employeeId;
    private Integer dimissionReason;
    private Integer dimissionType;
    private String dimissionDate;
    private Double yearHoliday;
    private Integer dimissionApplyType;

    /** 多园区员工选择的离职园区；Platform 会以当前认证会话的园区范围重新校验。 */
    private Integer parkId;
}
