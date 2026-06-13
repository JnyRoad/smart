package com.tce.smart.platform.api.dto.resp.badge;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 厂牌补领记录（PC端）
 * @author fushiping
 * @date 2020/7/8 11:44
 **/
@Data
public class BadgeApplyListRespDTO extends BaseDTO {
	/**
	 * ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	/**
	 * 员工工号
	 */
	private String badge;
	/**
	 * 员工姓名
	 */
	private String name;
	/**
	 * BU名
	 */
	private String compName;
	/**
	 * 部门名
	 */
	private String depName;
	/**
	 * 园区
	 */
	private Integer parkId;
	/**
	 * 园区名
	 */
	private String parkName;
	/**
	 * 申请时间
	 */
	private LocalDateTime createTime;
	/**
	 * 申请原因
	 */
	private String reason;
	/**
	 * 办理状态
	 */
	private Integer state;
	/**
	 * 办理状态描述
	 */
	private String stateDesc;

	/**
	 * 厂牌价格
	 */
	private BigDecimal price;


}
