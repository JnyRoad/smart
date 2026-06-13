package com.tce.smart.platform.core.vo;

import java.util.List;

import com.tce.smart.platform.core.model.ProcessRecordFlow;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class LeaveRrocessRecordVO extends LeaveApplicationVO {
    private static final long serialVersionUID = 1L;

    List<ProcessRecordFlow> recordList;
}
