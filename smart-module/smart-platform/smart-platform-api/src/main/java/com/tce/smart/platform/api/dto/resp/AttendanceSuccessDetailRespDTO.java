package com.tce.smart.platform.api.dto.resp;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 出勤详情
 *
 * @author 梁圆
 * @date 2019-05-09 18:19:00
 */
@Data
public class AttendanceSuccessDetailRespDTO implements Serializable {
	private static final long serialVersionUID = 6627754564897892659L;
	/**
	 * 员工号
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

	private List<String> kqTime;

}
