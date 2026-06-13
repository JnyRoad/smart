package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职工调休申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:30:08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AddBreakOffApplicationDTO extends Model<AddBreakOffApplicationDTO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 出勤日期
	 */
	private String term;
	/**
	 * 调休的id
	 */
	private String TermId;
	/**
	 * 员工号
	 */
	private String staffBadge;

	/**
	 * 调休类型
	 */
	private String restType;
	/**
	 * 调休时间
	 */
	private String restDate;
	/**
	 * 出勤时间标题
	 */
	private String workDate;
	/**
	 * 现在要调休天数
	 */
	private String restCount;
	/**
	 * 可调休天数
	 */
	private String restAbleCount;
	/**
	 * 出勤日期剩余可调休天数
	 */
	private String termCount;
	/**
	 *调休备注
	 */
	private String vacateDesc;

}
