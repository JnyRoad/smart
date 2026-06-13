package com.tce.smart.app.vo.fore;

import lombok.Data;

/**
 * 应聘状态字典信息
 * @author qipei
 *
 */
@Data
public class ApplicationStatusVo {

	//类型
	private Integer applyState;
	//描述
	private String applyStateDesc;

}
