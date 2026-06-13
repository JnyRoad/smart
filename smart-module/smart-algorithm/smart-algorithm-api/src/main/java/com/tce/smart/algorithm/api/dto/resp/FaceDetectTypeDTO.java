package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName AlgorithmChoose
 * @Description 算法选择表
 * @Author wxjason
 * @Date 2019\8\1 0001 12:01
 * Version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class FaceDetectTypeDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;

    /**
     * 人脸检测类型
     */
	@ApiModelProperty("人脸检测类型")
    private Integer type;

	/**
	 * 人脸检测名称
	 */
	@ApiModelProperty("人脸检测名称")
	private String name;

}
