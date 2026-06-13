package com.tce.smart.app.vo.fore;

import lombok.Data;

/**
 * 待审批
 * @author Administrator
 *
 */
@Data
public class ApproveListRequestVO {
    private Integer recordType;
    private Integer recordState;
    private Long current;
    private Long size;
}
