package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwCallowanceAllt;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 10:50
 */

public interface IEvwCallowanceAlltService extends IService<EvwCallowanceAllt> {
	List<EvwCallowanceAllt> list(String badge, String queryMonth);
}
