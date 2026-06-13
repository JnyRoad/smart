package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 应聘表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:24
 */
@Data
@TableName("smt_application")
@EqualsAndHashCode(callSuper = true)
public class SmtApplication extends Model<SmtApplication> {
	private static final long serialVersionUID = 1L;

	/**
	*
	*/
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 对应招聘id
	 */
	@NotBlank(message="所属应聘id不能为空")
	private Integer recruitId;
	/**
	 * 所属园区id
	 */
	@NotBlank(message="所属园区id不能为空")
	private Integer parkId;
	/**
	 * 应聘者姓名
	 */
	@NotBlank(message="应聘者姓名不能为空")
	private String name;
	/**
	 * 身份证号
	 */
	@NotBlank(message="应聘者身份证号不能为空")
	private String certno;
	/**
	 * 男/女，应聘填写时OCR识别出来身份证号码，最后一位顺奇数分配为男性，偶数分配为女性
	 */
	@NotBlank(message="应聘者性别不能为空")
	private Integer sex;
	/**
	 * 2位数字，根据身份证号码计算年龄
	 */
	@NotBlank(message="应聘者年龄不能为空")
	private Integer age;
	/**
	 * 民族
	 */
	@NotBlank(message="应聘者民族不能为空")
	private String nation;
	/**
	 * 出生年月日
	 */
	@NotBlank(message="应聘者生日不能为空")
	private String birth;
	/**
	 * 电话
	 */
	@NotBlank(message="应聘者电话不能为空")
	private String phone;
	/**
	 * 微信号
	 */
	private String wechat;

	/**
	 * 家庭住址
	 */
	@NotBlank(message="应聘者家庭住址不能为空")
	private String homeAddress;
	/**
	* 居住地址
	*/
	private String liveAddress;
	/**
	 * 证件照片id
	 */
	@NotBlank(message="应聘者的证件照不能为空")
	private String certnoPicId;
	/**
	 * 人脸照片URl
	 */
	@NotBlank(message="应聘者的人脸照不能为空")
	private String facePicId;
	/**
	 * 应聘状态
		0-已投递1-已拒绝 2-已邀请3-待入职/4待复试，暂定/5-已入职
		默认是0
	 */
	private Integer status;


	/**
	 * 面试时间
	 */
	private Date interviewTime;
	/**
	* 创建时间
	*/
	@Getter
	@Setter
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date createTime;

	/**
	 * 投递时间
	 */
	private Date applyDate;
	/**
	 * 是否删除;0：未删；1：已删，默认是0
	 */
	private Integer isDelete;


	/**
	 * 拒绝原因
	 */
	private String refuseReason;

	/**
	 * 签证机关
	 */
	private String police;

	/**
	 * 证件有效开始时间
	 */
	private Date validDateFm;


	/**
	 * 证件有效结束时间
	 */
	private Date validDate;


	/**
	 * 证件有效结束时间 存长期
	 */
	private String validDateChar;

	/**
	 * 婚姻状况0-未婚 1-已婚 其他-未知
	 */
	private Integer maritalStatus;
}
