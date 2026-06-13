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
@TableName("ovw_YsComp")
public class OvwYscomp extends Model<OvwYscomp> {

    private Integer compid;
    private String title;
    @TableField("CompAbbr")
    private String CompAbbr;
    @TableField("CompGrade")
    private Integer CompGrade;
    @TableField("AdminID")
    private Integer AdminID;
    private Integer ezid;
    @TableField("DisabledDate")
    private Date DisabledDate;
    private Date timestamp;
    @TableField("ASzstatus")
    private String ASzstatus;

}
