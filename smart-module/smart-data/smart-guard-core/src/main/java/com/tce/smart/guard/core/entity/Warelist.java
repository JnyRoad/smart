package com.tce.smart.guard.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@TableName("warelist")
@EqualsAndHashCode(callSuper = true)
public class Warelist extends Model<Warelist> {
    private static final long serialVersionUID = 1L;

	@TableField("WARECODE")
	private String warecode;
	@TableField("WARECUSTOMNAME")
	private String warecustomname;
	@TableField("WARESENTADDR")
	private String waresentaddr;
	@TableField("WARESENTTIME")
	private Date waresenttime;
	@TableField("WAREPICKMAN")
	private String warepickman;
	@TableField("WAREMANGER")
	private String waremanger;
	@TableField("WAREOPRATEMAN")
	private String wareoprateman;
	@TableField("WAREOPRATETIME")
	private Date wareopratetime;
	@TableField("WAREPART")
	private Integer warepart;
	@TableField("WAREPARENTCODE")
	private String wareparentcode;
	@TableField("WARECREATETIME")
	private Date warecreatetime;
	@TableField("WARESTATUS")
	private Integer warestatus;
	@TableField("WARECUSTOMCODE")
	private String warecustomcode;
	@TableField("COMPANYID")
	private String companyid;
	@TableField("WAREORDERSTATUS")
	private Integer wareorderstatus;
	@TableField("WARELNG")
	private String warelng;
	@TableField("WARELAT")
	private String warelat;
	@TableField("WAREPROVINCE")
	private String wareprovince;
	@TableField("WARECITY")
	private String warecity;
	@TableField("WAREAREA")
	private String warearea;
	@TableField("WARETOWN")
	private String waretown;
	@TableField("WAREMARK")
	private Integer waremark;
	@TableField("WAREISHIGH")
	private Integer wareishigh;
	@TableField("TYPECODE")
	private String typecode;
	@TableField("WAREREMARK")
	private String wareremark;


}
