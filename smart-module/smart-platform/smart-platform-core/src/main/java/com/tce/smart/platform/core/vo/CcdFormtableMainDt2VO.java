package com.tce.smart.platform.core.vo;

import java.sql.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 出差报告数据
 *
 * @author 梁园
 * @date 2019-06-24
 */
@Data
@TableName("ccd_formtable_main_dt2")
@EqualsAndHashCode(callSuper = true)
public class CcdFormtableMainDt2VO extends Model<CcdFormtableMainDt2VO> {
	 private static final long serialVersionUID = 1L;

	    @TableField("MAINID")
	    private Integer mainId;

	    @TableField("TripTime")
	    private Date tripTime;


	    @TableField("BusinessTrip")
	    private String businessTrip;

	    @TableField("WorkItem")
	    private String workItem;

	    @TableField("ExpectedEffect")
	    private String expectedEffect;

	    @TableField("ConfirmDep")
	    private String confirmDep;

	    @TableField("Recommendations")
	    private String recommendations;

}
