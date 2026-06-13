package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * <p>
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
@TableName("ovw_YsJob")
public class OvwYsjob extends Model<OvwYsjob> {

    private static final long serialVersionUID = 1L;

    private String jobid;
    private String jobname;
    @TableField("DepID")
    private Integer DepID;
    private String depname;
    @TableField("JchenID")
    private Integer JchenID;
    @TableField("JchenName")
    private String JchenName;
    @TableField("JXianID")
    private Integer JXianID;
    @TableField("JxianName")
    private String JxianName;
    @TableField("JQunID")
    private Integer JQunID;
    @TableField("JqunName")
    private String JqunName;
    @TableField("JZuID")
    private Integer JZuID;
    @TableField("JzuName")
    private String JzuName;
    @TableField("JZongID")
    private Integer JZongID;
    @TableField("JzongName")
    private String JzongName;
    @TableField("JobType")
    private Integer JobType;
    @TableField("jobTypeName")
    private String jobTypeName;
    @TableField("flCJ")
    private String flCJ;
    @TableField("EMPKIND")
    private Integer empkind;
    @TableField("empkindName")
    private String empkindName;
    @TableField("JCostID")
    private Integer JCostID;
    @TableField("JcostName")
    private String JcostName;
    private Date timestamp;
    @TableField("ASzstatus")
    private String ASzstatus;

}
