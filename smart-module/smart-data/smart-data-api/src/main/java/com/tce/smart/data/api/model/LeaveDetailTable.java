package com.tce.smart.data.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: LeaveDetailTable
 * @Author jinbo
 * @Date 2019/5/16
 */
@Data
public class LeaveDetailTable implements Serializable {

    private static final long serialVersionUID = 1L;
    @JsonProperty("Badge")
    private String Badge;

    @JsonProperty("Name")
    private String Name;

    @JsonProperty("Compid")
    private String Compid;

    @JsonProperty("Depid")
    private String Depid;

    @JsonProperty("Jobid")
    private String Jobid;

    @JsonProperty("Jchenid")
    private String Jchenid;

    @JsonProperty("Joindate")
    private String Joindate;

    @JsonProperty("Prize")
    private String Prize;

    @JsonProperty("effectdate")
    private String effectdate;
}
