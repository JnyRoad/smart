package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwBizLregleave;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-20 19:00
 */
public interface EvwBizLregleaveService extends IService<EvwBizLregleave> {
	List<EvwBizLregleave> list(String badge, String queryMonth);
}
