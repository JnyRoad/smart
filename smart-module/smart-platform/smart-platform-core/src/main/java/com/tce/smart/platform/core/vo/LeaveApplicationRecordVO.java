package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 离职申请表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LeaveApplicationRecordVO extends BaseVO {
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
