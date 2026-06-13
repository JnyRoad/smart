package com.tce.smart.platform.core.entity.ext;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据转移务表配置
 *
 * @author mkwu
 * @date 2019-08-06
 */
@Data
public class MoveDataTaskExt implements Serializable {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -2856284771309494853L;

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
