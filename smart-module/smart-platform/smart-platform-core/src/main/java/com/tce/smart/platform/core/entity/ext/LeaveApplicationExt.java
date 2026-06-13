package com.tce.smart.platform.core.entity.ext;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 离职申请
 * @author Lenovo
 *
 */
@Data
public class LeaveApplicationExt implements Serializable {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -4374891154062922225L;

	/**
	 * 员工号
	 */
	@NotBlank(message = "员工号不能为空")
	private String badge;
    /**
     * 离职类型
     */
    @NotNull(message = "离职类型不能为空")
    private Integer leaveType;
    /**
     * 离职原因
     */
    @NotNull(message = "离职原因不能为空")
    private Integer leaveReason;

    /**
     * 离职时间
     */
    @NotNull(message = "离职时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date leaveTime;

    /**
     * 剩余年假
     */
    private Double yearHoliday;

    /**
     *  0:正常离职：1：异常离职
     */
    private Integer leaveStatus;

    /**
     * 申请人
     */
    private String applyBadge;
}
