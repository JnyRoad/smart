package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * <p>
 * 班次
 * </p>
 *
 * @author 梁圆
 * @since 2019-05-03
 */
@Data
@TableName("evw_ashift_RunNo")
public class EvwAshiftRunNo extends Model<EvwAshiftRunNo> {

    private static final long serialVersionUID = 1L;

    @TableField("EmpNo")
    private String EmpNo;

    @TableField("RunName")
    private String runName;

    @TableField("EmpName")
    private String empName;

    @TableField("EmpRunDate")
    private String empRunDate;

    @TableField("Run1StartTime")
    private String run1StartTime;

    @TableField("Run1EndTime")
    private String run1EndTime;

    @TableField("Run2StartTime")
    private String run2StartTime;

    @TableField("Run2EndTime")
    private String run2EndTime;

    @TableField("Run3StartTime")
    private String run3StartTime;

    @TableField("Run3EndTime")
    private String run3EndTime;

    @TableField("Run4StartTime")
    private String run4StartTime;

    @TableField("Run4EndTime")
    private String run4EndTime;

    @TableField("Run5StartTime")
    private String run5StartTime;

    @TableField("Run5EndTime")
    private String run5EndTime;

    @TableField("Run6StartTime")
    private String run6StartTime;

    @TableField("Run6EndTime")
    private String run6EndTime;

}
