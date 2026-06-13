package com.tce.smart.platform.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 车辆信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleBlackVO extends BaseVO {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    private Integer id;
    /**
   * 园区名称
   */
    private String parkName;
    /**
   * 车牌号
   */
    private String vehiclePlate;

	/**
	 * 创建时间
	 */

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private LocalDateTime createTime;
	/**
	 * 原因
	 */
	private String remark;
}
