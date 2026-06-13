package com.tce.smart.data.api.dto.ehrview.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-20 19:05
 */
@Data
public class EvwBizLregleaveRespDTO extends BaseVO {
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
}
