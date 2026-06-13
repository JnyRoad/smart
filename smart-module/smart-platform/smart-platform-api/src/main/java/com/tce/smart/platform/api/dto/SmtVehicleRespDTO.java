package com.tce.smart.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 * 车辆信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
@Data
public class SmtVehicleRespDTO extends BaseVO {
private static final long serialVersionUID = 7238053474424835121L;

    /**
   * 主键
   */
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long id;
    /**
   * 园区ID
   */
    private Integer parkId;
    /**
   * 车牌号
   */
    @NotBlank(message = "车牌号不能为空")
    @Pattern(regexp = "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$",message = "车牌号格式不正确")
    private String vehiclePlate;
    /**
   * 车辆品牌
   */
    @NotBlank(message = "车辆品牌不能为空")
    private String vehicleBrand;
    /**
   * 车辆颜色
   */
    @NotNull(message = "车辆颜色不能为空")
    private Integer vehicleColor;
    /**
   * 车辆类型:(区分轿车、货车等)
   */
    @NotNull(message = "车辆类型不能为空")
    private Integer vehicleType;
    /**
   * 驾驶证图片ID
   */
//    @NotBlank(message = "驾驶证图片ID不能为空")
    private String driverLicenseId;
    /**
   * 行驶证图片ID
   */
//    @NotBlank(message = "行驶证图片ID不能为空")
    private String drivinglLicenseId;
    /**
   * 车辆归属分类：0:园区车辆；1：员工车辆；2：访客车辆；3：物流车辆 ;4:非员工车辆
   */
//    @NotNull(message = "车辆归属不能为空")
//    @Range(min = 0, max = 3, message = "车辆归属分类：0:园区车辆；1：员工车辆；2：访客车辆；3：物流车辆")
    private Integer vehicleAscription;
    /**
   * 是否删除：0未删；1：删除
   */
    private Integer isDelete;
    /**
   * 创建时间
   */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    private String reason;

}
