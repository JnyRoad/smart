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
@TableName("lvw_ayearholiday")
public class LvwAyearholiday extends Model<LvwAyearholiday> {

    private static final long serialVersionUID = 1L;

    @TableField("BADGE")
    private String badge;
    private String name;
    private Integer compid;
    private String compname;
    private Integer depid;
    private String depname;
    private String jobid;
    private String jobname;
    private Date startdate;
    @TableField("FromDate")
    private Date FromDate;
    @TableField("PREBALANCEUSED")
    private Double prebalanceused;
    @TableField("THISSHOULD")
    private Double thisshould;
    @TableField("THISUSED")
    private Double thisused;
    @TableField("ALTERNUM")
    private Double alternum;
    @TableField("THISBALANCE")
    private Double thisbalance;
}
