package com.tce.smart.data.api.dto.msg.req;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 加班申请EHR表字段
 *
 * @author mckaywu
 * @date 2019-06-19 17:32:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendExtraworkReqDTO extends MainBaseTableReqDTO<SendExtraworkReqDTO> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 32147840029509458L;

	/**
	 * 班别 1-白班/2-夜班
	 */
	@JsonProperty("TWID")
	private String TWID;
	/**
	 * 加班类型
	 */
	@JsonProperty("OTTYPE")
	private String OTTYPE;
	/**
	 * 加班日期
	 */
	@JsonProperty("OTTerm")
	private String OTTerm;
	/**
	 * 加班时长
	 */
	@JsonProperty("Amount")
	private String Amount;

	/**
	 * 第二段加班开始时间
	 */
	@JsonProperty("OT2STARTTIME")
	private String OT2STARTTIME;

	/**
	 * 第二段加班结束时间
	 */
	@JsonProperty("OT2ENDTIME")
	private String OT2ENDTIME;

	/**
	 * 4第四段加班开始时间
	 */
	@JsonProperty("OT4STARTTIME")
	private String OT4STARTTIME;

	/**
	 * 第四段加班结束时间
	 */
	@JsonProperty("OT4ENDTIME")
	private String OT4ENDTIME;

	/**
	 * 第五段加班开始时间
	 */
	@JsonProperty("OT5STARTTIME")
	private String OT5STARTTIME;

	/**
	 * 第五段加班结束时间
	 */
	@JsonProperty("OT5ENDTIME")
	private String OT5ENDTIME;

	/**
	 * 是否出差（布尔类型字符串:true,false）
	 */
	@JsonProperty("Iscc")
	private String Iscc;

	/**
	 * 附件
	 */
	@JsonProperty("FJ")
	private String FJ;

	/**
	 * 加班原因
	 */
	@JsonProperty("Reason")
	private String Reason;
	/**
	 * 备注
	 */
	@JsonProperty("Remark")
	private String Remark;
	/**
	 *流程id
	 */
	@JsonProperty("Seqid")
	private String Seqid;
	/**
	 * Ezid
	 */
	@JsonProperty("Ezid")
	private String Ezid;
	/**
	 *职层
	 */
	@JsonProperty("Jchenid")
	private String Jchenid;
	/**
	 * EID
	 */
	@JsonProperty("Eid")
	private String Eid;
	/**
	 * OT2TYPENAME
	 */
	@JsonProperty("OT2TYPENAME")
	private String OT2TYPENAME;
	/**
	 * OT4TYPENAME
	 */
	@JsonProperty("OT4TYPENAME")
	private String OT4TYPENAME;
	/**
	 * OT5TYPENAME
	 */
	@JsonProperty("OT5TYPENAME")
	private String OT5TYPENAME;
}
