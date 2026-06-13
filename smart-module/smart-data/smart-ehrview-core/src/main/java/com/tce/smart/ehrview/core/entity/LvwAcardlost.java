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
@TableName("lvw_ACARDLOST")
public class LvwAcardlost extends Model<LvwAcardlost> {

    private static final long serialVersionUID = 1L;

    private String badge;
    @TableField("Name")
    private String name;
    private Integer compid;
    private String compname;
    @TableField("DepID")
    private Integer depId;
    private String depname;
    private String jobid;
    private String jobname;
    @TableField("KqStartDate")
    private Date kqStartDate;
    @TableField("KQINTIME2")
    private String kqintime2;
    @TableField("KQOUTTIME2")
    private String kqouttime2;
    @TableField("KQINTIME4")
    private String kqintime4;
    @TableField("KQOUTTIME4")
    private String kqouttime4;
    @TableField("KQINTIME5")
    private String kqintime5;
    @TableField("KQOUTTIME5")
    private String kqouttime5;
    private String reason;
    @TableField("REMARKS")
    private String remarks;

}
