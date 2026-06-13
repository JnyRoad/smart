package com.tce.smart.platform.api.dto.resp.securityzone;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityStaffInputReqDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 权限申请员工备注返回
 *
 * @author
 * @date
 */
@Data
public class AuthApplyRemarkRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;


	@ApiModelProperty("员工工号")
	private String badge;

	@ApiModelProperty("员工ID")
	private Long staffId;

	@ApiModelProperty("关联权限列表")
	private List<Integer> authList;

	private String remark;
}
