package com.tce.smart.platform.api.dto.req.admittance;

import lombok.Data;

import java.io.Serializable;

/** 匿名访客仅可按姓名、手机号查找接待人，完整员工实体不得透出。 */
@Data
public class VisitorReceptionistSearchReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer parkId;
	private String receptionistName;
	private String receptionistPhone;
}
