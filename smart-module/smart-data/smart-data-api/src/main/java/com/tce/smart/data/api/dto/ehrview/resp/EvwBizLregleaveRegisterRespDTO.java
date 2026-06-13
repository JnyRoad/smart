package com.tce.smart.data.api.dto.ehrview.resp;


import java.math.BigDecimal;
import java.util.Date;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

/**
 * 请假信息
 * @author 齐佩
 *
 */
@Data
public class EvwBizLregleaveRegisterRespDTO  extends BaseVO{

	private String BADGE;

	private String NAME;

	private String TWIDName;

	private String DEPName;
	/**
	 * 请假开始时间
	 */
	private Date BeginTime;

	/**
	 * 请假结束时间
	 */
	private Date EndTime;

	private BigDecimal Amount;

	private String UnitName;

	private String DayoffReason;

	private String FormStateDesc;

}
