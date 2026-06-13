package com.tce.smart.platform.api.dto.resp.leavecount;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:02:12
 */
@Data
public class SettlementInfoPageRespDTO extends BaseDTO {

private static final long serialVersionUID = 6311500855664915455L;

	@ApiModelProperty("id")
    private Long id;

	@ApiModelProperty("园区id")
    private Integer parkId;

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

	@ApiModelProperty("结算状态")
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
