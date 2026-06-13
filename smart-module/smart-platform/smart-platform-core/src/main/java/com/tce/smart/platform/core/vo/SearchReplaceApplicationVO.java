package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 补卡申请列表返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchReplaceApplicationVO extends Model<SearchReplaceApplicationVO> {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Integer recordId;

	/**
	 * 员工号
	 */
	private String staffBadge;
	/**
	 * 员工姓名
	 */
	private String staffName;
	/**
	 * 流程id
	 */
	private String processId;
	/**
	 * 记录备注
	 */
	private String recordDesc;
	/**
	 * 补卡时间
	 */
	private String patchDate;
	/**
	 * 补卡原因ID
	 */
	private Integer cause;
	/**
	 * 补卡原因
	 */
	private String patchReasonDesc;
	/**
	 * bu名称
	 */
	private String buName;
	/**
	 * 班次描述
	 */
	private String classDesc;
	/**
	 * 缺卡次数
	 */
	private Integer missPatchCount;

	/**
	 * 记录时间
	 */
	private Date recordDate;

	/**
	 * 考勤月份
	 */
	private String workMonth;

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
