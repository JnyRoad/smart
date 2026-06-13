package com.tce.smart.platform.core.dto;

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
public class SecurityAuthApplyPageDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private String processId;

	private String name;

	private String badge;

	private String jobName;

	private String compName;

	private Integer oaStatus;

	private String deviceStatus;

	private String serialNum;

	private String depName;

	private LocalDateTime createTime;

	private Integer totalNum;

	private Integer successNum;
}
