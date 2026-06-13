package com.tce.smart.platform.api.dto.req.badge;


import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * 厂牌挂失编辑
 * @author fushiping
 * @date 2020/7/8 11:44
 **/
@Data
public class EditBadgeConfigReqDTO extends BaseDTO {

	/**
	 * id
	 */
	private Long id;
	/**
	 * 价格
	 */
	@NotNull
	@Range(min = 1, max = 300, message = "价格过高")
	@PositiveOrZero(message = "请输入正数")
	private BigDecimal price;
	/**
	 * 园区
	 */
	@NotNull
	private Integer parkId;
	/**
	 * 创建人
	 */
	private Integer createrId;

}
