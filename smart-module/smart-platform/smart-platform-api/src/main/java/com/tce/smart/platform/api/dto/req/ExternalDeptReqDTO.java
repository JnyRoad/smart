package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 *
 *
 * @author
 * @date 2019-04-15 11:34:58
 */
@Data
public class ExternalDeptReqDTO extends BaseDTO {


    private Long id;
    /**
   * 部门名
   */
    private String deptName ;
    /**
   * 上级部门id
   */
    private Long parentDept;
	/**
	 * 主管姓名
	 */
    private String directorName;

    private Long compId;
	/**
	 * 主管工号
	 */
    private String director;

    private String c6DeptNo;


}
