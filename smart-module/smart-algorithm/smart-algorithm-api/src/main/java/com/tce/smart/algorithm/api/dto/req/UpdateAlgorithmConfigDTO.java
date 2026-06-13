package com.tce.smart.algorithm.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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
public class UpdateAlgorithmConfigDTO extends BaseDTO {

    /**
     * 主键ID
     */
    @NotBlank(message = "算法类型不能为空")
	@ApiModelProperty("算法类型")
    private String algorithmType;

	/**
	 * 算法配置详情
	 */
	@NotEmpty(message = "算法配置项不能为空")
	@ApiModelProperty("算法配置列表")
	private List<ConfigDetailDTO> configList;

}
