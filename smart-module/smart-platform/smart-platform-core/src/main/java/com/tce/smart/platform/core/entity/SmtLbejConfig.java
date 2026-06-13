package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

/**
 * @description: 员工离职交接伙食费交接配置表
 * @date: 2021-02-26 13:37
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_LBEJ_CONFIG")
@EqualsAndHashCode(callSuper = true)
public class SmtLbejConfig extends Model<SmtLbejConfig> {

	/**
     * 交接项Id
	 */
	private Integer itemId;

	/**
     * 部门Id
	 */
	private Integer depId;


	/**
	 * EZ_ID
	 */
	private Integer ezId;

	/**
     * 交接人工号
	 */
	private String staffBadge;
}
