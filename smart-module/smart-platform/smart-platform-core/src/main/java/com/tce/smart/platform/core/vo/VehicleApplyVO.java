package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 我的车辆的入园列表
 * @author dell
 *
 */
@Data
public class VehicleApplyVO extends Model<VehicleApplyVO> {


	private Integer id;

	private String parkName;

	private Integer parkId;

	private Integer status;

	private String vehiclePlate;

	private String reason;

}
