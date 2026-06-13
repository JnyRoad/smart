package com.tce.smart.platform.api.dto.resp.securityzone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限申请表分页
 *
 * @author
 * @date
 */
@Data
public class SecurityAuthApplyPageRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("ID")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long id;

	@ApiModelProperty("OA单号")
	private String processId;

	@ApiModelProperty("流水号")
	private String serialNum;

	@ApiModelProperty("员工姓名")
	private String name;

	@ApiModelProperty("员工工号")
	private String badge;

	@ApiModelProperty("岗位名称")
	private String jobName;

	@ApiModelProperty("bu名称")
	private String compName;

	@ApiModelProperty("部门名称")
	private String depName;

	@ApiModelProperty("申请时间")
	private LocalDateTime createTime;

	@ApiModelProperty("OA状态描述")
	private String oaStatusDesc;

	@ApiModelProperty("OA状态编码")
	private Integer oaStatus;

	@ApiModelProperty("下发状态")
	private Integer deviceStatus;

	@ApiModelProperty("下发状态描述")
	private String deviceStatusDesc;

	@ApiModelProperty("下发任务总数")
	private Integer totalNum;

	@ApiModelProperty("成功数量")
	private Integer successNum;

	@ApiModelProperty("失败数量")
	private Integer failNum;

	private Integer parkId;

	private String parkName;
}
