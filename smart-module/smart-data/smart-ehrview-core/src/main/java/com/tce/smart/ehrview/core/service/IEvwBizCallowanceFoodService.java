package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwBizCallowanceFood;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 10:50
 */

public interface IEvwBizCallowanceFoodService extends IService<EvwBizCallowanceFood> {
	List<EvwBizCallowanceFood> list(String badge, String queryMonth);
}
