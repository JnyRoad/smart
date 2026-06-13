package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

@Data
public class LeaveHandoverDepItemRespDTO implements Serializable {

    private static final long serialVersionUID = 7067563806253847977L;

    private Integer itemId;
    /**
     * 交接事项
     */
    private String itemName;

    /**
     * 交接确认 0：否；1：是；
     */
    private Integer itemState;

    /**
     * 金额
     */
    private Double itemAmt;
    /**
     * 说明
     */
    private String ItemDesc;

}
