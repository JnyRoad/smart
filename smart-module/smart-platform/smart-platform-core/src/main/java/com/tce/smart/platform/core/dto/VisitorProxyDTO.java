package com.tce.smart.platform.core.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description: VisitorProxyDTO
 * @date: 2020/12/30 0030 20:26
 * @author: wuling
 * @version: 1.0
 */
@Data
public class VisitorProxyDTO {

	private Long id;

	private Integer parkId;

	private String parkName;

	private String interVieweeBadge;

	private String interVieweeName;

	private Integer interVieweeCompId;

	private String interVieweeCompName;

	private Integer interVieweeDepId;

	private String interVieweeDepName;

	private String interVieweeJobId;

	private String interVieweeJobName;

	private String proxyBadge;

	private String proxyName;

	private Integer proxyCompId;

	private String proxyCompName;

	private Integer proxyDepId;

	private String proxyDepName;

	private String proxyJobId;

	private String proxyJobName;

	private Date createTime;

	private List<Integer> parkIds;
}
