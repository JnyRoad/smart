package com.tce.smart.platform.core.entity;

import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 地点信息表
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:45
 */
@Data
@TableName("smt_area")
@EqualsAndHashCode(callSuper = true)
public class SmtArea extends Model<SmtArea> {
	private static final long serialVersionUID = 1L;

	/**
	 * 地点表
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;
	/**
	 * 所属园区id
	 */
	@NotBlank(message = "所属园区id不能为空")
	private Integer parkId;
	/**
	 * 地点名称
	 */
	@NotBlank(message = "地点名称不能为空")
	private String areaName;

	/**
	 * 上级地点id
	 */
	private Integer pid;


	/**
	 * 区域经度
	 */
	private BigDecimal areaLongitude;
	/**
	 * 区域纬度
	 */
	private BigDecimal areaLatitude;

	/**
	 * 备注
	 */
	private String remark;

}
