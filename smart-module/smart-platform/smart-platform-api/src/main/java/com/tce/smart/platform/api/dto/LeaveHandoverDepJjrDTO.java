package com.tce.smart.platform.api.dto;

import com.tce.smart.platform.api.dto.resp.LeaveHandoverItemJjrRespDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LeaveHandoverDepJjrDTO implements Serializable {

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
    List<LeaveHandoverItemJjrRespDTO> handItem;

}
