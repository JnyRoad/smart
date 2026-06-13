package com.tce.smart.data.api.dto.consume.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.common.core.ao.BaseAO;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 充值计划表
 * </p>
 *
 * @author fushiping
 * @since 2020-08-08
 */
@Data
@TableName("XF_PLANMONEY")
public class XfPlanMoneyReqDTO extends BaseAO {

    private static final long serialVersionUID = 1L;

    @TableField("工号")
    private String EMPNO;

    @TableField("姓名")
    private String EMPNAME;

    @TableField("部门")
    private String DPTNO;

    @TableField("部门名称")
    private String DPTNAME;

    @TableField("计划充值金额")
    private Double PlanPutMoneyValue;

}
