package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class LeaveHandoverDepItemVO extends BaseVO{

    private static final long serialVersionUID = 1L;

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
