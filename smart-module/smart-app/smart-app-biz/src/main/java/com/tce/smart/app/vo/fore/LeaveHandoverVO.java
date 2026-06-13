package com.tce.smart.app.vo.fore;

import java.util.List;

import lombok.Data;

/**
 * 工作交接
 * @author Administrator
 *
 */
@Data
public class LeaveHandoverVO {

    private String processId;

    private List<LeaveItemVO> handItem;
}
