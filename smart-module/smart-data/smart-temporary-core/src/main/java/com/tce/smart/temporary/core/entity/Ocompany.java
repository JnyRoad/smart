package com.tce.smart.temporary.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
/**
 *
 * @author QIPEI
 *
 */
@Data
@TableName("oCompany")
public class Ocompany  extends Model<Ocompany>{

	 private static final long serialVersionUID = 1L;

	@TableField("CompID")
	private Integer CompID;

	@TableField("CompCode")
	private String CompCode;

	@TableField("EZID")
	private Integer EZID;


}
