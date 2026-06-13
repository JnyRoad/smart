package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 园区宿舍楼l楼层中房间的床位数
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:21
 */

@Data
@TableName("smt_dormitory_bed")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryBed extends Model<SmtDormitoryBed> {
	private static final long serialVersionUID = 1L;

	/**
	*
	*/
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;
	/**
	 * 奇数是下铺，偶数是上铺
	 */
	private Integer bedNumber;
	/**
	 * 所属房间id
	 */
	private Integer roomId;

	/**
	 * 所属楼层ID
	 */
	private Integer floorId;

	/**
	 * 所属园区ID
	 */
	private Integer parkId;

	/**
	 * 所属住宿楼ID
	 */
	private Integer dormitoryId;

	/**
	 * 床位别名
	 */
	private String bedName;

	/**
	 * 是否删除 1.已删除 0.未删除
	 */
	private Integer delFlag;
}
