package com.tce.smart.platform.core.vo;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车辆抓拍记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:20
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SnapVehicleVO extends BaseVO {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    private Long id;

    /**
   * 车牌号
   */
    private String vehiclePlate;

    /**
   * 事件类型：1-进；2-出；
   */
    private String eventType;
    /**
   * 通过时间
   */
    private String snapTime;
    /**
   * 车主名称
   */
    private String driverName;

    /**
   * 车主手机号
   */
    private String driverPhone;

    private String parkName;

}
