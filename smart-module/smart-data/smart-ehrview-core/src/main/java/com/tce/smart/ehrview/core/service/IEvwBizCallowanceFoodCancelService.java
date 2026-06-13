package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwBizCallowanceFoodCancel;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 11:06
 */

public interface IEvwBizCallowanceFoodCancelService extends IService<EvwBizCallowanceFoodCancel> {
	List<EvwBizCallowanceFoodCancel> list(String badge, String queryMonth);
}
