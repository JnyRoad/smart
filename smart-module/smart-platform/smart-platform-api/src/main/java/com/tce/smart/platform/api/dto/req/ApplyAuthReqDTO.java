package com.tce.smart.platform.api.dto.req;


import lombok.Data;

import java.io.Serializable;

/**
 * 员工车辆入园申请添加
 * @author dell
 *
 */
@Data
public class ApplyAuthReqDTO implements Serializable {


	private String badge;

	private Integer parkId;

    /**
   * 车牌号
   */
    private String plateNumber;




}
