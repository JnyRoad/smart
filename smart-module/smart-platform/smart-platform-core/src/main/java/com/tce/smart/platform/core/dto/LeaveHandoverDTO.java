package com.tce.smart.platform.core.dto;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class LeaveHandoverDTO extends BaseVO{

    private static final long serialVersionUID = 1L;
    /**
   * 流程编号
   */
    @NotNull(message="流程编号不可为空")
    private String processId;
    /**
     * 交接人工号
     */
    @NotNull(message="EEEEEE")
    private String jjr;

    @Valid
    private List<LeaveHandoverItemDTO> itemList;
}
