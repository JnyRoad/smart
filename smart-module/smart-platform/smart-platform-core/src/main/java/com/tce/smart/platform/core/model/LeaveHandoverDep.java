package com.tce.smart.platform.core.model;

import java.util.List;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class LeaveHandoverDep extends BaseVO{

    private static final long serialVersionUID = 1L;
    /**
   * 离职申请ID
   */
//    private Integer applicationId;
    /**
     * 责任部门
     */
//    private Integer zrdep;

    /**
     * 责任部门名称
     */
    private String deptName;

    /**
     * 项目列表
     */
    List<LeaveHandoverItemJjrVO> handItem;

}
