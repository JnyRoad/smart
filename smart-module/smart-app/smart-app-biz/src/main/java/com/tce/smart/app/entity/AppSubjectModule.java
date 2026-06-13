package com.tce.smart.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("app_subject_module")
@EqualsAndHashCode(callSuper = true)
public class AppSubjectModule extends Model<AppSubjectModule> {
	private static final long serialVersionUID = 1L;
	/**
	 * 主键ID
	 */
	private Integer id;
	/**
	 * 协议主题ID
	 */
	private Integer subjectId;
	/**
	 * 对应模块ID
	 */
	private Integer moduleId;

}
