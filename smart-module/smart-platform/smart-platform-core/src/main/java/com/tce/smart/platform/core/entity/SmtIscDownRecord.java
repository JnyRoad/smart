package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @program: smart-module
 * @description:
 * @author: Wuling
 * @create: 2021-08-25 14:55
 **/
@Data
@TableName("SMT_ISC_DOWN_RECORD")
@EqualsAndHashCode(callSuper = true)
public class SmtIscDownRecord extends Model<SmtIscDownRecord> {
	/**
	 * 主键
	 */
	@TableId(value = "id",type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 操作类型
	 */
	private Integer action;

	/**
	 * 任务类型
	 */
	private Integer deviceType;

	/**
	 * 开始时间
	 */
	private Date startTime;
	/**
	 * 截止时间
	 */
	private Date overTime;

	/**
	 * 设备标识
	 */
	private String deviceCode;

	/**
	 * 卡片Id
	 */
	private String cardNo;


	/**
	 * 公共字段
	 */
	private String general;

	/**
	 * 图片ID
	 */
	private String imageId;

	/**
	 * 业务类型
	 */
	private Integer serviceType;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 任务ID
	 */
	private Long taskId;
	/**
	 * 0：待处理
	 * 1：已处理
	 * 2：失败
	 * 3：处理中
	 */
	private Integer taskType;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 操作人
	 */
	private String optUser;

	/**
	 * 工号
	 * 访客为身份证号，员工为Null
	 */
	private String badge;

	/**
	 * ISC人员ID
	 */
	private String personId;
}
