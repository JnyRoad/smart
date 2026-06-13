package com.tce.smart.data.api.dto.ehrview.resp;

import java.util.Date;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class EvwBizLcardlostRespDTO  extends BaseVO{
	private String BADGE;

	private String NAME;

	private String DEPName;
	/**
	 * 补卡时间
	 */
	private Date KQSTARTDATE;

	private String REASONDes;

	private String SHIFT;

	private Integer FormState;

	private String FormStateDesc;
}
