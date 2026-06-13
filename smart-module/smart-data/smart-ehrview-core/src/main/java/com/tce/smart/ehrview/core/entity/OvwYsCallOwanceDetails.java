package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("ovw_yscallowance_details")
public class OvwYsCallOwanceDetails {


	private Integer id;

	private Integer eid;

	private String badge;

	private Integer xtype;

	private Date begindate;

	private Date enddate;

	private Double amount;

	private Integer computationrule;

	private Integer convertrule;
}
