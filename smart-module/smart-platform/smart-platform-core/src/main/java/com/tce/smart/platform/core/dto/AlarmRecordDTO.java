package com.tce.smart.platform.core.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 警报记录
 * @author Lenovo
 *
 */
@Data
public class AlarmRecordDTO implements Serializable {
	private static final long serialVersionUID = 1L;

    /**
   * 区域ID
   */
    private Integer areaId;

    /**
   * 园区主键
   */
    private Integer parkId;

	/**
	 * 开始时间
	 */
	private String startTime;

	/**
	 * 结束时间
	 */
	private String endTime;

	private Integer pid;

	private List<Integer> list;

	private List<Integer> parkIds;
}
