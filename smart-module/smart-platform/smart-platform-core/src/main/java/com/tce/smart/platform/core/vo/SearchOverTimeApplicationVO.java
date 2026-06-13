package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 加班申请列表返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchOverTimeApplicationVO extends Model<SearchOverTimeApplicationVO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 申请id
	 */
	private Integer recordId;
	/**
	 * 员工工号
	 */
	private String staffBadge;
	/**
	 * 员工姓名
	 */
	private String staffName;

	/**
	 * 记录类型备注
	 */
	private String recordDesc;

	/**
	 * 流程id
	 */

	private String processId;

	/**
	 * 申请加班时间
	 */
	private String extraworkDate;

	/**
	 * 申请记录时间
	 */
	private Date recordDate;
	/**
	 * 加班时长
	 */
	private String extraworkCount;
	/**
	 * 加班类型说明
	 */
	private String extraworkTypeName;
	/**
	 * 加班类型
	 */
	private Integer workType;

	/**
	 * 加班原因
	 */
	private String cause;

	/**
	 * 班级
	 */
	private Integer workClassCode;

	/**
	 * 班级描述
	 */
	private String workClassCodeDesc;

	/**
	 * bu名称
	 */
	private String compName;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 岗位名称
	 */
	private String jobName;



}
