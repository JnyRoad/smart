package com.tce.smart.data.api.dto.msg.req;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调休申请EHR表字段
 *
 * @author mckaywu
 * @date 2019-06-19 17:32:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendRestReqDTO extends MainBaseTableReqDTO<SendRestReqDTO> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 923374173693564927L;

	/**
	 * 调休类型 1-全天 2-上午 3-下午
	 */
	@JsonProperty("Twid")
	private String Twid;


	/**
	 * 出勤日期 lvw_adjustPort
	 */
	@JsonProperty("Termid")
	private String Termid;

	/**
	 * 出勤日期（表单不显示后台要有）
	 */
	@JsonProperty("OLDBEGINTIME")
	private String OLDBEGINTIME;

	/**
	 * 可用天数
	 */
	@JsonProperty("PERIOD")
	private String PERIOD;

	/**
	 * 现在要用天数
	 */
	@JsonProperty("OLDAMOUNT")
	private String OLDAMOUNT;

	/**
	 * 需调休日期
	 */
	@JsonProperty("BEGINTIME")
	private String BEGINTIME;

	/**
	 * 备注
	 */
	@JsonProperty("Remark")
	private String Remark;

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
	/**
	 * Ezid
	 */
	@JsonProperty("Ezid")
	private String Ezid;
	/**
	 * Unit
	 */
	@JsonProperty("Unit")
	private String Unit;
	/**
	 * Amount
	 */
	@JsonProperty("Amount")
	private String Amount;
	/**
	 * 职层
	 */
	@JsonProperty("Jchenid")
	private String Jchenid;
}
