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
@TableName("lvw_attend_ycxx")
public class LvwAttendYcxx extends Model<LvwAttendYcxx> {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String badge;
    @TableField("Name")
    private String Name;
    private Integer compid;
    @TableField("DepID")
    private Integer DepID;
    private String jobid;
    private Date attenddate;
    private String week;
    private String type;
    @TableField("type_remark")
    private String typeRemark;
    private String shiftid;
    private String in1;
    private String out1;
    private String in2;
    private String out2;
    private String in3;
    private String out3;
    private String remark;
    private String shift;
    @TableField("stdIn2")
    private String stdIn2;
    @TableField("stdOt2")
    private String stdOt2;
    @TableField("stdIn4")
    private String stdIn4;
    @TableField("stdOt4")
    private String stdOt4;
    @TableField("stdIn5")
    private String stdIn5;
    @TableField("stdOt5")
    private String stdOt5;
    @TableField("IsRight")
    private Boolean IsRight;
    @TableField("KqDateStr")
    private String KqDateStr;

}
