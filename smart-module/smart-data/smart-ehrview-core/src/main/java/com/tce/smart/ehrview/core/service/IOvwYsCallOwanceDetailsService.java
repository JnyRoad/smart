package com.tce.smart.ehrview.core.service;

import com.tce.smart.ehrview.core.entity.OvwYsCallOwanceDetails;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IOvwYsCallOwanceDetailsService extends IService<OvwYsCallOwanceDetails> {

	OvwYsCallOwanceDetails getByBadge(String badge);

}
