package com.tce.smart.platform.api.dto.req;

import lombok.Data;

/**
 * 待审批
 * @author Administrator
 *
 */
@Data
public class RepairsApproveListReqDTO {

    private Integer recordType;

    private Integer recordState;

}
