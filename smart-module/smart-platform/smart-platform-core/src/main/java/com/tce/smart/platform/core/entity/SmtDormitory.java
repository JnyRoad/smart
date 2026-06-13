package com.tce.smart.platform.core.entity;

import javax.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.text.Collator;
import java.util.Locale;

/**
 * 园区宿舍楼表
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:25
 */
@Data
@TableName("smt_dormitory")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitory extends Model<SmtDormitory> implements Comparable<SmtDormitory>{
	private static final long serialVersionUID = 1L;

	/**
	 * 宿舍楼ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;
	/**
	 * 宿舍楼名称
	 */
	@NotBlank(message = "宿舍楼名称不能为空")
	private String dormitoryName;

	/**
	 * 宿舍楼楼层
	 */
	private Integer floorNum;

	/**
	 * 所属园区ID
	 */
	private Integer parkId;

	@Override
	public int compareTo(SmtDormitory o) {
		return Collator.getInstance(Locale.CHINA).compare(dormitoryName, o.getDormitoryName());
	}
}
