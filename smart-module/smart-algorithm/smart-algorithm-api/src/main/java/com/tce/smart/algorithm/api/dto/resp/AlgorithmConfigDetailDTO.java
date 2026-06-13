package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @ClassName AlgorithmChoose
 * @Description 算法选择表
 * @Author wxjason
 * @Date 2019\8\1 0001 12:01
 * Version 1.0
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class AlgorithmConfigDetailDTO extends BaseDTO {
    private static final long serialVersionUID = 1L;

    /**
     * 算法类型：对应AlgorithmTypeEnum枚举中的type
     */
	@ApiModelProperty("算法类型")
    private String algorithmType;

	/**
	 * 算法名称
	 */
	@ApiModelProperty("算法名称")
	private String algorithmName;

	/**
	 * 算法配置详情
	 */
	@ApiModelProperty("算法配置详情")
	private List<ConfigDetailDTO> configList;

}
