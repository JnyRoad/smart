package com.tce.smart.platform.core.ao;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: LeaveApplyAO
 * @Author jinbo
 * @Date 2019/5/16
 */
@Data
public class LeaveApplyAO extends BaseAO {
    /**
	 *
	 */
	private static final long serialVersionUID = -8133613656662421636L;
	private String badge;
    private Integer leaveType;
    private Integer leaveReason;
    private Integer leaitent;
    private Date leaveTime;
    private LocalDateTime applyTime;
    private Double yearHoliday;
    private Integer id;
}
