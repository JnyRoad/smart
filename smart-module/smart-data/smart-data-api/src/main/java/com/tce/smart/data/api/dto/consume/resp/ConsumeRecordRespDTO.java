package com.tce.smart.data.api.dto.consume.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 刷卡消费记录
 *
 * @author mkwu
 * @date 2019-08-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumeRecordRespDTO extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -1198504020159054757L;

	/**
	 * 卡号
	 */
	private Long CardID;

	/**
	 * 员工姓名
	 */
	private String EmpName;

	/**
	 * 卡机号
	 */
	private Short DevID;

	/**
	 * 消费时间
	 */
	private Date XFPosDay;

	/**
	 * 工号
	 */
	private String EmpNo;

	/**
	 * 消费金额
	 */
	private BigDecimal XFPosMoney;

	/**
	 * 剩余金额
	 */
	private BigDecimal XFCardMoney;
}
