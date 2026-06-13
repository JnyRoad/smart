package com.tce.smart.platform.api.dto.req;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

@Data
public class LeaveHandoverItemReqDTO extends BaseVO{

    private static final long serialVersionUID = 1L;
    /**
     * 交接事项ID
     */
    private Integer jjItemId;
    /**
     * 金额
     */
    private Double je;
    /**
     * 说明
     */
    private String jjRemark;
}
