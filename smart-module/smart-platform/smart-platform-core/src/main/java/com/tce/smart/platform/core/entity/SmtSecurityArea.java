package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @description: 保密区预
 * @date: 2020-07-30 8:52
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("smt_security_area")
@EqualsAndHashCode(callSuper = true)
public class SmtSecurityArea extends Model<SmtSecurityArea> {

	private static final long serialVersionUID = 8213014904037779448L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	/**
     * code
	 */
	@NotNull(message = "编号不能为空")
	private Integer code;

	/**
     * 字段名
	 */
	@NotBlank(message = "字段名不能为空")
	private String type;

	/**
     * 名称
	 */
	@NotBlank(message = "名称不能为空")
	@TableField("\"DESC\"")
	private String desc;

	/**
	 * 工厂, 1=新工厂, 2-老工厂
	 */
	@NotNull(message = "所属工厂不能为空")
	@Min(value = 1, message = "所属工厂参数有误")
	@Max(value = 2, message = "所属工厂参数有误")
	private Integer factoryType;
}
