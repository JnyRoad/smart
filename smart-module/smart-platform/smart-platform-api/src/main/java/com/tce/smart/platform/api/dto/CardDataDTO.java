package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/5/6 .
 * @Modified By:
 */
@Data
public class CardDataDTO implements Serializable{
    private static final long serialVersionUID = -2585380451684239293L;
    /**
     * 设备编号【必选】
     */
    private String deviceCode;

    /**
     * 总数
     */
    private Long totalNum;

    private List<CardDTO> accessList;
}
