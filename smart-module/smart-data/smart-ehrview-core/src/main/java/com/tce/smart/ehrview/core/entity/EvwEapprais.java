package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

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
@TableName("evw_Eapprais")
public class EvwEapprais extends Model<EvwEapprais> {

	/**
	 * 工号
	 */
	@TableField("Badge")
    private String badge;

    /**
     *姓名
     */
    @TableField("Name")
    private String name;

    /**
     * 公司编码
     */
    @TableField("Compid")
    private String CompID;

    /**
     * 公司
     */
    @TableField("compname")
    private String compname;

    /**
     * 部门编码
     */
    @TableField("Depid")
    private String DepID;

    /**
     * 部门
     */
    @TableField("Depname")
    private String depname;

    /**
     * 岗位编码
     */
    @TableField("Jobid")
    private String JobID;

    /**
     * 岗位
     */
    @TableField("JobName")
    private String jobname;

    /**
     * 职层
     */
    @TableField("Jchenid")
    private String Jchenid;

    /**
     * 职层名称
     */
    @TableField("JchenName")
    private String JchenName;

    /**
     * 入职日期
     */
    @TableField("Joindate")
    private String joindate;

    /**
     * 奖项
     */
    @TableField("Prize")
    private String prize;

    /**
     * 发生日期
     */
    @TableField("effectdate")
    private String effectdate;

}
