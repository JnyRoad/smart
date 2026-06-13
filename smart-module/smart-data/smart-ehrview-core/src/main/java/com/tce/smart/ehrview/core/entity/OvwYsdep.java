package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
@TableName("ovw_YsDep")
public class OvwYsdep extends Model<OvwYsdep> {

    private static final long serialVersionUID = 1L;

    private Integer depid;
    private String depname;
    @TableField("DepAbbr")
    private String DepAbbr;
    @TableField("CompID")
    private Integer CompID;
    private String director;
    @TableField("DirecName")
    private String DirecName;
    @TableField("DepGrade")
    private String DepGrade;
    @TableField("DisabledDate")
    private Date DisabledDate;
    @TableField("adminID")
    private Integer adminID;
    @TableField("DepCost")
    private String DepCost;
    private Date timestamp;
    @TableField("ASzstatus")
    private String ASzstatus;

}
