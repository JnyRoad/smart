package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.*;

/**
 * 园区宿舍楼中每个楼层的房间信息
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("smt_dormitory_room")
@EqualsAndHashCode(callSuper = true)
public class SmtDormitoryRoom extends Model<SmtDormitoryRoom> {
	private static final long serialVersionUID = 1L;

	/**
	 * 房间ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;
	/**
	 * 房间名称，与楼层号拼接
	 */
	private Integer roomName;

	/**
	 * 房间号，按顺序排
	 */
	private Integer roomNum;
	/**
	 * 床位数,默认是10
	 */
	private Integer bedTotal;
	/**
	 * 房间性别类型 默认是0，0-男，1-女
	 */
	private Integer roomSex;
	/**
	 * 默认是员工类型
	 */
	private Integer roomType;
	/**
	 * 是否为寝室，默认0， 0-是，1-否
	 */
	private Integer isDormitoryRoom;

	/**
	 * 是否参与水电结算，默认0， 0-是，1-否
	 */
	private Integer isCount;
	/**
	 * 所属楼层ID
	 */
	private Integer floorId;

	/**
	 * 所属园区IDSMT_DORMITORY_TYPE
	 */
	private Integer parkId;

	/**
	 * 所属住宿楼ID
	 */
	private Integer dormitoryId;

	/**
	 * 水电模板标识
	 */
	private Long sdTemplateId;

	/**
	 * 自动分配标识
	 */
	private Integer allotFlag;

	/**
	 * 别名
	 */
	private String aliasName;

}
