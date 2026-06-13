package com.tce.smart.app.vo.fore;

import com.tce.smart.platform.api.dto.ProcessRecordFlowDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 工作交接詳情
 * @author Administrator
 *
 */
@Data
public class ProcessRecordVO {

    private Map<String,Object> employee;

    private List<ProcessRecordFlowDTO> flow;
}
