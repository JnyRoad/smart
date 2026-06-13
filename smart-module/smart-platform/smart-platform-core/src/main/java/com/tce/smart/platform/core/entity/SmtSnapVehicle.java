package com.tce.smart.platform.core.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 车辆抓拍记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:20
 */
@Data
@TableName("smt_snap_vehicle")
@EqualsAndHashCode(callSuper = true)
public class SmtSnapVehicle extends Model<SmtSnapVehicle> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
   * 园区主键
   */
    private Integer parkId;
    /**
   * 设备ID
   */
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;
    /**
   * 设备通道号
   */
    private Integer channelNo;
    /**
   * 区域ID
   */
    private Integer areaId;
    /**
   * 区域名称
   */
    private String areaName;
    /**
   * 车牌号
   */
    @NotBlank(message = "车牌号不能为空")
    private String vehiclePlate;
    /**
   * 车辆品牌
   */
//    @NotBlank(message = "车辆品牌不能为空")
    private String vehicleBrand;
    /**
   * 车辆颜色
   */
    @NotNull(message = "车辆颜色不可空")
    private Integer vehicleColor;
    /**
   * 车牌颜色
   */
    private Integer plateColor;
    /**
   * 车辆类型
   */
    private Integer vehicleType;
    /**
   * 车辆归属分类：1:园区车辆；2：员工车辆；3：访客车辆；4：物流车辆；5:非员工车辆
   */
    private Integer vehicleAscription;
    /**
   * 车辆图片ID
   */
    private String snapPhotoId;
    /**
   * 进出类型：1-进；2-出；
   */
    @NotNull(message = "进出类型不可空")
    private Integer eventType;
    /**
   * 通过时间
   */
    @NotNull(message = "通过时间不可空")
    private Date snapTime;
    /**
   * 车主ID,如果是物流车测试物流车预约ID
   */
    private Long driverId;
    /**
   * 车主名称
   */
    private String driverName;
    /**
   * 1:员工；2：访客；3：物流车车主；4：非员工
   */
    private Integer driverType;
    /**
   * 车主手机号
   */
    private String driverPhone;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

	/**
	 * 是否放行 0-未放行;1-放行;2-未知
	 */
	private Integer letPass;

	/**
	 * 权限 0:没有；1：有
	 */
	private Integer authority;

	/**
	 * 卡片号
	 */
	private String cardNo;
}
