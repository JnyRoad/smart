package com.tce.smart.platform.core.dto;

import lombok.Data;

/**
 * 修改住宿 时间
 * @author QIPEI
 *
 */
@Data
public class UpdateDormitoryStaffDTO {

	 private Integer id;


	 private String createTime;

	 /**
	  * 宿舍类型，0-入职，1-换宿，2-外宿，3-离职
	  */
	 private Integer type;
}
