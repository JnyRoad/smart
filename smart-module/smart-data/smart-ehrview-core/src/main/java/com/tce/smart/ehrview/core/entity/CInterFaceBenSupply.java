package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @description: CInterFaceBenSupply
 * @date: 2020/10/12 15:22
 * @author: wuling
 * @version: 1.0
 */
@Data
@TableName("cInterFace_BenSupply")
public class CInterFaceBenSupply {

	@TableField("Id")
	private Integer Id;

	@TableField("badge")
	private String Badge;

	@TableField("Amount")
	private BigDecimal Amount;

	@TableField("Object")
	private String Object;

	@TableField("IsDisPose")
	private String IsDisPose;
}
