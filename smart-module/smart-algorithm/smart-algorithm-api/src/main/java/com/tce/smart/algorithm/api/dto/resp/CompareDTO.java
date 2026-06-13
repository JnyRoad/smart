package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
     * 比对相似度:0-1小数
     */
	@ApiModelProperty("比对相似度:0-1小数")
    private Double similarity;

}
