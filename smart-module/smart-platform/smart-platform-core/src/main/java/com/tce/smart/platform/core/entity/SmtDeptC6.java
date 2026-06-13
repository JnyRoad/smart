package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

/**
 * @description: C6部门
 * @date: 2021-01-19
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_DEPT_C6")
@EqualsAndHashCode(callSuper = true)
public class SmtDeptC6 extends Model<SmtDeptC6> {

	/**
	 * C6部门编号
	 */
	private String c6DptNo;

	/**
	 * 上级部门编号
	 */
	private String parentC6No;

	/**
	 * 园区ID
	 */
	private String parkId;

	/**
	 * 部门名
	 */
	private String deptName;
}
