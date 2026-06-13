package com.tce.smart.data.api.dto.ehrview.resp;

import java.math.BigDecimal;
import java.util.Date;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 * 请假历史表
 * @author 齐佩
 *
 */
@Data
public class EvwLregLeaveAllRespDTO  extends BaseVO{
	private String BADGE;

	private String NAME;

	private Integer TWID;

	private Date BeginTime;

	private Date EndTime;

	private Date BeginDate;

	private Date EndDate;

	private BigDecimal AMOUNT;

	private Integer Unit;

	private Date regdate;

}
