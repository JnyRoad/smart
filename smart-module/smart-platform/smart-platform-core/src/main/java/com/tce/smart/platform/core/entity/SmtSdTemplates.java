package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * @description: 水电模板表
 * @date: 2020-07-01 14:52
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("smt_sd_templates")
@EqualsAndHashCode(callSuper = true)
public class SmtSdTemplates extends Model<SmtSdTemplates> {
	private static final long serialVersionUID = 2741460830973596081L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 模板名称
	 */
	@NotBlank(message="模板名称不能为空")
	private String templateName;


	/**
	 * 所属园区id
	 */
	@NotBlank(message="所属园区id不能为空")
	private Integer parkId;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 级层ID
	 */
	private Integer jchenid;

	/**
	 * 级层名称
	 */
	private String jchenname;
}
