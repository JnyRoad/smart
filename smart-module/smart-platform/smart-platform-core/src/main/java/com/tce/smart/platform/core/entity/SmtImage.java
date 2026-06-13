package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;

/***
 * description: 图片存储 <br>
 * date: 2019/12/11 9:23 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
@TableName("smt_image")
public class SmtImage extends Model<SmtImage> {
	private static final long serialVersionUID = -2078184057118831374L;
	/**
	 * 主键ID
	 */
	@TableId(value = "ID", type = IdType.AUTO)
	private Integer id;
	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 图片编码
	 */
	private String imageCode;
	/**
	 * 图片二进制
	 */
	private byte[] image;/**
	 * 缩略图
	 */
	private byte[] imageSmall;
	/**
	 * 图片类型
	 */
	private Integer imageType;
	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;
	/**
	 * 更新时间
	 */
	@TableField(fill = FieldFill.UPDATE)
	private LocalDateTime updateTime;
}
