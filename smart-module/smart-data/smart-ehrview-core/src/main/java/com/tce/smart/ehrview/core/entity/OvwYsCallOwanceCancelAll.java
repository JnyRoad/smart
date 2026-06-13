package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/***
 * 外宿审批撤销实体类
 * @author QIPEI
 * 2019-09-09
 */
@Data
@TableName("ovw_YsCALLOWANCE_CANCEL_ALL")
public class OvwYsCallOwanceCancelAll extends Model<OvwYsCallOwanceCancelAll> {


	/**
	 * 员工号
	 */
	private String Badge;

	/**
	 * 补贴类型
	 */
	private Integer xtype;

	/**
	 * 补贴开始时间
	 */
	private String begindate;


}
