package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.util.Date;

/**
 * @description: 员工申诉记录表
 * @date: 2020-07-23 13:51
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_STAFF_APPEAL")
@EqualsAndHashCode(callSuper = true)
public class SmtStaffAppeal extends Model<SmtStaffAppeal> {

	private static final long serialVersionUID = 874046663241959531L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 员工编号
	 */
	private String staffBadge;

	/**
	 * 园区Id
	 */
	private Integer parkId;

	/**
	 * 申诉类型
	 */
	private Integer appealType;

	/**
	 * 发生时间
	 */
	private Date happenTime;

	/**
	 * 文字描述
	 */
	private String appealDesc;

	/**
	 * 图片描述列表
	 */
	private String appealImgs;

	/**
	 * 状态
	 */
	private Integer status;

	/**
	 * 是否转交TA人
	 */
	private Integer ischange;

	/**
	 * 回复人名称
	 */
	private String replyName;

	/**
	 * 回复内容
	 */
	private String replyDesc;

	/**
	 * 回复时间
	 */
	private Date replyTime;

	/**
	 * 添加时间
	 */
	private Date createTime;

	/**
	 * 更新时间
	 */
	private Date updateTime;
}
