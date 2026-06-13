package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@Data
@TableName("evw_JJitem")
public class EvwJjitem extends Model<EvwJjitem> {

	@TableField("ID")
	private Integer JJItemId;
    @TableField("EZID")
    private Integer EZID;
    private String empzone;
    @TableField("ZRDep")
    private Integer ZRDep;
    @TableField("ZRDepName")
    private String ZRDepName;
    @TableField("JJItem")
    private String JJItem;
    @TableField("JJR")
    private String JJR;
    @TableField("JJRName")
    private String JJRName;
	@TableField("JE")
    private Double JE;
	@TableField("JJremark")
    private String JJremark;

}
