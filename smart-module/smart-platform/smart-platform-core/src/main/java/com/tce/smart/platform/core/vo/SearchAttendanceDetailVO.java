package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 出勤详情
 *
 * @author 梁圆
 * @date 2019-05-09 18:19:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchAttendanceDetailVO extends Model<SearchAttendanceDetailVO> {
	private static final long serialVersionUID = 1L;
	/**
	 * 员工id
	 */
	private String employeeBadge;
	/**
	 * 员工姓名
	 */
	private String employeeName;
	/**
	 * 班次描述
	 */
	private String classDesc;
    /**
     *出勤日期信息
     */
	private String dateInfo;

	/**
	 * 星期信息
	 */
	private String weekInfo;
	/**
	 * 打卡次数
	 */
	private String totalPunchCount;
	/**
	 * 总工作时长（单位:小时）
	 */
	private String totalHourCount;
	/**
	 * 总工作时长（单位:分钟）
	 */
	private String totalMinCount;
		/**
	 * 2入
	 */
	private String secondEnter;
	/**
	 * 2出
	 */
	private String secondOut;
	/**
	 * 4入
	 */
	private String fourthEnter;
	/**
	 * 4出
	 */
	private String fourthOut;
	/**
	 * 5入
	 */
	private String fifthEnter;
	/**
	 * 5出
	 */
	private String fifthOut;

//	@Builder
//	public SearchAttendanceDetailVO(String employeeId, String employeeName, String classDesc, String dateInfo,
//			String weekInfo, Integer totalPunchCount, Integer totalHourCount, Integer totalMinCount, String secondEnter,
//			String secondOut, String fourthEnter, String fourthOut, String fifthEnter, String fifthOut) {
//		this.employeeId = employeeId;
//		this.employeeName = employeeName;
//		this.classDesc = classDesc;
//		this.dateInfo = dateInfo;
//		this.weekInfo = weekInfo;
//		this.totalPunchCount = totalPunchCount;
//		this.totalHourCount = totalHourCount;
//		this.totalMinCount = totalMinCount;
//		this.secondEnter = secondEnter;
//		this.secondOut = secondOut;
//		this.fourthEnter = fourthEnter;
//		this.fourthOut = fourthOut;
//		this.fifthEnter = fifthEnter;
//		this.fifthOut = fifthOut;
//	}
//
//

}
