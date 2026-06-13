package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.util.Date;

/**
 * @description: 身份证信息表
 * @date: 2020-11-18 11:20
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_IDCARD_INFO")
@EqualsAndHashCode(callSuper = true)
public class SmtIdCardInfo extends Model<SmtIdCardInfo> {

	/**
	 * 主键ID
	 */
	@TableId(value = "id",type = IdType.INPUT)
	private String id;

	/**
	 * 名称
	 */
	private String name;

	/**
	 * 有效期开始时间
	 */
	private Date validDateStart;

	/**
	 * 有效期结束时间
	 */
	private Date validDateEnd;

	/**
	 * 签证机关
	 */
	private String signOrg;

	/**
	 * 性别 0.男性 1.女性
	 */
	private Integer sex;

	/**
	 * 民族
	 */
	private String nation;

	/**
	 * 出生日期
	 */
	private Date birthday;

	/**
	 * 住址
	 */
	private String address;
}
