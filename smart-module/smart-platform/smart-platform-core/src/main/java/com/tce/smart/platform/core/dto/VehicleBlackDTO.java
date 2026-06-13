package com.tce.smart.platform.core.dto;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 车辆信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleBlackDTO extends BaseVO {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    private List<Integer> ids;
}
