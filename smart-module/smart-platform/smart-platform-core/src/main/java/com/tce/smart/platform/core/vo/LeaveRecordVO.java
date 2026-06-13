package com.tce.smart.platform.core.vo;

import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class LeaveRecordVO extends Model<LeaveRecordVO> {
    private static final long serialVersionUID = 1L;

    /**
     * 审批记录Id
     */
    private Integer id;

    /**
     * 流程编号
     */
    private String processId;

    /**
     * 0:正常；1：异常；
     */
    private Integer leaveStatus;

    /**
     * 员工名称
     */
    private String name;
    /**
     * 标题
     */
    private String title;
    /**
     * 节点名称
     */
    private String nodeName;
    /**
     * 节点状态
     */
    private String nodeState;
    /**
     * 离职时间
     */
    private Date leaveTime;
    /**
     * 记录时间
     */
    private Date recordDate;

    /**
     * 0:审核中；1：通过；2：未通过
     */
    private Integer approveStatus;

    private Date createTime;
}
