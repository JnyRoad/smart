package com.tce.smart.platform.core.dto;

import lombok.Data;

import java.util.List;

/**
 * 查询加班列表请求类
 * @author QIPEI
 *
 */
@Data
public class SearchOutDormitoryDTO {


	private String staffBadge;

	private String staffName;

	private String startTime;

	private String endTime;

	private List<Integer> parkIds;
}
