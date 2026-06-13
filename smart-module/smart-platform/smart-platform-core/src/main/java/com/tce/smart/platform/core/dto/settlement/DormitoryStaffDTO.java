package com.tce.smart.platform.core.dto.settlement;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.util.Date;

/**
 * @author Li.JiaJun
 * @since 2022/7/15 9:22
 */
@Data
public class DormitoryStaffDTO extends BaseDTO {
	/**
	 * 在住记录id || 退宿记录id
	 */
	private Integer id;
	/**
	 * 宿舍楼名称
	 */
	private String dormitoryName;
	/**
	 * 房间id
	 */
	private Integer roomId;
	/**
	 * 房间号
	 */
	private Integer roomName;
	/**
	 * 园区id
	 */
	private Integer parkId;
	/**
	 * 入住日期 || 退宿日期
	 */
	private Date createTime;
	/**
	 * 住宿类型  0-入住  1-换宿  2-外宿  3-离职  4-退房  5-自离
	 */
	private Integer  type;
	/**
	 * 最初入住时间
	 */
	private Date inTime;
	/**
	 * 操作时间  如：type是0，该时间是入住时间，type是1，该时间是换宿时间，type是2，该时间是退宿时间
	 */
	private Date time;
}
