package com.tce.smart.platform.core.model;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;

@Data
public class YearHoliday extends BaseVO{

    private static final long serialVersionUID = 1L;
    private Double dayCount;

    public YearHoliday(Double dayCount) {
        super();
        this.dayCount = dayCount;
    }

    public YearHoliday() {
        super();
    }

}
