package com.tce.smart.guard.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@TableName("vcallcar2")
@EqualsAndHashCode(callSuper = true)
public class VcallCar extends Model<VcallCar> {
    private static final long serialVersionUID = 1L;

    /**
     * 指令单号
     */
    @TableField("CALLCODE")
    private String callCode;
    /**
     * 出货时间
     */
    @TableField("CALLSENTTIME")
    private Date callSentTime;

    /**
     * 3：已派车
     */
    @TableField("CALLSTATUS")
    private Integer callStatus;

    /**
     * 车牌号
     */
    @TableField("CARIDSTR")
    private String carIdStr;

    /**
     * 驾驶人姓名
     */
    @TableField("DRIVERNAMESTR")
    private String driverNameStr;

    /**
     * 驾驶员手机号
     */
    @TableField("DRIVERTELSTR")
    private String driverTelStr;

    /**
     * 所属单位
     */
    @TableField("SUPPLIERNAME")
    private String supplierName;

    @TableField("COMPANYID")
    private String companyId;

}
