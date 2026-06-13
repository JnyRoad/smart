package com.tce.smart.platform.core.entity;

import javax.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区宿舍类型
 *
 * @author 齐佩
 * @date 2019-04-13 18:16:57
 */
@Data
@TableName("smt_dormitory_type")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryType extends Model<SmtDormitoryType> {
	private static final long serialVersionUID = 1L;

	/**
	 * 分类ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;
	/**
	 * 分类名称
	 */
	@NotBlank(message = "宿舍分类名称不能为空")
	private String typeName;
	/**
	 * 每个类型房间中床位的默认个数
	 */
	private Integer bedTotal;

	/**
	 * 所属园区
	 */
	private Integer parkId;

}
