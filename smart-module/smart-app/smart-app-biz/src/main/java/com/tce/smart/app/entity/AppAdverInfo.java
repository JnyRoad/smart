package com.tce.smart.app.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/***
 * description: App广告 <br>
 * date: 2019/12/30 17:45 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
@TableName("app_adver_info")
@EqualsAndHashCode(callSuper = true)
public class AppAdverInfo extends Model<AppAdverInfo> {
	private static final long serialVersionUID = -7140725763844862260L;

	/**
	 * 主键ID
	 */
	@TableId
	private Integer id;

	/**
	 * 图片二进制
	 */
	private byte[] imageBinary;

	/**
	 * 投放位置
	 */
	private String imagePosition;

	/**
	 * 跳转链接
	 */
	private String imageLink;

	/**
	 * 图片排序
	 */
	private String imageOrder;

	/**
	 * 是否置顶（0:否；1:置顶)
	 */
	private String topFlag;

	/**
	 * 发布状态（0:待发布；1:已发布；2:已下线））
	 */
	private String publishFlag;

	/**
	 * 删除状态（0:删除；1:正常）
	 */
	private String delFlag;

	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 修改时间
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;

}
