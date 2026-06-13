package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.tool.enums.DeviceTaskEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Data
@TableName("smt_device_task")
@EqualsAndHashCode(callSuper = true)
public class SmtDeviceTask extends Model<SmtDeviceTask> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
	@TableId(value = "id",type = IdType.AUTO)
    private Integer id;

	/**
	 * 图片ID
	 */
	private String imageId;

	/**
	 * 卡片类型
	 */
	private Integer cardType;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 卡片Id
     */
    private String cardNo;

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
   * 创建时间
   */
    private LocalDateTime createTime;

	/**
	 * 备注
	 */
	private String remark;

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
	 * 修改时间
	 */
	private LocalDateTime updateTime;

	/**
	 * 公共字段
	 */
	private String general;

	/**
	 * 序号
	 */
	private String serialNo;


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

	@TableField(exist = false)
	private Integer parkId;


	public boolean isFail() {
		return !isSuccess();
	}

	public boolean isSuccess(){
		return this.code.equals(DeviceTaskEnum.DEVICE_OK.getCode());
	}
}
