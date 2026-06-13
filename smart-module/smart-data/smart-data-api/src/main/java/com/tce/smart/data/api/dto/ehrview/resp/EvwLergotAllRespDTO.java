package com.tce.smart.data.api.dto.ehrview.resp;

import java.math.BigDecimal;
import java.util.Date;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;


@Data
public class EvwLergotAllRespDTO extends BaseVO{

	private String BADGE;

	private String NAME;

	private String DEPName;
	/**
	 * 加班日期
	 */
	private Date OTTERM;

	private String OTTYPEName;

	private String OT2STARTTIME;

	private String OT2ENDTIME;

	private String OT4STARTTIME;

	private String OT4ENDTIME;

	private String OT5STARTTIME;

	private String OT5ENDTIME;

	private BigDecimal AMOUNT;

	private String REASON;
}
