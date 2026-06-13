package com.tce.smart.platform.core.entity.admittance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 入厂申请预约车辆表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:05
 */
@Data
@TableName("smt_admittance_vehicle")
@EqualsAndHashCode(callSuper = true)
public class SmtAdmittanceVehicle extends Model<SmtAdmittanceVehicle> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(value = "id", type = IdType.ID_WORKER)
    private Long id;
    /**
   * 预约ID
   */
    private Long visitorId;
    /**
   * 车牌号
   */
    private String plate;
    /**
   * 司机姓名
   */
    private String name;
    /**
   * 司机籍贯
   */
    private String nativePlace;
    /**
   * 驾驶证号
   */
    private String licenseNo;
    /**
   * 紧急联系人
   */
    private String emergencyName;
    /**
   * 紧急联系人联络方式
   */
    private String emergencyPhone;
    /**
   * 车型
   */
    private String modle;
    /**
   * 颜色
   */
    private Integer colour;
    /**
   * 相关证件图片
   */
    private String certImg;
    /**
   * 证件类型
   */
    private Integer certType;

	/**
	 * 车辆类型
	 */
	private Integer vehicleType;

}
