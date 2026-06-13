package com.tce.smart.platform.core.dto;

import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 设备绑定车辆信息
 * @author Administrator
 *
 */
@Data
public class DeviceVehicleDTO  extends Model<DeviceVehicleDTO> {

    private static final long serialVersionUID = 1L;

    private String cardNo;

    private String plate;

    private String personName;

    private Date createTime;

	private Date overTime;

	private String serialNo;
}
