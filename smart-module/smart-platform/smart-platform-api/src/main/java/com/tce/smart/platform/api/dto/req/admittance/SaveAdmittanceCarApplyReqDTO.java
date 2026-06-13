package com.tce.smart.platform.api.dto.req.admittance;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入厂申请预约表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:45
 */
@Data
public class SaveAdmittanceCarApplyReqDTO implements Serializable {

private static final long serialVersionUID = 1L;

    /**
   * 园区ID
   */
	@ApiModelProperty("园区ID")
    private Integer parkId;
    /**
   * 访客姓名
   */
	@ApiModelProperty("访客姓名")
    private String visitorName;
    /**
   * 访客手机号
   */
	@ApiModelProperty("访客手机号")
    private String visitorPhone;
    /**
   * 开始时间
   */
	@ApiModelProperty("开始时间")
    private LocalDateTime startTime;
    /**
   * 备注说明
   */
	@ApiModelProperty("备注说明")
    private String remark;
    /**
   * 参观单位
   */
	@ApiModelProperty("参观单位")
    private String company;
    /**
   * 来访事由
   */
	@ApiModelProperty("来访事由")
    private Integer cause;

	@ApiModelProperty("车辆信息")
	List<AdmittanceVehicleReqDTO> vehicleList;

	@ApiModelProperty("用户微信unionId")
	private String unionId;


}
