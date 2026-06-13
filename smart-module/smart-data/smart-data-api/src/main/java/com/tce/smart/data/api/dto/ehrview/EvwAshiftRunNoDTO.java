package com.tce.smart.data.api.dto.ehrview;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 班次
 * </p>
 *
 * @author 梁圆
 * @since 2019-05-03
 */
@Data
public class EvwAshiftRunNoDTO implements Serializable {

    private static final long serialVersionUID = 1414247356060788508L;

    private String EmpNo;

    private String runName;

    private String empName;

    private String empRunDate;

    private String run1StartTime;

    private String run1EndTime;

    private String run2StartTime;

    private String run2EndTime;

    private String run3StartTime;

    private String run3EndTime;

    private String run4StartTime;

    private String run4EndTime;

    private String run5StartTime;

    private String run5EndTime;

    private String run6StartTime;

    private String run6EndTime;

}
