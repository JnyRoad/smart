package com.tce.smart.platform.api.dto.resp.commonconfig;

import lombok.Data;

import java.io.Serializable;

/**
 * @author fushiping
 * @date 2021/8/13 0013 17:35
 **/
@Data
public class ConfigSettlementLastDayDTO implements Serializable {

	private static final long serialVersionUID = -1L;

	/**
	 * 离职水电结算是否计算最后一天 0 否 1 是
	 */
	private Integer isSettlementLast;

	/**
	 * 模板id
	 */
	private Long tempId;

}
