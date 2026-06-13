package com.tce.smart.data.api.dto.msg.req;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外宿申请
 * @author QIPEI
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendOutDormitoryReqDTO extends MainBaseTableReqDTO<SendOutDormitoryReqDTO> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 923374173693564927L;


	/**
	 * 员工状态
	 */
	@JsonProperty("Status")
	private Integer Status;

	/**
	 * 入职时间
	 */
	@JsonProperty("Joindate")
	private String Joindate;

	/**
	 * 福利层次
	 */
	@JsonProperty("FLCJ")
	private String FLCJ;

	/**
	 * 补贴开始月份
	 */
	@JsonProperty("Begindate")
	private String Begindate;

	/**
	 * 补贴结束月份
	 */
	@JsonProperty("APPenddate")
	private String APPenddate;


	/**
	 * 补贴类型
	 */
	@JsonProperty("Xtype")
	private Integer Xtype;


	/**
	 * 补贴金额
	 */
	@JsonProperty("Amount")
	private Double Amount;
	/**
	 * 计算规则
	 */
	@JsonProperty("COMPUTATIONRULE")
	private Integer COMPUTATIONRULE;


	/**
	 * 补贴说明
	 */
	@JsonProperty("EXPLAIN")
	private String EXPLAIN;

	/**
	 * 流程编号
	 */
	@JsonProperty("Seqid")
	private Integer Seqid;


	@JsonProperty("EID")
	private String EID;

	 @JsonProperty("PZID")
	 private Integer PZID;

}
