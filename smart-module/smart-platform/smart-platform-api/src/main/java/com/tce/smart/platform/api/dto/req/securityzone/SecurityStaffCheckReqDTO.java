package com.tce.smart.platform.api.dto.req.securityzone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 *
 * @author
 * @date
 */
@Data
public class SecurityStaffCheckReqDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("待筛选人员")
	List<SecurityStaffInputReqDTO> staffInfos;

	@ApiModelProperty("是否有人脸照片")
	Integer isImg;

	@ApiModelProperty("是否签署保密协议")
	Integer isSecurity;

	@ApiModelProperty("入职时间")
	String startDate;

	@ApiModelProperty("入职时间")
	String endDate;

	@ApiModelProperty("关联权限")
	List<Integer> authIds;
}
