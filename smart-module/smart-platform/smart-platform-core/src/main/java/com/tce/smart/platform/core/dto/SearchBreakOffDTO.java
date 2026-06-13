package com.tce.smart.platform.core.dto;

import lombok.Data;

import java.util.List;

/**
 * 查询调休列表请求类
 * @author QIPEI
 *
 */
@Data
public class SearchBreakOffDTO {


	private String staffBadge;

	private String staffName;

	private String startTime;

	private String endTime;

	private List<Integer> parkIds;
}
