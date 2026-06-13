package com.tce.smart.data.api.dto.ehrview.req;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @description: CInterFaceBenSupplyReqDTO
 * @date: 2020/10/12 15:47
 * @author: wuling
 * @version: 1.0
 */
@Data
public class CInterFaceBenSupplyReqDTO extends BaseVO {

	/**
	 * 工号
	 */
	private String badge;

	/**
	 * 金额
	 */
	private BigDecimal amount;

	/**
	 * 项目
	 */
	private String object;
}
