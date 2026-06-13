package com.tce.smart.platform.core.vo;

import java.util.List;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工模块的ID、HR招聘职层ID数据Vo
 *
 * @author mckaywu
 * @date 2019-06-15 14:07:16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffAuthModuleHrVO extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 6038690482518436420L;

	/**
	 * 主键
	 */
	private String bragde;

	/**
	 * 模块ID
	 */
	private List<String> moduleId;

	/**
	 * Hr招聘招聘权限ID
	 */
	private List<String> hrAuthId;

}
