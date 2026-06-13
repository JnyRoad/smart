package com.tce.smart.data.api.dto.ehrview.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 10:56
 */
@Data
public class EvwBizCallowanceFoodRespDTO extends BaseVO {
	private String BADGE;

	private String NAME;

	private String DEPName;

	private String XTYPEName;

	private Date BEGINDATE;

	private Date APPENDDATE;

	private BigDecimal AMOUNT;

	private String Explain;

	private String FormStateDesc;
}
