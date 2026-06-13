package com.tce.smart.app.vo.fore;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 厂牌信息返回
 * @author fushiping
 * @date 2020/7/9 18:22
 **/
@Data
public class BadgeInfoVo {

	/**
	 * 员工姓名
	 */
	@ApiModelProperty("员工姓名")
	private String staffName;

	/**
	 * 员工工号
	 */
	@ApiModelProperty("员工工号")
	private String staffNo;

	/**
	 * 厂牌号
	 */
	@ApiModelProperty("厂牌号")
	private String badgeNo;

	/**
	 * 厂牌状态
	 */
	@ApiModelProperty("厂牌状态")
	private Integer badgeStatus;

	/**
	 * 厂牌ID
	 */
	@ApiModelProperty("厂牌ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long cardId;
}
