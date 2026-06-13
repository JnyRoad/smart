package com.tce.smart.data.api.dto.ehrview.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-14 11:14
 */
@Data
public class EvwHortationsAllRespDTO extends BaseVO {
	private Integer eid;
	private String badge;
	private String name;
	private Integer CompID;
	private Integer depOne;
	private Integer depTwo;
	private Integer DepID;
	private String JobID;
	private String StatusDesc;
	private String JchenDesc;
	private Integer isout;
	private Date begindate;
	private String typeDesc;
	private String kindDesc;
	private BigDecimal sumMoney;
	private Date paymonth;
	private BigDecimal Fraction;
	private String reason;
	private String description;
	private String remark;
}
