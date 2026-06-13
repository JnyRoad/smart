package com.tce.smart.app.vo.fore;

import java.util.Date;

import lombok.Data;

/**
 * 应聘流程信息
 * @author qipei
 *
 */
@Data
public class RecordVo {

	//操作名称
	private String optName;

	//操作时间
	private Date optDate;

	//操作人
	private String optUser;

	//操作备注
	private String optDesc;


}
