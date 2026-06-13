package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程节点
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Data
public class FlowRespDTO implements Serializable {
private static final long serialVersionUID = 1L;


    private String nodeName;
    private Integer nodeState;
    private String processDesc;
    private Date processDate;
    private String remark;
    /**
     * 审批人
     */
    private String createUser;
}
