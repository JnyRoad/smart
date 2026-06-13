package com.tce.smart.platform.api.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

@Data
public class SmtWageSignDTO implements Serializable {
	private static final long serialVersionUID = 6199116473892965887L;

	/**
	 * ID
	 */
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
	private Date createTime;

}
