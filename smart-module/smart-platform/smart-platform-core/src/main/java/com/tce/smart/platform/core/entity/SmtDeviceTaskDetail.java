package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 设备下发任务记录表
 *
 * @author
 * @date 2019-04-15 15:09:27
 */
@Data
@TableName("smt_device_task_detail")
@EqualsAndHashCode(callSuper = true)
@Builder
public class SmtDeviceTaskDetail extends Model<SmtDeviceTaskDetail> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;

	/**
	 * 任务下发记录id
	 * (目前为生成时随机，后续有需求变动可以根据需求改为一张表记录)
	 */
	private String taskListId;

	/**
	 * 任务ID
	 */
	private String taskId;


	/**
	 * 1:下发；2：删除
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
   * 创建时间
   */
    private LocalDateTime createTime;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 工号
	 */
	private String badge;

	/**
	 * 姓名
	 */
	private String name;


	/**
	 * 设备名
	 */
	private String deviceName;

	/**
	 * 区域名
	 */
	private String areaName;

}
