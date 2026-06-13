package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * LED信息
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/18 .
 * @Modified By:
 */
@Data
public class QueryLedDTO implements Serializable{
	private Integer parkId;

    /**
     * 设备编号【必选】
     */
    private String deviceCode;

    /**
     * 显示场景 0：正常场景；1：有权限过车场景；2：无权限过车场景
     */
    private Integer displayScene;

}
