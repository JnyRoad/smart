package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwBizCallowance;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 09:32
 */

public interface IEvwBizCallowanceService extends IService<EvwBizCallowance> {
	List<EvwBizCallowance> list(String badge, String queryMonth);
}
