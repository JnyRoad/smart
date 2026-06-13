package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @program: smart-module
 * @description:
 * @author: Wuling
 * @create: 2021-08-25 14:53
 **/
@Data
@TableName("SMT_ISC_DEVICE_TASK")
@EqualsAndHashCode(callSuper = true)
public class SmtIscDeviceTask extends Model<SmtIscDeviceTask> {
	/**
	 * 主键
	 */
	@TableId(value = "id",type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 1:下发；2：删除；11:延迟下发；12:延迟删除
	 */
	private Integer action;

	/**
	 * 任务状态：
	 * 0：初始化
	 * 1：成功
	 * 2：失败
	 * 3：处理中
	 * 4：已取消
	 */
	private Integer status;

	/**
	 * 1：卡片
	 * 2：车辆
	 */
	private Integer deviceType;

	/**
	 * 开始时间（秒）
	 */
	private Long startTime;

	/**
	 * 截止时间（秒）
	 */
	private Long overTime;

	/**
	 * 设备编码
	 */
	private String deviceCode;

	/**
	 * 卡片Id
	 */
	private String cardNo;

	/**
	 * 异常码
	 */
	private Integer code;

	/**
	 * 请求耗时 毫秒
	 */
	private Long consume;

	/**
	 * 重复操作的次数
	 */
	private Integer times;

	/**
	 * 公共字段
	 */
	private String general;

	/**
	 * 图片ID
	 */
	private String imageId;

	/**
	 * 人脸下发记录的业务类型：
	 * 1、导入员工照片
	 * 2、APP信息完善
	 * 3、访客预约
	 * 4、员工扫码登记
	 *
	 * 车辆下发记录的业务类型：
	 * 1、员工车辆
	 * 2、公司车辆
	 * 3、非员工车辆
	 * 4、访客预约
	 * 5、物流车预约
	 */
	private Integer serviceType;


	/**
	 * 备注
	 */
	private String remark;

	/**
	 * ISC任务ID
	 */
	private String iscTaskId;


	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;


	/**
	 * 修改时间
	 */
	private LocalDateTime updateTime;


	@TableField(exist = false)
	private Integer parkId;

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
