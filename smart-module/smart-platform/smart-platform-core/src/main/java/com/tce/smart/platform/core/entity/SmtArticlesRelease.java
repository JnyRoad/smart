package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-23 16:24
 */
@Data
@TableName("smt_articles_release")
@EqualsAndHashCode(callSuper = true)
public class SmtArticlesRelease extends Model<SmtArticlesRelease> {
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;
	private Integer parkId;
	/**
	 * 住宿id
	 */
	private Integer dormitoryId;
	private Integer floorId;
	private Integer roomId;
	private Integer bedId;
	private String badge;
	private String name;
	private String phone;
	/**
	 * 物品类型
	 */
	private Integer articlesType;
	private String articlesDesc;
	/**
	 * 提交人
	 */
	private String carrier;
	private Date plannedDepartureTime;
	private String licensePlate;
	private String remarks;
	private Integer status;
	private String approver;
	private Date approveTime;
	private String securityStaff;
	private Date departureTime;
	private String oneImg;
	private String twoImg;
	private String threeImg;
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;
	private String remark;

	/**
	 * 物品放行事项
	 */
	private String releaseItem;
	/**
	 * 流程编号
	 */
	private String processId;
	/**
	 * OA节点
	 */
	private String oaNode;
	/**
	 * 是否返厂
	 */
	private String isBack;
	/**
	 * 返厂时间
	 */
	private Date backTime;
	/**
	 * 返厂保安确认人工号
	 */
	private String guardBadge;
	/**
	 * 保安放行上传图片
	 */
	private String guardOneImg;
	private String guardTwoImg;
	private String guardThreeImg;
}
