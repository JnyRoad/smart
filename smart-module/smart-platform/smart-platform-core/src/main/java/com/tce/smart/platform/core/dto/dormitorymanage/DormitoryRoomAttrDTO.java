package com.tce.smart.platform.core.dto.dormitorymanage;

import com.tce.smart.platform.core.entity.SmtDormitoryRoom;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class DormitoryRoomAttrDTO extends SmtDormitoryRoom {

	/**
	 * 房间Id列表
	 */
	private List<Integer> roomIds;

}
