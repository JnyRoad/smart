package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区供应商员工表
 * @author QIPEI
 * 2020/2/11
 */
@Data
@TableName("smt_supplier_staff")
@EqualsAndHashCode(callSuper = true)
public class SmtSupplierStaff extends Model<SmtSupplierStaff> {

	  /**
	   *
	   */
		@TableId(value = "id", type = IdType.AUTO)
	    private Integer id;


		private String name;

		private Integer supplierId;

		private Integer parkId;

		private String remark;

		private String phone;

		private LocalDateTime createTime;



}
