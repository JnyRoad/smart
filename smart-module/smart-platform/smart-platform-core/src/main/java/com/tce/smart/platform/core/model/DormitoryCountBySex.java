package com.tce.smart.platform.core.model;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宿舍监控-按性别统计
 *
 * @author jinbo
 * @date 2019-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormitoryCountBySex extends Model<DormitoryCountBySex> {

	private static final long serialVersionUID = 1L;

	/**
	 * 性别
	 */
	private Integer roomSex;

	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 园区名称
	 */
	private String parkName;
	/**
	 * 宿舍ID
	 */
	private Integer dormitoryId;
	/**
	 * 宿舍名称
	 */
	private String dormitoryName;
	/**
	 * 房间类型
	 */
	private Integer roomType;
	/**
	 * 房间类型描述
	 */
	private String typeName;

	/**
	 * 床位总数
	 */
	private Integer total;

	/**
	 * 实际入住人数
	 */
	private Integer actualNumber;

	/**
	 * 剩余床位数
	 */
	private Integer surplus;

}
