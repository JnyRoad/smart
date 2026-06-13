package com.tce.smart.platform.api.dto.resp.leavecount;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:02:12
 */
@Data
public class SettlementInfoRespDTO extends BaseDTO {

	@ApiModelProperty("ID")
    private Long id;

	@ApiModelProperty("园区名")
	private String parkName;

	@ApiModelProperty("工号")
    private String badge;

	@ApiModelProperty("姓名")
    private String name;

	@ApiModelProperty("BU")
    private String bu;

	@ApiModelProperty("部门")
    private String dept;

	@ApiModelProperty("结算费用")
    private BigDecimal fee;

	@ApiModelProperty("离职时间")
    private LocalDateTime leaveDate;

	@ApiModelProperty("结算状态：0、未结算，1、已结算")
    private Integer status;

	@ApiModelProperty("结算时间")
    private LocalDateTime createTime;

	@ApiModelProperty("退宿时间")
    private LocalDateTime quitDate;

	@ApiModelProperty("上月抄表时间")
    private LocalDateTime preCollect;

	@ApiModelProperty("离职天数")
    private Integer leaveDays;

}
