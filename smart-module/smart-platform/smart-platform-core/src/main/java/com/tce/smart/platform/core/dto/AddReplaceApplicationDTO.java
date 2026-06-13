package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 添加补卡申请
 *
 * @author 梁圆
 * @date 2019-05-05 18:19:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AddReplaceApplicationDTO extends Model<AddReplaceApplicationDTO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 员工号
	 */
	private String staffBadge;

	/**
	 * 考勤月份
	 */
	private String patchMonth;
	/**
	 * 补卡开始时间
	 */
	private String patchDate;
	/**
	 *补卡原因
	 */
	private String patchReason;

	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 附件图片base位
	 */
	private String photo;

	/**
	 * 2入
	 */
	private String secondEnter;

	/**
	 * 2出
	 */
	private String secondOut;
	/**
	 * 2出是否跨天
	 */
	private String secondOutCover;
	/**
	 * 4入
	 */
	private String fourthEnter;
	/**
	 * 4出
	 */
	private String fourthOut;

	/**
	 * 4入是否跨天
	 */
	private String fourthEnterCover;
	/**
	 * 4出是否跨天
	 */
	private String fourthOutCover;
	/**
	 * 5入
	 */
	private String fifthEnter;
	/**
	 * 5出
	 */
	private String fifthOut;

	/**
	 * 5入是否跨天
	 */
	private String fifthEnterCover;
	/**
	 * 5出是否跨天
	 */
	private String fifthOutCover;

}
