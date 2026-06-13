package com.tce.smart.data.api.dto.ehrview.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 11:38
 */
@Data
public class EvwLdxRegLeaveAllRespDTO extends BaseVO {
	private String BADGE;

	private String NAME;

	private String TWIDName;

	private String DEPName;

	private Date BEGINTIME;

	private String REMARK;
}
