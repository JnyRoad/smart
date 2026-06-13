package com.tce.smart.platform.core.entity.ext;

import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import lombok.Data;

/**
 * 宿舍房间扩展
 * @author wuling
 *
 */
@Data
public class DormitoryRoomExt extends SmtDormitoryRoom {

	/**
	 * 空余床位
	 */
	private Integer freeBedNum;

	/**
	 * 使用床位
	 */
	private Integer useBedNum;

	/**
	 * 床位类型名称
	 */
	private String typeName;

	/**
	 * 总床位
	 */
	private Integer bedTotal;

	/**
	 * 楼层
	 */
	private Integer floorName;
}
