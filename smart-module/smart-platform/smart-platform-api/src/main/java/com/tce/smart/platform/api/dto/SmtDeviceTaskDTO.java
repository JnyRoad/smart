package com.tce.smart.platform.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Data
public class SmtDeviceTaskDTO implements Serializable {
private static final long serialVersionUID = -8299216129047065383L;

    /**
   * 主键
   */
    private Integer id;
    /**
     * 下方内容json（注意属性名称，必须和brige实体类一样）
     */
    private String content;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 卡片Id
     */
    private String cardNo;
    /**
   * 1：下发；2：删除
   */
    private Integer action;
    /**
     * 固定值0
     * 2：异常
     * 1：下发成功；
     * 0：待处理；
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
    private Date createTime;

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
	private Date updateTime;

	/**
	 * 公共字段
	 */
	@NotBlank(message="查询参数不能为空")
	@NotNull(message="查询参数不能为空")
	private String general;

	private Integer parkId;
}
