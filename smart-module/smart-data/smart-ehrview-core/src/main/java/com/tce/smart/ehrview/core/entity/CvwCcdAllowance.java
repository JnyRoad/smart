package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;

/*
 * 补贴类型
 */
@Data
@TableName("cvw_CCD_ALLOWANCE")
public class CvwCcdAllowance  extends Model<CvwCcdAllowance>{

    @TableField("Id")
    private Integer Id;

    @TableField("Title")
    private String Title;

    @TableField("ComputationRule")
    private Integer ComputationRule;

    @TableField("ConvertRule")
    private Integer ConvertRule;

    @TableField("Pzid")
    private Integer Pzid;
}
