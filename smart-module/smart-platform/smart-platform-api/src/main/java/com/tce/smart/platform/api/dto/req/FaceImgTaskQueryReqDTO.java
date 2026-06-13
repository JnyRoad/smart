package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-20 17:44:40
 */
@Data
public class FaceImgTaskQueryReqDTO implements Serializable {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@ApiModelProperty("id")
    private Long id;
    /**
   * 园区id
   */
	@ApiModelProperty("园区id")
    private Integer parkId;
    /**
   * 任务名称
   */
	@ApiModelProperty("任务名称")
    private String taskName;


}
