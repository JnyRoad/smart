package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class LeaveHandoverItemVO extends BaseVO{

    private static final long serialVersionUID = 1L;
    /**
   * 离职申请ID
   */
    private Integer applicationId;
    /**
     * 责任部门
     */
    private Integer zrdep;

    /**
     * 责任部门名称
     */
    private String zrdepName;

    /**
     * 交接事项ID
     */
    private Integer jjItemId;
    /**
     * 交接事项
     */
    private String jjItem;
}
