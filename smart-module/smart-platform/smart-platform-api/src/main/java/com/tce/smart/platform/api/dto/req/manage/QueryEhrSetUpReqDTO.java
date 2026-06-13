package com.tce.smart.platform.api.dto.req.manage;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-27 10:45:36
 */
@Data

public class QueryEhrSetUpReqDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

    /**
   * 园区
   */
    private Integer parkId;
    /**
   * 设置类型
   */
    private Integer setType;

}
