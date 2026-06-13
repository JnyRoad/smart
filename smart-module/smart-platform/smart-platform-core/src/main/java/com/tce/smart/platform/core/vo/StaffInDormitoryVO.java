package com.tce.smart.platform.core.vo;

import java.util.Date;

import cn.hutool.core.date.DateTime;
import lombok.Data;

/**
 * 内宿员工信息
 * @author 齐佩
 *
 */

@Data
public class StaffInDormitoryVO {

	private Integer id;

	/**
	 * 员工号
	 */
	private String staffBadge;

	/**
	 * 员工姓名
	 */
	private String staffName;

	/**
	 * 员工性别
	 */
	private String sex;

	/**
	 * BU
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

	/**
	 * 入住时间
	 */
	private Date createTime;

	/**
	 * 职层
	 */
	private String jcheName;

	/**
	 * 宿舍类型
	 */
	private String dormitoryTypeName;

	/**
	 * 房间ID
	 */
	private Integer roomId;

	/**
	 * 房间名称
	 */
	private String roomName;

	/**
	 * 所属园区
	 */
	private String parkName;

	/**
	 * 楼栋
	 */
	private String dormitoryName;



	/**
	 * BU
	 */
	private String dorCompName;

	/**
	 * 部门名称
	 */
	private String dorDepName;

	/**
	 * 岗位名称
	 */
	private String dorJobName;

	/**
	 * 床铺编码
	 */
	private Integer bedNumber;

	/**
	 * 入职状态
	 */
	private Integer status;

	/**
	 * 操作人名称
	 */
	private String optUser;
}
