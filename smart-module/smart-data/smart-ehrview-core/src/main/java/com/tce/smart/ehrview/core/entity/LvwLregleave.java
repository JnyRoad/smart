package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
@TableName("lvw_LREGLEAVE")
public class LvwLregleave extends Model<LvwLregleave> {

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
    private String begintime;
    private String endtime;
    private String begindate;
    private String enddate;
    private Double amount;
    private String unit;

}
