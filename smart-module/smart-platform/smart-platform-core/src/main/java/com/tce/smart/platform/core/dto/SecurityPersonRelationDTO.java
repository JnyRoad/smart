package com.tce.smart.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:00
 */
@Data
public class SecurityPersonRelationDTO implements Serializable {
private static final long serialVersionUID = 1L;


	private Long id;
    /**
   * 保密区id
   */
    private Long securityId;
    /**
   * 关联人员id
   */
    private Long staffId;
    /**
   * 人员工号
   */
    private String staffBadge;
    /**
   * 人员姓名
   */
    private String staffName;
    /**
   * 园区id
   */
    private Integer parkId;

	/**
	 * buID
	 */
    private String compId;

	/**
	 * bu名
	 */
    private String compName;

	/**
	 * 部门id
	 */
    private String depId;

	/**
	 * 部门名
	 */
    private String depName;

	/**
	 * 岗位id
	 */
    private String jobId;

	/**
	 * 岗位名
	 */
    private String jobName;

	/**
	 * 入职时间
	 */
    private Date inTime;

}
