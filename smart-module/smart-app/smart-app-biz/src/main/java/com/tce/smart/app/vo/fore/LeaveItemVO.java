package com.tce.smart.app.vo.fore;

import lombok.Data;

/**
 * 交接项
 * @author Administrator
 *
 */
@Data
public class LeaveItemVO {

    private Integer itemId;

    private String itemName;

    private Double itemAmt;

    private String itemDesc;
}
