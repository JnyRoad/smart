package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwBizLcardlost;

import java.util.List;

public interface EvwBizLcardlostService extends IService<EvwBizLcardlost> {
	List<EvwBizLcardlost> list(String badge, String queryMonth);
}
