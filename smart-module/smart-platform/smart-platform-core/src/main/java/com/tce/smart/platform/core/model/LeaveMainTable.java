package com.tce.smart.platform.core.model;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: LeaveMainTable
 * @Author jinbo
 * @Date 2019/5/10
 */
@Data
public class LeaveMainTable extends Model<LeaveMainTable> {

    private static final long serialVersionUID = 1L;
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

}