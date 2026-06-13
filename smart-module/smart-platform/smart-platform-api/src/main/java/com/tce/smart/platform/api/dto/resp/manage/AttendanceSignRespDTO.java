package com.tce.smart.platform.api.dto.resp.manage;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 考勤确认列表
 * @author Lenovo
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AttendanceSignRespDTO extends Model<AttendanceSignRespDTO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 状态
	 */
	private Integer id;

	/**
	 * 员工号
	 */
	private String badge;

	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * bu
	 */
	private String compName;

	/**
	 * 考勤月份
	 */
	private String checkDate;

	/**
	 * 创建时间
	 */
	private Date createTime;

	private String parkName;

	private Integer signStatus;

	private String signStatusDesc;

	private Integer countNotSign;

	private Integer countSign;

	private Integer isObjection;

	private String isObjectionDesc;

	private String objection;

}
