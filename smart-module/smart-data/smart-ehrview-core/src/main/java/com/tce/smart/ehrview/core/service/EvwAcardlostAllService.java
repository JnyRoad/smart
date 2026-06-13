package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwAcardlostAll;

import java.util.List;

public interface EvwAcardlostAllService extends IService<EvwAcardlostAll> {
	List<EvwAcardlostAll> list(String badge, String queryMonth);
}
