package com.tce.smart.platform.core.vo;

import lombok.Data;
/**
 * 根据园区的职层去统计入住情况
 * @author QIPEI
 *
 */
@Data
public class DormitoryCountJche {

	private Integer jcheId;

	private String  jcheName;

	private Integer manNum;

	private Integer womanNum;

	/**
	 * 没有性别的数据
	 */
	private Integer otherNum;

	private Integer total;

	private String welfareLevel;


}
