package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wuxinjian
 * @date 2020-02-07 13:05:42
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LivenessDTO extends BaseDTO {

	/**
	 * 活体置信度:0-1小数
	 */
	@ApiModelProperty("活体置信度:0-1小数")
	private Double livevnessConfidence;
	/**
	 * 活体检测提取的质量最高图片base64
	 */
	@ApiModelProperty("活体检测提取的质量最高图片base64")
	private String bestImageBase64;

}
