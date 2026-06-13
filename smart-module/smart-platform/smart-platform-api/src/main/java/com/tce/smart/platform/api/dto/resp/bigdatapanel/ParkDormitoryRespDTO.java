package com.tce.smart.platform.api.dto.resp.bigdatapanel;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 大数据面板-宿舍动态实体类
 * @date: 2020-08-04 15:02
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ParkDormitoryRespDTO implements Serializable {

	private static final long serialVersionUID = -4163999245683849463L;

	/**
	 * 房间数量
	 */
	@ApiModelProperty("房间数量")
	private Integer roomCount;

	/**
	 * 空闲房间数量
	 */
	@ApiModelProperty("空闲房间数量")
	private Integer roomFreeCount;

    /**
     * 床位数量
	 */
	@ApiModelProperty("床位数量")
	private Integer bedCount;

	/**
	 * 空闲床位数量
	 */
	@ApiModelProperty("空闲床位数量")
	private Integer bedFreeCount;
}
