package com.tce.smart.platform.core.vo;

import java.util.Date;

import cn.hutool.core.date.DateTime;
import lombok.Data;

/**
 * 员工住宿历史信息查询结果
 *
 * @author QIPEI
 *
 */
@Data
public class DormitoryStaffHistoryVO {


	/**
	 * 员工入住表主键
	 */
	private Integer id;
	/*
	 * 员工号
	 */
	private String staffBadge;

	private String staffName;

	/**
	 * 入住员工在职状态 1-在职 0-离职
	 */
	private Integer status;


	private Integer bedId;
	/**
	 * 床铺编码
	 */
	private Integer bedNumber;

	/**
	 * 房间ID
	 */
	private Integer roomId;

	/**
	 * 房间
	 */
	private String roomName;

	/**
	 *房间的入住性别
	 */
	private Integer roomSex;

	/**
	 * 男/女，应聘填写时OCR识别出来身份证号码，最后一位顺奇数分配为男性，偶数分配为女性
	 */
	private Integer staffSex;



	/**
	 *BU
	 */
	private String compName;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 职层名称
	 */
	private String jcheName;


	private String jcheId;

	/**
	 * 宿舍类型
	 *
	 */
	private String dormitoryTypeName;


	/**
	 * 入住时间
	 */
	private Date inTime;

	/**
	 * 操作时间
	 */

	private Date createTime;

	private Date time;

	private String jobName;


	private Integer type;

	/**
	 * 所属园区
	 */
	private String parkName;

	/**
	 * 宿舍楼
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
	 * 离职原因
	 */
	private String leaType;

	private String optUser;

	/**
	 * 入住操作人员
	 */
	private String inOptUser;

	/**
	 * 入住操作时间
	 */
	private Date inCreateTime;
}
