package com.tce.smart.platform.core.entity.ext;

import lombok.Data;

import java.io.Serializable;

/**
 * 抓拍人员出入记录查询
 *
 * @author 梁园
 * @date 2019-04-21 18:19:30
 */
@Data
public class SearchSnapPersonAccessExt implements Serializable  {
	private static final long serialVersionUID = 5302146690610842439L;

	private String compId;
	private String depId;
	private String jobId;
	private String jcheId;
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
}
