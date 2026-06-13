package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新车位信息
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/18 .
 * @Modified By:
 */
@Data
public class ParkingLotUpdateDTO implements Serializable {
    private static final long serialVersionUID = -1652934850948326249L;
    /**
     * 车库编号【必选】
     */
    private String garageCode;

    /**
     * 车位总数【必选】
     */
    private Integer totalParkingSpace;

    /**
     * 剩余车位数【必选】
     */
    private Integer  remainParkingSpace;

}
