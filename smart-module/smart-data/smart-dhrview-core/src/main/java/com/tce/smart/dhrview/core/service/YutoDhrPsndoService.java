package com.tce.smart.dhrview.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.dhrview.core.entity.YutoDhrPsndo;

import java.util.List;

/**
 * @description: YutoDhrPsndoService
 * @date: 2021/5/27 0027 15:48
 * @author: wuling
 * @version: 1.0
 */
public interface YutoDhrPsndoService {

	IPage<YutoDhrPsndo> getPage(Page page, List<Integer> buIds);

	YutoDhrPsndo getByBadge(String badge);

	YutoDhrPsndo getByUserId(String userId);

	List<YutoDhrPsndo> getByCompId(Integer compId);

	List<YutoDhrPsndo> getInStaffByCompId(Integer compId);
}
