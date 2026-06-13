package com.tce.smart.platform.core.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 车辆记录统计
 *
 * @author 王艳勇
 * @date 2019-04-13 18:19:30
 */
@Data
public class SnapVehicleCountDTO implements Serializable {

	private static final long serialVersionUID = 1L;

    /**
   * 时间段
   */
    private Integer orderIndex;

    /**
     * 统计数量
     */
    private Integer total;

    /**
   * 事件类型：1-进；2-出；
   */
    private Integer eventType;

}
