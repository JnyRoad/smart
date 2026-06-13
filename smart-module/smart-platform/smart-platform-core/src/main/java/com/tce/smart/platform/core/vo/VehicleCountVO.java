package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车辆信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleCountVO extends Model<VehicleCountVO> {
private static final long serialVersionUID = 1L;
    /**
   * 内部车辆
   */
    private Integer innerTotal;

    /**
   * 外来车辆
   */
    private Integer foreignTotal;

}
