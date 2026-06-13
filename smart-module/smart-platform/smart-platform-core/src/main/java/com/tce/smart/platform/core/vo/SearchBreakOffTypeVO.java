package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调休类型返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchBreakOffTypeVO extends Model<SearchBreakOffTypeVO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 类型编号
	 */
	private String restCode;
	/**
	 * 类型名称
	 */
	private String restName;


}
