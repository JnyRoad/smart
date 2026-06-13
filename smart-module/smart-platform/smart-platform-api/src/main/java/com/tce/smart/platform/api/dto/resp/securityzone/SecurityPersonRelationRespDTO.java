package com.tce.smart.platform.api.dto.resp.securityzone;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
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
public class SecurityPersonRelationRespDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@JsonFormat(shape=JsonFormat.Shape.STRING)
	@ApiModelProperty("员工保密区关联表ID")
	private Long id;
    /**
   * 保密区id
   */
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	@ApiModelProperty("保密区id")
    private Long securityId;
    /**
   * 关联人员id
   */
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	@ApiModelProperty("关联人员id")
    private Long staffId;
    /**
   * 人员工号
   */
	@ApiModelProperty("人员工号")
    private String staffBadge;
    /**
   * 人员姓名
   */
	@ApiModelProperty("人员姓名")
    private String staffName;
    /**
   * 园区id
   */
	@ApiModelProperty("园区id")
    private Integer parkId;

	/**
	 * buID
	 */
	@ApiModelProperty("buID")
	private String compId;

	/**
	 * bu名
	 */
	@ApiModelProperty("bu名")
	private String compName;
	/**
	 * 部门id
	 */
	@ApiModelProperty("部门id")
    private String depId;

	/**
	 * 部门名
	 */
	@ApiModelProperty("部门名")
    private String depName;

	/**
	 * 岗位id
	 */
	@ApiModelProperty("岗位id")
    private String jobId;

	/**
	 * 岗位名
	 */
	@ApiModelProperty("岗位名")
    private String jobName;

	/**
	 * 入职时间
	 */
	@ApiModelProperty("入职时间")
	@JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date inTime;

}
