package com.tce.smart.platform.core.dto.dormitorymanage;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class DormitoryApplyDTO {

	private Long id;

	private Integer parkId;

	private String parkName;

	private String staffBadge ;

	private String staffName;

	private Date applyDate;

	private Date resultDate;

	private Integer status;

	private Integer likeType;

	private String likeTypeDesc;

	private String applyRemark;

	private String compName;

	private String depName;

	private String jcheName;

	private List<Integer> parkList;
}
