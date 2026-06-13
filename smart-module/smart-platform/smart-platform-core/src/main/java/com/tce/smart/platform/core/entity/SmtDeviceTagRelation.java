package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备标签关联表
 * @author sunfujian
 * @date 2021/7/28 18:45
 */
@Data
@TableName("smt_device_tag_relation")
@EqualsAndHashCode(callSuper = true)
public class SmtDeviceTagRelation extends Model<SmtDeviceTagRelation>{

	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 设备ID
	 */
	private String deviceId;
	/**
	 * 标签ID
	 */
	private Long tagId;
}
