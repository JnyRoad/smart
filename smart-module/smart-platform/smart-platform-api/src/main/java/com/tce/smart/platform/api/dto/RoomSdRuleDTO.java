package com.tce.smart.platform.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 房间水电规则
 * @Author: wuling
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomSdRuleDTO implements Serializable {

    private static final long serialVersionUID = -32294122564126855L;
    /**
     * 房间标识Id
     */
    private Integer roomId;

    /**
     * 收费项目
     */
    private Integer categoryId;

    /**
     * 配额
     */
    private Double curQty;

    /**
     * 超出单价
     */
    private BigDecimal overFee;
}
