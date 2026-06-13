package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.util.Date;

/**
 * @description: 保密区供应商人员表
 * @date: 2020-07-20 13:37
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_SUPPLIER_PERSON")
@EqualsAndHashCode(callSuper = true)
public class SmtSupplierPerson extends Model<SmtSupplierPerson> {

	private static final long serialVersionUID = 7746396877644254227L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
     * 保密区供应商记录标识
	 */
	private Long supplierId;

	/**
     * 人员名称
	 */
	private String personName;

	/**
	 * 电话
	 */
	private String phone;

	/**
     * 身份证
	 */
	private String idCard;

	/**
	 * 备注
	 */
	private String remark;

	/**
     * 创建时间
	 */
	private Date createTime;

    /**
     * 最后更新时间
	 */
	private Date updateTime;
}
