package com.tce.smart.platform.core.dto;


import lombok.Data;

/**
 * 员工车辆入园申请添加
 * @author dell
 *
 */
@Data
public class ApplyAuthDTO {


	private String badge;

	private Integer parkId;

    /**
   * 车牌号
   */
    private String plateNumber;




}
