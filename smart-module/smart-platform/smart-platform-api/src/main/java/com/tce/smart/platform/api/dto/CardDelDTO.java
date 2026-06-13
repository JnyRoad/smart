package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 人员卡片删除信息
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/18 .
 * @Modified By:
 */
@Data
public class CardDelDTO implements Serializable {
    private static final long serialVersionUID = -8349267975666492115L;
    /**
     * 设备编号【必选】
     */
    private String deviceCode;

    /**
     * 卡片编号【必选,纯数字且小于32位】
     */
    private String cardNo;

	/**
	 * 主键
	 */
	private Integer reqId;

	/**
	 * 序号
	 */
	private String serialNo;
}
