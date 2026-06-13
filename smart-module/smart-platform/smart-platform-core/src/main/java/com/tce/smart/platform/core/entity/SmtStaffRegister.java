package com.tce.smart.platform.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工登记
 * @author QIPEI
 *
 */
@Data
public class SmtStaffRegister extends Model<SmtStaffRegister>  {

	private static final long serialVersionUID = 1L;



	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 身份证号
	 */
	private String certno;
	/**
	 * 0-男 1-女
	 */
	private Integer sex;
	/**
	 * 2位数字，根据身份证号码计算年龄
	 */
	private Integer age;
	/**
	 * 出生年月
	 */
	private String birth;

	/**
	 * 家庭住址
	 */
	private String homeAddress;

	/**
	 * 员工状态 0-已登记  1-已入职  2-已删除
	 */
	private Integer status;

	/**
	 * 登记录入时间
	 */
	private Date createTime;


	/**
	 * 签证机关
	 */
	private String police;

	/**
	 * 证件有效开始时间
	 */
	private String validDateFm;


	/**
	 * 证件有效结束时间
	 */
	private String validDate;

	/**
	 * 民族
	 */
	private String nation;

}
