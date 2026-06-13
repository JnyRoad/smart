package com.tce.smart.app.ao.fore;

import lombok.Data;

/**
 * 修改紧急联系人
 * @author tce
 *
 */
@Data
public class EmployeeUpdateAo {


	private String relation;

	private String emergencyName;

	private String emergencyPhone;
}
