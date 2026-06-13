package com.tce.smart.data.api.dto.ehrview.resp;

import java.math.BigDecimal;
import java.util.Date;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class EvwBizAregotRegisterRespDTO  extends BaseVO {
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

	private BigDecimal Amount;

	private String Reason;

	private String FormStateDesc;

	/**
	 * 审批状态 0-草稿，1-撤销，2-审批中，3-退回，4-归档通过，5-申请，6-归档未通过
	 */
	private Integer FORMSTATE;
}
