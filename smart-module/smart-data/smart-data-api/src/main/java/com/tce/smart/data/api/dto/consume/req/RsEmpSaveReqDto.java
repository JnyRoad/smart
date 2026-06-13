package com.tce.smart.data.api.dto.consume.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * RsEmp员工信息保存DTO
 *
 * @author wuling
 * @date 2021-01-19
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RsEmpSaveReqDto implements Serializable {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 4476029298600102910L;

	/**
	 * 员工号
	 */
	private String empNo;

	/**
	 * 员工姓名
	 */
	private String empName;

	/**
	 * 性别
	 */
	private Integer empSex;

	/**
	 * 身份证
	 */
	private String empIDNo;

	/**
	 * 部门编号
	 */
	private String dptNo;

	/**
	 * 添加时间
	 */
	private Date grpDate;

}
