package com.tce.smart.platform.core.vo;

import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;

/**
 * 员工表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:42
 */
@Data
public class SmtStaffVO extends Model<SmtStaffVO> {
	private static final long serialVersionUID = 1L;

	/**
	*
	*/
	private String id;
	/**
	 * 员工所属园区id
	 */
	private Integer parkId;
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
	 * 福利层次
	 */
	private String welfareLevel;
	/**
	 * 身份证号
	 */
	private String certno;
	/**
	 * 男/女，应聘填写时OCR识别出来身份证号码，最后一位顺奇数分配为男性，偶数分配为女性
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
	 * 电话
	 */
	private String phone;
	/**
	 * 微信号
	 */
	private String wechat;
	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 家庭住址
	 */
	private String homeAddress;
	/**
	 * 居住地址
	 */
	private String liveAddress;
	/**
	 * 证件照片id
	 */
	private String certnoPicId;
	/**
	 * 人脸照片id
	 */
	private String facePicId;
	/**
	 * 员工状态 0-已离职 1-在职
	 */
	private Integer status;
	/**
	 * 入职时间
	 */
	private Date createTime;


}
