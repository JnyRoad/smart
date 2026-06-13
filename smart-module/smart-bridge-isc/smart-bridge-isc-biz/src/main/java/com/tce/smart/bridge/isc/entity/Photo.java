package com.tce.smart.bridge.isc.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description: TODO
 * @ProjectName smart-bridge
 * @ClassName: ImageDTO
 * @Author jinbo
 * @Date 2019/11/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 图片类型
	 */
	@ApiModelProperty(value = "图片类型")
	private Integer photoType;
	/**
	 * 图片ID
	 */
	@ApiModelProperty(value = "图片ID")
	private String photoId;

	/**
	 * 图片Base64
	 */
	@ApiModelProperty(value = "图片Base64")
	private String photoData;
}
