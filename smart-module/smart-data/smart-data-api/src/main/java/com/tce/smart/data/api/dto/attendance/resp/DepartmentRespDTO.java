package com.tce.smart.data.api.dto.attendance.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 * @Auther: guohongtai
 * @Date: 2020-08-07 09:13
 */
@Data
public class DepartmentRespDTO extends BaseVO {
	private String deptName;

	private Integer jchenId;

	private Integer requiredNumber;

	private Integer actualNumber;

	private Integer cardLostNumber;

	private Integer leaveNumber;

	private Integer makeUpNumber;
}
