package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区供应商表
 * @author QIPEI
 * 2020/2/11
 */
@Data
@TableName("smt_supplier")
@EqualsAndHashCode(callSuper = true)
public class SmtSupplier extends Model<SmtSupplier> {

	  /**
	   *
	   */
		@TableId(value = "id", type = IdType.AUTO)
	    private Integer id;


		private String name;

		private String legalPerson;

		private String remark;

		private Integer parkId;

		private String parkName;

		private String numbers;

		private LocalDateTime createTime;
}
