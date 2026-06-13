package com.tce.smart.dispatcher.api.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Description: TODO
 * @ProjectName smart-dispatcher
 * @ClassName: ImageDTO
 * @Author jinbo
 * @Date 2019/11/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 园区ID
	 */
	@ApiModelProperty(value = "园区ID")
	private Integer parkId;
	/**
	 * 图片ID
	 */
	@ApiModelProperty(value = "图片ID")
	private String id;

	/**
	 * 图片Base64
	 */
	@ApiModelProperty(value = "图片Base64")
	private String base64Image;
}
