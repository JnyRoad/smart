package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 招聘设置
 *
 * @author mckaywu
 * @date 2019-11-20 10:37:43
 */
@Data
@TableName("smt_recruitment_setting")
@EqualsAndHashCode(callSuper = true)
public class SmtRecruitmentSetting extends Model<SmtRecruitmentSetting> {

	private static final long serialVersionUID = -4414380514993348432L;

	/**
	 * 主键ID
	 */
	@TableId
	private Integer id;
	/**
	 * 园区主键
	 */
	private Integer parkId;
	/**
	 * 工作地点编号
	 */
	private String workBaseCode;

	/**
	 * 工作地点名称
	 */
	@TableField(exist = false)
	private String workBaseName;
	/**
	 * BU编号
	 */
	private String workCompId;
	/**
	 * BU名称
	 */
	@TableField(exist = false)
	private String workCompTitle;
	/**
	 * 签约单位ID
	 */
	private String workOrgId;
	/**
	 * 签约单位名称
	 */
	@TableField(exist = false)
	private String workOrgName;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

}
