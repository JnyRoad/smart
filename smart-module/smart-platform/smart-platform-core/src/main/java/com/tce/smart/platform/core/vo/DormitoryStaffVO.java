package com.tce.smart.platform.core.vo;

import java.util.Date;
import java.util.List;

import cn.hutool.core.date.DateTime;
import lombok.Data;

/**
 * 床位及占用员工信息
 *
 * @author QIPEI
 *
 */
@Data
public class DormitoryStaffVO {


	/**
	 * 员工入住表主键
	 */
	private Integer id;
	/*
	 * 员工号
	 */
	private String staffBadge;

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
	 * 床位名称
	 */
	private String bedName;

	/**
	 * 床位删除状态 1.已删除 0.未删除
	 */
	private Integer delFlag;

	/**
	 * 家属
	 */
	private List<String> family;

	/**
	 * 房间Id
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
	private Integer sex;

	/**
	 * 员工姓名
	 */
	private String name;



	/**
	 * 出差/请假备注
	 */
	private String remark;

	/**
	 * 中心
	 */
	private String depAbbr;

	/**
	 * BU
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


	private List<String> jcheId;

	/**
	 * 宿舍类型
	 *
	 */
	private Integer dormitoryTypeId;
	private String dormitoryTypeName;

	/**
	 * 入住时间
	 */

	private Date CreateTime;

	private String jobName;

	/**
	 * 是否是员工0-非员工 1-员工
	 */
	private Integer isStaff;

	private String parkName;

	private Integer parkId;

	private Integer dormitoryId;
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
	 * 失败记录信息
	 */
	private String mark;

	/**
	 * 民族
	 */
	private String nation;

	/**
	 * 户籍
	 */
	private String residentaddress;

	/**
	 * 入职日期
	 */
	private Date joinDate;

	/**
	 * 简单备注
	 */
	private String simpleRemark;

	/**
	 * 离职日期
	 */
	private Date LeaDate;

	private String phone;

	/**
	 * 离职原因
	 */
	private String leaType;

	private String certno;

	/**
	 * 派遣公司
	 */
	private String pqcompany;
}
