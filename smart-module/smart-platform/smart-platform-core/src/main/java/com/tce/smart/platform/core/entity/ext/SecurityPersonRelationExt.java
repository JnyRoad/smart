package com.tce.smart.platform.core.entity.ext;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:00
 */
@Data
public class SecurityPersonRelationExt implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Long> relationId;

	private Long securityId;

	private Integer parkId;

	private String staffBadge;

	private String staffName;

	private String buId;

	private String depId;

	private List<String> staffBadges;

	private List<Integer> parkIds;

	private List<String> buIds;

	private List<String> depIds;

	private Integer signStatus;

	private String startDate;

	private String endDate;

}
