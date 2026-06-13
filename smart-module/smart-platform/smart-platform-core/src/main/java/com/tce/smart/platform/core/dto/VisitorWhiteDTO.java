package com.tce.smart.platform.core.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description: VisitorWhiteDTO
 * @date: 2020/12/30 0030 20:26
 * @author: wuling
 * @version: 1.0
 */
@Data
public class VisitorWhiteDTO {

	private Long id;

	private Integer parkId;

	private String parkName;

	private String staffBadge;

	private String staffName;

	private Integer compId;

	private String compName;

	private Integer depId;

	private String depName;

	private String jobId;

	private String jobName;

	private Date createTime;

	private List<Integer> parkIds;
}
