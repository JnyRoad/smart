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
@TableName("lvw_LLREGOT")
public class LvwLlregot extends Model<LvwLlregot> {

    private static final long serialVersionUID = 1L;

    private String badge;
    private String Name;
    private Integer compid;
    private String compname;
    private Integer DepID;
    private String depname;
    private String jobid;
    private String jobname;
    @TableField("TWID")
    private String twid;
    private Date otterm;
    @TableField("OT2STARTTIME")
    private String ot2starttime;
    @TableField("OT2ENDTIME")
    private String ot2endtime;
    @TableField("OT4STARTTIME")
    private String ot4starttime;
    @TableField("OT4ENDTIME")
    private String ot4endtime;
    @TableField("OT5STARTTIME")
    private String ot5starttime;
    @TableField("OT5ENDTIME")
    private String ot5endtime;
    private Double amount;
    private String reason;

}
