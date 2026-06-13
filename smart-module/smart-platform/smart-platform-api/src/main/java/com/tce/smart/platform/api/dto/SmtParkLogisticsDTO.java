package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 园区物流关系表
 *
 * @author mckaywu
 * @date 2019-11-20 10:36:33
 */
@Data
public class SmtParkLogisticsDTO implements Serializable {
private static final long serialVersionUID = -592223363997550623L;

    /**
   * 主键ID
   */
    private Integer id;
    /**
   * 园区编号
   */
    private Integer parkId;
    /**
   * 物流中心编号
   */
    private String companyId;
	/**
	 * 物流中心名称
	 */
	private String companyName;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
