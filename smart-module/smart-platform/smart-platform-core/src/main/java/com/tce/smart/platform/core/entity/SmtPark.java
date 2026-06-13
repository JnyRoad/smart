package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 园区表
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:35
 */
@Data
@TableName("smt_park")
@EqualsAndHashCode(callSuper = true)
public class SmtPark extends Model<SmtPark> {
	private static final long serialVersionUID = 1L;

	/**
	 * 园区ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;
	/**
	 * 园区名称
	 */
	@NotBlank(message = "园区名称不能为空")
	private String parkName;

	/**
	 * 园区经度
	 */

	@NotBlank(message = "园区经度不能为空")
	private BigDecimal parkLongitude;
	/**
	 * 园区纬度
	 */
	@NotBlank(message = "园区维度不能为空")
	private BigDecimal parkLatitude;
	/**
	 * 园区地址
	 */
	private String parkAddress;
	/**
	 * 识别半径,单位是米
	 */
	private Integer radius;

	/**
	 * 定位计算距离，单位:米
	 */
	@TableField(exist=false)
	private BigDecimal distance;

	/**
	 * 咨询电话
	 */
	private String parkPhone;

	/**
	 * 转发服务器URL
	 */
	private String bridgeUrl;

	/**
	 * 占地面积 （亩）
	 */
	private Integer area;

	/**
	 * 产房个数
	 */
	private Integer workShopNum;

	/**
	 * 食堂楼栋个数
	 */
	private Integer diningRoomNum;
}
