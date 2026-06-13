package com.tce.smart.algorithm.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * @ClassName: CompareDTO
 * @Package com.tce.smart.yunxun.algorithm.api.dto.req
 * @Description:
 * @Author wuxinjian
 * @Date 2019-10-10 11:43
 * @Version V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompareImageDTO extends BaseDTO {

    /**
     * 图片base64
     */
	@ApiModelProperty("图片Base64,该参数与图片ID二选一")
    private String imageBase64;
	/**
	 * 图片base64
	 */
	@ApiModelProperty("图片ID,该参数与图片Base64二选一")
	private String imageId;
    /**
     * 人脸类型,对应com.tce.smart.yunxun.algorithm.api.enums.FaceTypeEnum中type字段
     */
    @NotBlank(message = "人脸类型为空")
	@ApiModelProperty("人脸类型:CERT证件照,LIVE活体照")
    private String faceType;
	/**
	 * 是否是证据照0,不是 1，是
	 */
	@ApiModelProperty("是否是证据照")
	private Integer isCard;

}
