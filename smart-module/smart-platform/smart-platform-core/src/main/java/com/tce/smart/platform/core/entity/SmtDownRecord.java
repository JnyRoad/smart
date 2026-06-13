package com.tce.smart.platform.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 下发记录表
 *
 * @author梁圆
 * @date 2019-05-29 11:34:58
 */
@Data
@TableName("smt_down_record")
@EqualsAndHashCode(callSuper = true)
public class SmtDownRecord extends Model<SmtDownRecord> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@TableId(value = "id",type = IdType.AUTO)
	private Integer id;
	/**
	 * 设备id
	 */
	private String deviceId;
	/**
	 * 类型1访客,2员工车辆,3物流车辆 4面试人员,5复试人员6待入职人员
	 */
	private Integer type;
	/**
	 * 下发的id
	 */
	private Long downId;
	/**
	 * 状态 1成功 2失败
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 类型1下发,2删除
	 */
	private Integer operationType;
}
