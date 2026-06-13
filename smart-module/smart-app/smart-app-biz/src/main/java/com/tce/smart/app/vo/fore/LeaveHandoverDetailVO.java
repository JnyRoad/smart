package com.tce.smart.app.vo.fore;

import java.util.Map;

import lombok.Data;

/**
 * 工作交接詳情
 * @author Administrator
 *
 */
@Data
public class LeaveHandoverDetailVO {

    private Map<String,Object> employee;

    private Map<String,Object> handover;
}
