package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * @description: 水电模板项目表
 * @date: 2020-07-01 14:52
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_TEMPLATES_CATEGORY")
@EqualsAndHashCode(callSuper = true)
public class SmtTemplatesCategory extends Model<SmtTemplatesCategory> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 所属模板ID
	 */
	@NotBlank(message="所属模板ID不能为空")
	private Long tempId;

	/**
	 * 项目名称
	 */
	@NotBlank(message="模板名称不能为空")
	private String categoryName;

	/**
	 * 父项目ID
	 */
	private Long parentId;
}
