package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/2 16:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_ORGANIZE_ACCESS")
@EqualsAndHashCode(callSuper = true)
public class SmtOrganizeAccess extends Model<SmtOrganizeAccess> {

	private static final long serialVersionUID = -3902331923944793619L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 组织ID
	 */
	private Long organizeId;

	/**
	 * 门禁权限ID
	 */
	private Integer deviceAuthId;

}
