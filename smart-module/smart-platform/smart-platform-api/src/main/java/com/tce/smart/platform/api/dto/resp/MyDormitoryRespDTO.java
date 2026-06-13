package com.tce.smart.platform.api.dto.resp;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.Date;

/**
 * 员工的住宿信息
 *
 * @author QIPEI
 *
 */
@Data
public class MyDormitoryRespDTO extends BaseVO {

	/**
	 *
	 */
	private Integer id;
	/**
	 * 员工id
	 */
	private Long staffId;
	/**
	 * 员工名称
	 */
	private String staffName;
	/**
	 * 员工工号
	 */
	private String staffBadge;

	/**
	 * 性别
	 */
	private Integer staffSex;
	/**
	 * 园区id
	 */
	private Integer parkId;
	/**
	 * 园区名称
	 */
	private String parkName;
	/**
	 * 宿舍楼id
	 */
	private Integer dormitoryId;
	/**
	 * 宿舍楼名称
	 */
	private String dormitoryName;
	/**
	 * 楼层id
	 */
	private Integer floorId;
	/**
	 * 楼层层数
	 */
	private Integer floorName;
	/**
	 * 房间id
	 */
	private Integer roomId;
	/**
	 * 房间号
	 */
	private Integer roomName;
	/**
	 * 床位id
	 */
	private Integer bedId;
	/**
	 * 床位编号  1-10
	 */
	private Integer bedNumber;
	/**
	 * 宿舍类型id
	 */
	private Integer dormitoryTypeId;
	/**
	 * 宿舍类型名称
	 */
	private String dormitoryTypeName;

	/**
	 *
	 */
	private Date createTime;

	/**
	 * 是否是员工 0-否  1-是
	 */
	private Integer isStaff;

	private Integer bedToal;

	private Integer usedNum;

}
