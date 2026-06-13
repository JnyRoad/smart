package com.tce.smart.platform.core.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 抓拍人员出入记录查询
 *
 * @author 梁园
 * @date 2019-04-21 18:19:30
 */
@Data
public class SearchSnapPersonAccessDTO implements Serializable  {
	private static final long serialVersionUID = 1L;

	private String compId;
	private String depId;
	private String jobId;
	private String jcheId;
	private String jobName;
	private String personName;
    /**
   * 1:员工；2：访客；
   */
    private Integer personType;
    /**
   *
   */
    private Integer eventType;
    /**
   *
   */
    private Integer areaId;
	/**
	 * 抓拍查询开始时间
	 */
	private String startTime;

	/**
	 * 抓拍查询结束时间
	 */
	private String endTime;
	private String company;

	/**
	 * 体温是否正常 1.正常 0.不正常
	 */
	private Integer isNormal;

	/**
	 * 园区Id
	 */
	private Integer parkId;

	private String badge;

	/**
	 * 设备ID
	 */
	private String deviceId;
}
