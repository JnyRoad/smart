package com.tce.smart.data.api.dto.msg.req;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 补卡申请EHR表字段
 *
 * @author mckaywu
 * @date 2019-06-19 17:32:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendAttendancePatchkAo extends MainBaseTableReqDTO<SendAttendancePatchkAo> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 568829416215714191L;

	/**
	 * 职层
	 */
	@JsonProperty("Jchenid")
	private String Jchenid;

	/**
	 * 补卡日期 lvw_attend_yc
	 */
	@JsonProperty("KQSTARTDATE")
	private String KQSTARTDATE;

	/**
	 * 第二段进
	 */
	@JsonProperty("KQINTIME2")
	private String KQINTIME2;

	/**
	 * 第二段出
	 */
	@JsonProperty("KQOUTTIME2")
	private String KQOUTTIME2;

	/**
	 * 二段出是否跨天(布尔类字符串,true,false)
	 */
	@JsonProperty("OUT2")
	private Integer OUT2;

	/**
	 * 第四段进
	 */
	@JsonProperty("KQINTIME4")
	private String KQINTIME4;

	/**
	 * 第四段出
	 */
	@JsonProperty("KQOUTTIME4")
	private String KQOUTTIME4;

	/**
	 * 四段进是否跨天(布尔类字符串,true,false)
	 */
	@JsonProperty("IN4")
	private Integer IN4;

	/**
	 * 四段出是否跨天(布尔类字符串,true,false)
	 */
	@JsonProperty("OUT4")
	private Integer OUT4;

	/**
	 * 第五段进
	 */
	@JsonProperty("KQINTIME5")
	private String KQINTIME5;

	/**
	 * 第五段出
	 */
	@JsonProperty("KQOUTTIME5")
	private String KQOUTTIME5;

	/**
	 * 5段进是否跨天(布尔类字符串,true,false)
	 */
	@JsonProperty("IN5")
	private Integer IN5;
	/**
	 * 五段出是否跨天(布尔类字符串,true,false)
	 */
	@JsonProperty("Out5")
	private Integer Out5;

	/**
	 * 补卡原因 lvw_LCD_cardtype
	 */
	@JsonProperty("Reason")
	private String Reason;

	/**
	 * 备注
	 */
	@JsonProperty("REMARKS")
	private String REMARKS;

	/**
	 * 附件
	 */
	@JsonProperty("FJ")
	private String FJ;

	/**
	 * 提示信息
	 */
	@JsonProperty("ERRMSG")
	private String ERRMSG;

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
	 * shift
	 */
	@JsonProperty("shift")
	private String shift;
	/**
	 * stdIn2
	 */
	@JsonProperty("stdIn2")
	private String stdIn2;
	/**
	 * stdOt2
	 */
	@JsonProperty("stdOt2")
	private String stdOt2;
	/**
	 * stdIn4
	 */
	@JsonProperty("stdIn4")
	private String stdIn4;
	/**
	 * stdOt4
	 */
	@JsonProperty("stdOt4")
	private String stdOt4;
	/**
	 * stdIn5
	 */
	@JsonProperty("stdIn5")
	private String stdIn5;
	/**
	 * stdOt5
	 */
	@JsonProperty("stdOt5")
	private String stdOt5;
	/**
	 * EmpNoList
	 */
	@JsonProperty("EmpNoList")
	private String EmpNoList;

}
