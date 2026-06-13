package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.CvwCcdAllowance;

public interface CvwCcdAllowanceService extends IService<CvwCcdAllowance> {

	CvwCcdAllowance getByName(String allowanceName);

}
