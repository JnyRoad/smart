package com.tce.smart.platform.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("smt_ehr_to_staff")
@EqualsAndHashCode(callSuper = true)
public class SmtEhrToStaff extends Model<SmtEhrToStaff> {

	private static final long serialVersionUID = 1L;
	/**
	*
	*/
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 员工姓名
	 */
	private String name;
	/**
	 * 员工工号
	 */
	private String badge;
	/**
	 * 岗位ID
	 */
	private String jobId;
	/**
	 * 岗位名称
	 */
	private String jobName;
	/**
	 * BUId
	 */
	private String compId;
	/**
	 * buname
	 */
	private String compName;
	/**
	 * 部门ID
	 */
	private String depId;
	/**
	 * 部门名称
	 */
	private String depName;
	/**
	 * 职层ID
	 */
	private String jcheId;
	/**
	 * 职层名称
	 */
	private String jcheName;

	/**
	 * 创建时间
	 */
	private Date createTime;
}
