package com.tce.smart.data.api.dto.msg.req;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 离职申请基本字段Ao
 *
 * @author mingkai.wu
 * @date 2019-05-15 10:33:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendLeaveMainTableReqDTO extends MainBaseTableReqDTO<SendLeaveMainTableReqDTO> {

	/**
	 *
	 */
	private static final long serialVersionUID = 8053098423912728322L;

    @JsonProperty("badge")
    private String badge;
    @JsonProperty("name")
    private String name;
    @JsonProperty("compid")
    private String compid;
    @JsonProperty("depid")
    private String depid;
    @JsonProperty("Jobid")
    private String Jobid;
    @JsonProperty("Jchenid")
    private String Jchenid;
    @JsonProperty("Jxianid")
    private String Jxianid;
    @JsonProperty("Leavetype")
    private String Leavetype;
    @JsonProperty("Leavereason")
    private String Leavereason;
    @JsonProperty("LEAINTENT")
    private String LEAINTENT;
    @JsonProperty("Joindate")
    private String Joindate;
    @JsonProperty("ApplyDate")
    private String ApplyDate;
    @JsonProperty("Leavedate")
    private String Leavedate;
    @JsonProperty("NEWCOMPANY")
    private String NEWCOMPANY;
    @JsonProperty("NEWJOB")
    private String NEWJOB;
    @JsonProperty("NEWSALARY")
    private String NEWSALARY;
    @JsonProperty("IFPY")
    private String IFPY;
    @JsonProperty("ISCGPY")
    private String ISCGPY;
    @JsonProperty("Ezid")
    private String Ezid;
    @JsonProperty("YEARDAY")
    private String YEARDAY;
    @JsonProperty("type")
    private Integer type;
    @JsonProperty("EID")
    private String EID;
    @JsonProperty("seqid")
    private Integer seqid;
    @JsonProperty("isEndCon")
    private Integer isEndCon;

}
