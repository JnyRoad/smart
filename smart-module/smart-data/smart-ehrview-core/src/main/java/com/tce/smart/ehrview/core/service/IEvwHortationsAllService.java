package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwHortationsAll;

import java.util.List;

/**
 * @Descripition:
 * @Auther: guohongtai
 * @Date: 2020-07-13 18:03
 */

public interface IEvwHortationsAllService extends IService<EvwHortationsAll> {
	List<EvwHortationsAll> list(String badge, String queryMonth);
}
