package com.tce.smart.data.api.dto.msg.req;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 请假申请EHR表字段
 *
 * @author mckaywu
 * @date 2019-06-19 17:32:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendVacateReqDTO extends MainBaseTableReqDTO<SendVacateReqDTO> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -8480106527640997033L;

	/**
	 * 职层
	 */
	@JsonProperty("Jchenid")
	private String Jchenid;

	/**
	 * 请假类型
	 */
	@JsonProperty("TWID")
	private String TWID;

	/**
	 * 开始日期
	 */
	@JsonProperty("Begintime")
	private String Begintime;

	/**
	 * 结束日期
	 */
	@JsonProperty("Endtime")
	private String Endtime;

	/**
	 * 开始时间
	 */
	@JsonProperty("Starttime")
	private String Starttime;

	/**
	 * 结束时间
	 */
	@JsonProperty("Enddate")
	private String Enddate;

	/**
	 * 享有年假
	 */
	@JsonProperty("Cyear")
	private String Cyear;

	/**
	 * 请假长度
	 */
	@JsonProperty("Amount")
	private String Amount;

	/**
	 * 单位
	 */
	@JsonProperty("Unit")
	private String Unit;

	/**
	 * 附件
	 */
	@JsonProperty("FJ")
	private String FJ;

	/**
	 * 请假原因
	 */
	@JsonProperty("Dayoffreason")
	private String Dayoffreason;

	/**
	 * Ezid
	 */
	@JsonProperty("Ezid")
	private String Ezid;

	/**
	 * 班次
	 */
	@JsonProperty("Shift")
	private String Shift;
	/**
	 * 2入
	 */
	@JsonProperty("In2")
	private String In2;

	/**
	 * 2出
	 */
	@JsonProperty("Out2")
	private String Out2;

	/**
	 * 4入
	 */
	@JsonProperty("In4")
	private String In4;

	/**
	 * 4出
	 */
	@JsonProperty("Out4")
	private String Out4;

	/**
	 * 5入
	 */
	@JsonProperty("In5")
	private String In5;

	/**
	 * 5出
	 */
	@JsonProperty("Out5")
	private String Out5;

	/**
	 * 备注
	 */
	@JsonProperty("Remark")
	private String Remark;

	/**
	 * 假期说明
	 */
	@JsonProperty("TEXT")
	private String TEXT;
	/**
	 * 流程ID
	 */
	@JsonProperty("Seqid")
	private String Seqid;
	/**
	 * EID
	 */
	@JsonProperty("Eid")
	private String Eid;
}
