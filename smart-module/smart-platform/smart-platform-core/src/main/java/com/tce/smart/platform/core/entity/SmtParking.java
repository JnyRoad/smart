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
 * 停车场管理表
 *
 * @author wangyanyong
 * @date 2019-04-13 18:17:35
 */
@Data
@TableName("smt_parking")
@EqualsAndHashCode(callSuper = true)
public class SmtParking extends Model<SmtParking> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.UUID)
	private String id;

	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 停车场名称
	 */
	private String name;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 总车位
	 */
	private Integer totalCount;

}
