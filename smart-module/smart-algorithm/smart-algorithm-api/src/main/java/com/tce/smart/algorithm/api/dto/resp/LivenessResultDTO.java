package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: StaticLiveResultDTO
 * @Package com.tce.smart.yunxun.algorithm.api.dto.resp
 * @Description:
 * @Author wuxinjian
 * @Date 2019-10-10 11:43
 * @Version V1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LivenessResultDTO extends BaseDTO {

    /**
     * 静默活体检测分数值0-1小数
     */
	@ApiModelProperty("活体置信度:0-1小数")
    private Double score;

}
