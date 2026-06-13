package com.tce.smart.platform.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 离职申请表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
@Data
public class SmtLeaveApplicationDTO implements Serializable {
private static final long serialVersionUID = -2500889131861232153L;

    /**
   * 主键
   */
    private Integer id;
    /**
   * 园区ID
   */
    private Integer parkId;
    /**
     * 人事区域
     */
    private Integer ezid;

    private Integer eid;
    /**
   * 员工号
   */
    @NotBlank(message = "员工号不能为空")
    private String badge;
    /**
     * 员工名称
     */
    private String name;
    /**
     * 公司ID
     */
    private Integer compId;
    /**
     * 公司名称
     */
    private String compName;
    /**
     * 部门ID
     */
    private Integer depId;
    /**
     * 部门名称
     */
    private String depName;
    /**
     * 职层ID
     */
    private Integer jchenId;
    /**
     * 职层名称
     */
    private String jchenName;
    /**
     * 岗位ID
     */
    private String jobId;
    /**
     * 岗位名称
     */
    private String jobName;
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
     * 剩余年假
     */
    private Double yearHoliday;
    /**
     * 入职时间
     */
    private Date joinTime;
    /**
     * 离职时间
     */
    @NotNull(message = "离职时间不能为空")
    private Date leaveTime;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 流程编号
     */
    private String processId;

    /**
     *  0:正常离职：1：异常离职
     */
    private Integer leaveStatus;

    /**
     * 0：审批中；
	 * 1：通过；
     * 2：拒绝；
	 * 3：交接开始；；
	 * 4：交接完成
     */
    private Integer approveStatus;

    private String applyBadge;
}
