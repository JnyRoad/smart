package com.tce.smart.platform.core.dto;


import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchSmtVisitorDTO extends Model<SearchSmtVisitorDTO> {
private static final long serialVersionUID = 1L;


    /**
   *
   */
    private String visitorName;

    /**
   *
   */
    private String visitorPhone;
    /**
   *
   */
    private String vehiclePlate;
    /**
   *
   */
    private String company;
    /**
   * 来访状态 0:已通过1:已驳回2:未处理3:已到达4超时未到
   */
    private Integer status;
    /**
   *  0:没有,1:有车
   */
    private Integer isVehicle;

    /**
   *
   */
    private String startTime;
    /**
   *
   */
    private String endTime;

    /**
     * 被访人姓名
     */
    private String receptionistName;

    /**
     * 被访人Bu
     */
    private String compId;

    /**
     * 被访人部门id
     */
    private String depId;

    /**
     * 访客原因
     */
    private Integer cause;

    private Integer parkId;


    /**
     * 身份证号
     */
    private String certNo;

    private Integer applyType;

}
