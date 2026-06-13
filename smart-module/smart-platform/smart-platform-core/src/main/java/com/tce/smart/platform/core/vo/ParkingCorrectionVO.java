package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 停车场车位校正表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:31:55
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ParkingCorrectionVO extends Model<ParkingCorrectionVO> {
private static final long serialVersionUID = 1L;

    /**
   * 车位总数量
   */
    private Integer totalCount;
    /**
   * 车位剩余数量
   */
    private Integer freeCount;

}
