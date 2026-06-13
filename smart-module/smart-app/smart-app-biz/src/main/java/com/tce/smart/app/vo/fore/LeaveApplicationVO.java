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
}
