package com.tce.smart.platform.core.model;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: 按楼栋统计入住情况
 * @date: 2020/9/28 15:46
 * @author: wuling
 * @version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormitoryCountByBuilding extends Model<DormitoryCountByBuilding> {

	private static final long serialVersionUID = 1L;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 楼栋ID
	 */
	private Integer dormitoryId;

	/**
	 * 楼栋名称
	 */
	private String dormitoryName;

	/**
	 * 床位总数
	 */
	private Integer total;

	/**
	 * 实际入住人数
	 */
	private Integer actualNumber;

}
