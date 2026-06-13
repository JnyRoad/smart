package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 水电模板规则表
 * @date: 2020-07-07 18:20
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_TEMPLATES_RULE")
@EqualsAndHashCode(callSuper = true)
public class SmtTemplatesRule extends Model<SmtTemplatesRule> {

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 所属模板ID
	 */
	@NotBlank(message="模板ID不能为空")
	private Long tempId;

	/**
	 * 收费项目ID
	 */
	@NotBlank(message="模板项目ID不能为空")
	private Integer categoryId;

	/**
	 * 月份
	 */
	@NotBlank(message="月份不能为空")
	private Integer monthNum;

	/**
	 * 标准用量
	 */
	@NotBlank(message="标准用量不能为空")
	private Double standardQty;

	/**
	 * 超出费用单价
	 */
	@NotBlank(message="超出费用单价不能为空")
	private BigDecimal overFee;

	/**
	 * 添加时间
	 */
	private Date createTime;
}
