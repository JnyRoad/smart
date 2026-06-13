package com.tce.smart.app.vo.fore;

import java.util.List;

import lombok.Data;

@Data
public class LeaveTypeDataVO<T> {

    private List<T> records;

    private int total;
}
