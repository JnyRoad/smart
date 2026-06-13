package com.tce.smart.platform.core.model;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宿舍管理列表-按楼层统计
 *
 * @author jinbo
 * @date 2019-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormitoryCountList extends Model<DormitoryCountList> {

	private static final long serialVersionUID = 1L;

	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 园区名称
	 */
	private String parkName;
	/**
	 * 楼栋数
	 */
	private Integer buildingNumber;
	/**
	 * 容纳人数
	 */
	private Integer total;
	/**
	 * 实际入住人数
	 */
	private Integer actualNumber;

}
