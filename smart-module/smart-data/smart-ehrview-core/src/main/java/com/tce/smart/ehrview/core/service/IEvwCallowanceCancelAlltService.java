package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwCallowanceCancelAllt;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 10:50
 */

public interface IEvwCallowanceCancelAlltService extends IService<EvwCallowanceCancelAllt> {
	List<EvwCallowanceCancelAllt> list(String badge, String queryMonth);
}
