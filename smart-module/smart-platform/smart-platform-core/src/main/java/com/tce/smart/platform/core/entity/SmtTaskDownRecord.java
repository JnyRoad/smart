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
 * 任务下发记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Data
@TableName("smt_task_down_record")
@EqualsAndHashCode(callSuper = true)
public class SmtTaskDownRecord extends Model<SmtTaskDownRecord> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
	@TableId(value = "id",type = IdType.AUTO)
    private Integer id;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 1：行人
	 * 2：车辆
	 */
	private Integer deviceType;

	/**
	 * 1：下发；2：删除
	 */
	private Integer action;


	/**
	 * 公共字段
	 */
	private String general;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 卡片Id
     */
    private String cardNo;

    /**
     * 图片ID
     */
    private String imageId;

    /**
     * 开始时间
     */
    private Date startTime;
    /**
   * 截止时间
   */
    private Date overTime;

    /**
   * 创建时间
   */
    private LocalDateTime createTime;

	/**
	 * 任务ID
	 */
	private Integer taskId;

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
	 * 0：待处理
	 * 1：已处理
	 * 2：失败
	 * 3：处理中
	 */
	private Integer taskType;

	/**
	 * 备注
	 */
	private String remark;

}
