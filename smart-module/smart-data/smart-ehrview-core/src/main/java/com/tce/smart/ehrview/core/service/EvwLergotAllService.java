package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwLergotAll;

import java.util.List;

public interface EvwLergotAllService extends IService<EvwLergotAll> {
	List<EvwLergotAll> list(String badge, String queryMonth);
}
