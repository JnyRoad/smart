package com.tce.smart.platform.api.dto.resp.leavecount;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:02:12
 */
@Data
public class SettlementInfoDhrRespDTO extends BaseDTO {

private static final long serialVersionUID = -5817398582874184218L;

	@ApiModelProperty("序列号")
	private String num;

	@ApiModelProperty("工号")
    private String badge;

	@ApiModelProperty("姓名")
    private String name;

	@ApiModelProperty("离职时间")
    private String leaveDate;

	@ApiModelProperty("结算时间")
    private String createTime;

	@ApiModelProperty("结算总费用")
	private BigDecimal totalFee;

	@ApiModelProperty("住宿房间产生费用")
	private List<CountRoom> countRoom;

	@Data
	public static class CountRoom{

		@ApiModelProperty("产生费用")
		private BigDecimal fee;

		@ApiModelProperty("离职计算天数")
		private Integer countDays;

		@ApiModelProperty("房间名")
		private String roomInfo;

		@ApiModelProperty("上月结算时间")
		private String preCollect;

		@ApiModelProperty("备注天数")
		private Integer remarkDays;

	}
}
