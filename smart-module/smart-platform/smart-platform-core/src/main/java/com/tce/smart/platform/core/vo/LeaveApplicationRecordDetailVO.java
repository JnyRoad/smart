package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import com.tce.smart.platform.core.entity.SmtLeaveHandover;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 离职申请表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class LeaveApplicationRecordDetailVO extends BaseVO {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    private Integer id;

    /**
   * 员工号
   */
    private String badge;

    /**
     * 员工名称
     */
    private String name;

	/**
	 * 离职原因
	 */
	private String leaveTypeDesc;

	/**
	 * 离职原因
	 */
	private String leaveReasonDesc;

    /**
     * 离职时间
     */
    private Date leaveTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 流程编号
     */
    private String processId;

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
	 * 入职时间
	 */
	private Date joinTime;

	/**
	 * 交接项
	 */
	private List<SmtLeaveHandover> items;

}
