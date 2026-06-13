package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区宿舍楼的楼层
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:15
 */
@Data
@TableName("smt_dormitory_floor")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryFloor extends Model<SmtDormitoryFloor> implements Comparable<SmtDormitoryFloor>{
	private static final long serialVersionUID = 1L;

	/**
	 * 楼层ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;
	/**
	 * 所属住宿楼ID
	 */
	private Integer dormitoryId;
	/**
	 * 楼层数
	 */
	private Integer floorName;
	/**
	 * 房间数默认是0
	 */
	private Integer roomNum;
	/**
	 * 是否为宿舍楼层0-否 1-是 默认是1
	 */
	private Integer isDormitoryFloor;

	/**
	 * 所属园区ID
	 */
	private Integer parkId;

	/**
	 * 别名
	 */
	private String aliasName;

	@Override
	public int compareTo(SmtDormitoryFloor o) {
		return floorName.compareTo(o.getFloorName());
	}
}
