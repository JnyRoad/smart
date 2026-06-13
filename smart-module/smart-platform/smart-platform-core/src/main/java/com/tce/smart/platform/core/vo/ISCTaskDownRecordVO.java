package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 任务下发记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ISCTaskDownRecordVO extends Model<ISCTaskDownRecordVO> {
private static final long serialVersionUID = 1L;


	private Long ID;
	/**
	 * 园区ID
	 */
	private Integer parkId;

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
   * 创建时间
   */
    private LocalDateTime createTime;

	/**
	 * 任务ID
	 */
	private String personName;

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

	/**
	 * 工号
	 */
	private String badge;

	/**
	 * 开始时间
	 */
	private LocalDateTime startTime;

	/**
	 * 设备
	 */
	private String deviceName;

	/**
	 * 结束时间
	 */
	private LocalDateTime overTime;

	/**
	 * 任务列表id
	 */
	private Integer taskId;

	/**
	 * 设备类型
	 */
	private Integer deviceType;

	/**
	 * 区域名称
	 */
	private String areaName;

	private Integer action;

	private String actionDesc;

	private String optUser;

}
