package com.tce.smart.platform.api.dto.resp.badge;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 厂牌设置列表
 * @author fushiping
 * @date 2020/7/8 11:44
 **/
@Data
public class BadgeConfigListRespDTO extends BaseDTO {
	/**
	 * ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	/**
	 * 价格
	 */
	private BigDecimal price;
	/**
	 * 园区
	 */
	private Integer parkId;
	/**
	 * 园区名
	 */
	private String parkName;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
	/**
	 * 创建人ID
	 */
	private Integer createrId;
}
