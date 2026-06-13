package com.tce.smart.temporary.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * EHR员工头像
 *
 * @author mkwu
 * @date 2019-07-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ePhoto")
public class EPhoto extends Model<EPhoto> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -397104911174855040L;

	@TableField("EID")
	private Integer EID;

	@TableField("Photo")
	private byte[] Photo;

	@TableField("IsDisPose")
	private String IsDisPose;

}
