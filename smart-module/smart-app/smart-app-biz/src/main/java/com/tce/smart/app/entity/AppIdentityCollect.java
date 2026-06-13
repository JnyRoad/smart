package com.tce.smart.app.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 身份证信息采集表
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:49:29
 */
@Data
@TableName("app_identity_collect")
@EqualsAndHashCode(callSuper = true)
public class AppIdentityCollect extends Model<AppIdentityCollect> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 1252144879532699947L;
	/**
	 * 主键ID
	 */
	@TableId
	private Integer id;
	/**
	 * 员工号
	 */
	private String staffId;
	/**
	 * 身份证号
	 */
	private String identityCard;
	/**
	 * 姓名
	 */
	private String name;
	/**
	 * 性别
	 */
	private String gender;
	/**
	 * 民族
	 */
	private String ethnicity;
	/**
	 * 出生日期
	 */
	private String birthday;
	/**
	 * 家庭住址
	 */
	private String address;
	/**
	 * 签发机关
	 */
	private String signOrg;
	/**
	 * 签发日期
	 */
	private LocalDate signDate;
	/**
	 * 有效期至
	 */
	private LocalDate validityEndDate;
	/**
	 * 有效期限
	 */
	private String validityDate;
	/**
	 * 正面照片(base64字符串)
	 */
	private String frontImage;
	/**
	 * 背面照片(base64字符串)
	 */
	private String backImage;
	/**
	 * 人脸照片(base64字符串)
	 */
	private String faceImage;
	/**
	 * 采集状态，0-未使用，1-已使用
	 */
	private String collectFlag;
	/**
	 * 头像同步到裕同状态(-1:同步失败 0:未同步；1:同步成功)
	 */
	private String photoSyncFlag;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;
}
