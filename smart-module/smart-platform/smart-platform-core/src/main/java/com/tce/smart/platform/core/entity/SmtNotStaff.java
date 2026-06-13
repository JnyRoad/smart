package com.tce.smart.platform.core.entity;

import java.util.Date;

import javax.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import cn.hutool.core.date.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:42
 */
@Data
@TableName("smt_not_staff")
@EqualsAndHashCode(callSuper = true)
public class SmtNotStaff extends Model<SmtNotStaff> {
	private static final long serialVersionUID = 1L;

	/**
	*
	*/
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;


	/**
	 * 所属园区id
	 */
	private String parkId;

	private Long vehicleId;

	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 电话
	 */
	private String phone;

	/**
	 * 备注
	 */
	private String remark;

}
