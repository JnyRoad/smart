package com.tce.smart.platform.api.dto.resp.commonconfig;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fushiping
 * @date 2021/8/13 0013 17:35
 **/
@Data
public class ConfigSettlementLogDeleteDTO implements Serializable {

	private static final long serialVersionUID = -1L;

	/**
	 * 日志保留天数
	 */
	private Integer deleteDays;

}
