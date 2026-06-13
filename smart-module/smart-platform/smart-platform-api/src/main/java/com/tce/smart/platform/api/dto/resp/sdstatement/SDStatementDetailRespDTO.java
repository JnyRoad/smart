package com.tce.smart.platform.api.dto.resp.sdstatement;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @description: 水电结算详情实体类
 * @date: 2020-07-24 18:11
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SDStatementDetailRespDTO implements Serializable {

	private static final long serialVersionUID = 816960693265687353L;

	@ApiModelProperty("记录标识")
	private Long id;

	@ApiModelProperty("员工名称")
	private String staffName;

	@ApiModelProperty("员工编号")
	private String staffBadge;

	@ApiModelProperty("园区名称")
	private String parkName;

	@ApiModelProperty("楼栋名称")
	private String dormitoryName;

	@ApiModelProperty("房间号")
	private String roomName;

	@ApiModelProperty("结算时间")
	private Date statementDate;

	@ApiModelProperty("抄表月份")
	@JsonFormat(pattern = "yyyy-MM")
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;

	@ApiModelProperty("总费用")
	private BigDecimal totalFee;

	@ApiModelProperty("收费列表")
	private List<CateInfo> cateInfos;

	@Data
	public static class CateInfo{

		@ApiModelProperty("收费项目Id")
		private Integer categoryId;

		@ApiModelProperty("收费项目")
		private String cateName;

		@ApiModelProperty("费用")
		private BigDecimal fee;

		@ApiModelProperty("抄表类型 1.房间抄表 2.公摊抄表")
		private Integer meterType;
	}
}
