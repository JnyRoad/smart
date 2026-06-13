package com.tce.smart.data.api.dto.msg.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 撤销外宿补贴申请请求类
 * @author QIPEI
 *
 */
@Data
public class SendCallowanceCancelReqDTO extends MainBaseTableReqDTO<SendCallowanceCancelReqDTO> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 832054726625907587L;

	/**
	 *  员工序号
	 */
	@JsonProperty("EID")
	private Integer  EID;

	/**
	 * 员工号
	 */
	@JsonProperty("Badge")
	private String Badge;

	/**
	 * 员工姓名
	 */
	@JsonProperty("Name")
	private String Name;

	/**
	 * 补贴类型 10-外食补贴，11-外宿补贴
	 */
	@JsonProperty("Xtype")
	private Integer Xtype;

	/**
	 * 补贴开始时间
	 */
	@JsonProperty("Begindate")
	private String Begindate;

	/**
	 * 补贴结束时间
	 */
	@JsonProperty("APPENDDATE")
	private String APPENDDATE;

	/**
	 * 补贴撤销时间
	 */
	@JsonProperty("Backdate")
	private String Backdate;

	/**
	 * 是否撤销  0-否  1-是
	 */
	@JsonProperty("IFCANCEL")
	private Integer IFCANCEL;

	/**
	 * bu
	 */
	@JsonProperty("Compid")
	private String Compid;
	/**
	 * 岗位id
	 */
	@JsonProperty("Jobid")
	private String Jobid;
	/**
	 * 部门id
	 */
	@JsonProperty("Depid")
	private String Depid;

	/**
	 * 申请id
	 */
	@JsonProperty("APPID")
	private Integer APPID;

	/**
	 * 薪资区域
	 */
	@JsonProperty("PZID")
	private Integer PZID;


	/**
	 * 补贴金额
	 */
	@JsonProperty("Amount")
	private Double Amount;

	@JsonProperty("COMPUTATIONRULE")
	private Integer COMPUTATIONRULE;

	/**
	 * 备注
	 */
	@JsonProperty("Remark")
	private String Remark ;

	/**
	 * 登记人 传员工EID
	 */
	@JsonProperty("REGBY")
	private Integer REGBY;

	@JsonProperty("Status")
	private Integer Status;

	@JsonProperty("Joindate")
	private String Joindate;

	@JsonProperty("FLCJ")
	private String FLCJ;


}
