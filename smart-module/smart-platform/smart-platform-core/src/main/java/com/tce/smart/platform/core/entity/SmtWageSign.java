package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("smt_wage_sign")
@EqualsAndHashCode(callSuper = true)
public class SmtWageSign extends Model<SmtWageSign> {
	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 员工号
	 */
	@NotBlank(message = "员工号不能为空")
	private String badge;

	/**
	 * 工资月份
	 */
	@NotBlank(message = "工资月份不能为空")
	private String wageDate;

	/**
	 * 签名照
	 */
	private String signImg;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 签收状态
	 */
	private Integer signStatus;

	/**
	 * 通知状态
	 */
	private Integer noticeStatus;

}
