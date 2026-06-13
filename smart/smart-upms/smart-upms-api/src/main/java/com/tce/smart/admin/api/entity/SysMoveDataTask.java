package com.tce.smart.admin.api.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据转移务表配置
 *
 * @author mkwu
 * @date 2019-08-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysMoveDataTask extends Model<SysMoveDataTask> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 8664957317452957432L;

	/**
	 * 源表名称
	 */
	private String srcTable;

	/**
	 * 目标表名称
	 */
	private String destTable;

	/**
	 * 模块类型:1-smart 2-platform 3-app
	 */
	private Integer moduleType;

	/**
	 * 保留几个月
	 */
	private Integer retainMonth;

	/**
	 * 对应表时间字段名称
	 */
	private String dateColumnName;

	/**
	 * 生效标志:0-失效 1-生效
	 */
	private Integer effectFlag;

	/**
	 * 操作类型:1-转移 2-删除
	 */
	private Integer optType;

}
