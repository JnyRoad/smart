package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 设备标签表
 *
 * @author sunfujian
 * @date 2021-7-28 18:40
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_DEVICE_TAG")
@EqualsAndHashCode(callSuper = true)
public class SmtDeviceTag extends Model<SmtDeviceTag>{
	private static final long serialVersionUID = 1L;

	/**
	 * 标签ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;
	/**
	 * 标签名称
	 */
	@NotBlank(message = "标签名称")
	private String tagName;

	/**
	 * 创建时间
	 */
	private Date createTime;
}
