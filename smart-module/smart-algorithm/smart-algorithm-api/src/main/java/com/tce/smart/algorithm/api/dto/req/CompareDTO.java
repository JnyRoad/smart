package com.tce.smart.algorithm.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

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
public class CompareDTO extends BaseDTO {

    /**
     * 图片参数
     */
    @NotNull(message = "图片A参数为空")
	@ApiModelProperty("图片A参数")
    private CompareImageDTO compareImageA;

    /**
     * 图片参数
     */
    @NotNull(message = "图片B参数为空")
	@ApiModelProperty("图片B参数")
    private CompareImageDTO compareImageB;

}
