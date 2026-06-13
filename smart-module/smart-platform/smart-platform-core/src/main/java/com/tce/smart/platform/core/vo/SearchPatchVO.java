package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 补卡信息返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchPatchVO extends Model<SearchPatchVO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 考勤月份
	 */
	private String patchMonth;
	/**
	 * 考勤日期
	 */
	private String patchDate;
	/**
	 * 2入
	 */
	private String secondEnter;
	/**
	 * 2出
	 */
	private String secondOut;
	/**
	 * 4入
	 */
	private String fourthEnter;
	/**
	 * 4出
	 */
	private String fourthOut;
	/**
	 * 5入
	 */
	private String fifthEnter;
	/**
	 * 5出
	 */
	private String fifthOut;

}
