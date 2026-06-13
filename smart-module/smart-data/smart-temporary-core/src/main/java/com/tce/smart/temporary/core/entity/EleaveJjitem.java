package com.tce.smart.temporary.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
@TableName("eLeave_JJitem")
public class EleaveJjitem extends Model<EleaveJjitem> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @NotNull(message = "人事区域不可空")
    @TableField("EZID")
    private Integer ezid;
    @NotBlank(message = "员工号不可空")
    private String badge;
    @NotBlank(message = "员工姓名不可空")
    private String name;
//    @NotNull(message = "入职时间不可空")
//    @TableField("StartDate")
//    private Date startDate;
    @NotNull(message = "离职时间不可空")
    @TableField("leaveDate")
    private Date leaveDate;
    @NotNull(message = "责任部门不可空")
    @TableField("ZRDep")
    private Integer zrDep;
    @NotBlank(message = "交接项不可空")
    @TableField("JJItem")
    private String jjItem;
    @NotBlank(message = "交接人工号不可空")
    @TableField("JJR")
    private String jjr;
    @NotBlank(message = "交接人姓名不可空")
    @TableField("JJRName")
    private String jjrName;
    @NotBlank(message = "确认人工号不可空")
    @TableField("QRR")
    private String qrr;
    @NotBlank(message = "确认人姓名不可空")
    @TableField("QRRName")
    private String qrrName;
    @TableField("JE")
    private Double je;
    @TableField("JJRemark")
    private String jjRemark;
    @NotNull(message = "开始标识不可空")
    @TableField("JJBegin")
    private Integer jjBegin;
    @NotNull(message = "开始时间不可空")
    @TableField("JJBegintime")
    private Date jjBegintime;
    @NotNull(message = "结束标识不可空")
    @TableField("JJClosed")
    private Integer jjClosed;
    @NotNull(message = "结束时间不可空")
    @TableField("JJClosedTime")
    private Date jjClosedTime;
    @TableField("EID")
    private Integer eid;

    public EleaveJjitem() {
        super();
    }

    public EleaveJjitem(Integer ezid,String badge,
            String name, Date leaveDate,
            Integer zrDep, String jjItem,
            String jjr, String jjrName,
            String qrr, String qrrName, Double je,
            String jjRemark, Integer jjBegin,
            Date jjBegintime, Integer jjClosed,
            Date jjClosedTime,Integer eid) {
        this.ezid = ezid;
        this.badge = badge;
        this.name = name;
        this.leaveDate = leaveDate;
        this.zrDep = zrDep;
        this.jjItem = jjItem;
        this.jjr = jjr;
        this.jjrName = jjrName;
        this.qrr = qrr;
        this.qrrName = qrrName;
        this.je = je;
        this.jjRemark = jjRemark;
        this.jjBegin = jjBegin;
        this.jjBegintime = jjBegintime;
        this.jjClosed = jjClosed;
        this.jjClosedTime = jjClosedTime;
        this.eid = eid;
    }


}
