package com.tce.smart.platform.core.model;

import java.util.Date;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class LeaveRecordList extends BaseVO {
    private static final long serialVersionUID = 1L;

    /**
     * 标题
     */
    private Integer recordId;
    /**
     * 流程Id
     */
    private String processId;
    /**
     * 记录标题
     */
    private String recordTitle;
    /**
     * 备注
     */
    private String recordDesc;
    /**
     * 离职时间 yyyy-MM-dd
     */
    private String dismissionDate;
    /**
     * 记录时间 yyyy-MM-dd
     */
    private String recordDate;

    /**
     * 0:审核中；1：通过；2：未通过
     */
    private Integer approveStatus;
}
