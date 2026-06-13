package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-24 09:44
 */
@Data
public class AddArticlesReleaseReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer parkId;
	private Integer dormitoryId;
	private Integer floorId;
	private Integer roomId;
	private Integer bedId;
	private String badge;
	private String name;
	private String phone;
	private Integer articlesType;
	private String articlesDesc;
	private String carrier;
	private String plannedDepartureTime;
	private String licensePlate;
	private String remarks;
	private Integer status;
	private String oneImg;
	private String twoImg;
	private String threeImg;
	private String remark;
}
