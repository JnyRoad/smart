package com.tce.smart.platform.core.entity;


import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客黑名单
 * @author QIPEI
 * @date 2019/10/21
 */

@Data
@TableName("smt_black_visitor")
@EqualsAndHashCode(callSuper = true)
public class SmtBlackVisitor extends Model<SmtBlackVisitor> {


	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 身份证号
	 */
	private String cardNo;

	/**
	 * 黑名单姓名
	 */
	private String personName;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 园区id
	 */
	private Integer parkId;

	/**
	 * 创建人
	 */
	private String createUser;

	/**
	 * 原因
	 */
	private String reason;

}
