package com.tce.smart.platform.api.dto.req.badge;

import com.baomidou.mybatisplus.annotation.FieldFill;
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
public class QueryBadgeConfigReqDTO extends BaseDTO {

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
}
